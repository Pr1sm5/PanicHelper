package com.example.panichelper.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.annotation.RawRes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "SoundPlayer"

/**
 * Universeller Sound-Player fürs ganze Projekt: kurze Hinweis-Clips über
 * einen [SoundPool] (Zufallsauswahl innerhalb einer [SoundCategory], nie
 * zweimal hintereinander dieselbe Variante) sowie optional eine leise
 * Hintergrundmusik in Endlosschleife über [ExoPlayer].
 *
 * Für die Hintergrundmusik kommt bewusst ExoPlayer statt `MediaPlayer` zum
 * Einsatz: `MediaPlayer.seekTo()`/`start()` sind blockierende native
 * Aufrufe - hängt der Decoder auf einem Gerät einmal (auf einem Pixel 8 Pro
 * beobachtet, vermutlich ein kaputter Codec2-mp3-Decoder), blockiert das
 * für immer den Coroutine-Mutex und damit die komplette Musik-Wiedergabe.
 * ExoPlayers Steuermethoden (`play()`/`seekTo()`/...) reichen Anweisungen
 * dagegen nur an einen internen Wiedergabe-Thread weiter und kehren sofort
 * zurück - ein hängender Decoder kann so nie unseren Code blockieren.
 * ExoPlayer darf allerdings nur vom Thread aus gesteuert werden, auf dem es
 * erstellt wurde (hier: der Hauptthread, über [mainScope]).
 *
 * [SoundPool.load] öffnet Dateien und bereitet die Dekodierung synchron vor
 * - auf dem Hauptthread aufgerufen (z. B. direkt aus `remember {}`/
 * `LaunchedEffect`) blockiert das die UI spürbar, gerade beim Vorladen
 * mehrerer Kategorien auf einmal beim Betreten eines Screens. Diese Klasse
 * verlagert deshalb jedes Laden auf einen eigenen IO-Dispatcher-Scope; nur
 * die schnellen Freigabe-Aufrufe in [release] laufen synchron.
 *
 * Sowohl einzelne Ladevorgänge als auch die Scopes insgesamt fangen Fehler
 * ab und loggen sie nur, statt die App abstürzen zu lassen - manche Geräte/
 * Emulatoren (insbesondere der Android-Studio-Emulator mit seinem
 * softwarebasierten Audio-Backend) können beim Dekodieren einzelner Clips
 * scheitern, obwohl auf echten Geräten alles funktioniert; das darf nie mehr
 * als "dieser eine Sound bleibt stumm" bedeuten.
 *
 * Kennt keine bildschirmspezifischen Inhalte – nur [SoundCategory]. Jeder
 * Screen-Bereich definiert sein eigenes Kategorien-Enum (siehe
 * `BreathingSoundCategory` für ein Beispiel inkl. Auswahl-Logik) und kann
 * dieselbe Engine wiederverwenden, z. B. später für Sounds im Ablenkungs-Screen.
 *
 * Eine Instanz gehört zu genau einem Screen-Aufenthalt (per `remember {}`
 * erzeugt, per [release] beim Verlassen aufgeräumt).
 *
 * @param categoriesToPreload Kategorien, deren Varianten sofort beim Erstellen
 * im Hintergrund geladen werden. Ohne Vorladen würde die erste Wiedergabe
 * einer Kategorie oft stumm bleiben, weil das Laden Zeit braucht und ein
 * `play()` auf eine noch nicht fertig geladene Sample-ID wirkungslos verpufft.
 * @param isEnabled Wird vor jeder Wiedergabe geprüft (z. B. eine per
 * `collectAsState()` gebundene Einstellung) – so kann Audio global aus- und
 * eingeschaltet werden, ohne dass jede Aufrufstelle das selbst prüfen muss.
 */
class SoundPlayer(
    context: Context,
    categoriesToPreload: List<SoundCategory> = emptyList(),
    private val isEnabled: () -> Boolean = { true }
) {

    private val appContext = context.applicationContext

    // Eigener Hintergrund-Scope fürs Laden der kurzen SoundPool-Clips, damit
    // das nie den Hauptthread (Compose-Komposition/Recomposition) blockiert.
    // Der ExceptionHandler ist ein zusätzliches Sicherheitsnetz: Sollte trotz
    // der try/catches unten irgendwo eine Ausnahme durchrutschen, wird sie
    // geloggt statt den Prozess abstürzen zu lassen.
    private val backgroundScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, error ->
            Log.w(TAG, "Unerwarteter Fehler in einer Audio-Hintergrundaufgabe", error)
        }
    )

    // ExoPlayer darf nur vom Thread aus gesteuert werden, auf dem es erstellt
    // wurde - deshalb ein eigener, auf den Hauptthread festgelegter Scope nur
    // für die Hintergrundmusik.
    private val mainScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate + CoroutineExceptionHandler { _, error ->
            Log.w(TAG, "Unerwarteter Fehler bei der Hintergrundmusik-Steuerung", error)
        }
    )

    @Volatile
    private var isReleased = false

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        .build()

    // Thread-sicher, da sowohl vom Vorlade-Hintergrundthread als auch
    // (im Notfall-Nachladepfad von play()) vom Hauptthread beschrieben.
    private val soundIdsByResId = ConcurrentHashMap<Int, Int>()

    // Zuletzt gespielte Variante je Kategorie, um Wiederholungen zu vermeiden.
    // Wird ausschließlich aus play() heraus verändert (immer derselbe Aufrufer-Thread).
    private val lastPlayedResId = mutableMapOf<SoundCategory, Int>()

    // Von SoundPool tatsächlich fertig dekodierte Sample-IDs. Erst wenn eine
    // ID hier drin ist, darf play() sie an soundPool.play() übergeben -
    // andernfalls verpufft die Wiedergabe lautlos (SoundPool ignoriert
    // play()-Aufrufe für noch nicht fertig geladene IDs).
    private val loadedSoundIds = ConcurrentHashMap.newKeySet<Int>()

    // Nur vom mainScope (Hauptthread) aus gelesen/verändert - kein zusätzlicher
    // Lock nötig, da Dispatchers.Main.immediate alles auf einen Thread verengt.
    private var backgroundMusicPlayer: ExoPlayer? = null
    private var musicWatchdogJob: Job? = null

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedSoundIds.add(sampleId)
        }
        backgroundScope.launch {
            categoriesToPreload.forEach { loadCategory(it) }
        }
    }

    private fun loadCategory(category: SoundCategory) {
        for (resId in category.variants) {
            if (soundIdsByResId.containsKey(resId)) continue
            try {
                soundIdsByResId[resId] = soundPool.load(appContext, resId, 1)
            } catch (error: Exception) {
                Log.w(TAG, "Konnte Sound-Ressource $resId nicht laden, überspringe sie", error)
            }
        }
    }

    /** Lädt die Varianten einer Kategorie im Hintergrund vor, falls noch nicht geschehen. */
    fun preload(category: SoundCategory) {
        backgroundScope.launch { loadCategory(category) }
    }

    /**
     * Spielt eine zufällige Variante der Kategorie, nie dieselbe wie beim letzten Mal.
     *
     * Ist die gewählte Variante noch nicht (fertig) geladen - z. B. weil dieser
     * Aufruf direkt nach dem Erstellen des Players kommt, bevor das
     * Hintergrund-Vorladen durch ist -, wartet diese Funktion bis zu
     * [LOAD_WAIT_TIMEOUT_MS] auf den Ladevorgang, statt die Wiedergabe einfach
     * zu verwerfen. Genau das war der Grund, warum z. B. der Start-Hinweis nie
     * zu hören war: er wird sofort beim Betreten des Screens ausgelöst, lange
     * bevor SoundPool die Datei dekodiert hat.
     */
    suspend fun play(category: SoundCategory) {
        if (!isEnabled()) return
        val variants = category.variants
        if (variants.isEmpty()) return

        val previous = lastPlayedResId[category]
        val choices = if (variants.size > 1) variants.filterNot { it == previous } else variants
        val chosenResId = choices.random()
        lastPlayedResId[category] = chosenResId

        var soundId = soundIdsByResId[chosenResId]
        if (soundId == null) {
            withContext(Dispatchers.IO) { loadCategory(category) }
            soundId = soundIdsByResId[chosenResId] ?: return
        }

        var waited = 0L
        while (soundId !in loadedSoundIds && waited < LOAD_WAIT_TIMEOUT_MS) {
            delay(LOAD_POLL_INTERVAL_MS)
            waited += LOAD_POLL_INTERVAL_MS
        }
        if (soundId !in loadedSoundIds) return

        try {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } catch (error: Exception) {
            Log.w(TAG, "Konnte Sound-ID $soundId nicht abspielen", error)
        }
    }

    /**
     * Startet eine leise Hintergrundmusik in Endlosschleife (kein Effekt, falls schon aktiv).
     *
     * Nutzt ExoPlayers eingebautes [Player.REPEAT_MODE_ONE] fürs Schleifen -
     * das passiert komplett innerhalb von ExoPlayers eigenem Wiedergabe-Thread,
     * ganz ohne manuelles `seekTo`/`start` unsererseits. Ein zusätzlicher
     * [ensureMusicWatchdog] prüft periodisch den Zustand und stößt bei Bedarf
     * eine Neuvorbereitung an (z. B. nach einem [Player.Listener.onPlayerError]).
     */
    fun startBackgroundMusic(@RawRes resId: Int, volume: Float = DEFAULT_BACKGROUND_MUSIC_VOLUME) {
        if (!isEnabled() || isReleased) return
        mainScope.launch {
            if (isReleased || backgroundMusicPlayer != null || !isEnabled()) return@launch
            val player = try {
                ExoPlayer.Builder(appContext).build()
            } catch (error: Exception) {
                Log.w(TAG, "Konnte ExoPlayer nicht erstellen", error)
                return@launch
            }
            try {
                player.setMediaItem(MediaItem.fromUri(RawResourceDataSource.buildRawResourceUri(resId)))
                player.repeatMode = Player.REPEAT_MODE_ONE
                player.volume = volume
                player.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Log.w(TAG, "ExoPlayer-Fehler bei der Hintergrundmusik, versuche Neuvorbereitung", error)
                        if (backgroundMusicPlayer === player) {
                            try {
                                player.prepare()
                                player.playWhenReady = true
                            } catch (recoverError: Exception) {
                                Log.w(TAG, "Konnte nach ExoPlayer-Fehler nicht neu starten", recoverError)
                            }
                        }
                    }
                })
                player.playWhenReady = true
                player.prepare()
                backgroundMusicPlayer = player
                ensureMusicWatchdog(resId, volume)
            } catch (error: Exception) {
                Log.w(TAG, "Konnte Hintergrundmusik nicht starten", error)
                player.release()
            }
        }
    }

    /**
     * Prüft in regelmäßigen Abständen, ob die Hintergrundmusik wirklich noch
     * läuft, und stößt sonst eine Neuvorbereitung an. Läuft nur einmal pro
     * aktiver Wiedergabe (kein Effekt, falls schon ein Watchdog aktiv ist).
     * Da alle ExoPlayer-Aufrufe nicht-blockierend sind, kann dieser Watchdog
     * selbst nie hängen bleiben - anders als der alte MediaPlayer-Ansatz.
     */
    private fun ensureMusicWatchdog(resId: Int, volume: Float) {
        if (musicWatchdogJob?.isActive == true) return
        musicWatchdogJob = mainScope.launch {
            while (isActive) {
                delay(MUSIC_WATCHDOG_INTERVAL_MS)
                if (isReleased || !isEnabled()) continue
                val player = backgroundMusicPlayer
                if (player == null) {
                    startBackgroundMusic(resId, volume)
                    continue
                }
                Log.d(
                    TAG,
                    "Watchdog-Check: isPlaying=${player.isPlaying} state=${player.playbackState} " +
                        "playWhenReady=${player.playWhenReady} position=${player.currentPosition}ms"
                )
                when {
                    player.playbackState == Player.STATE_IDLE -> {
                        Log.w(TAG, "Hintergrundmusik im IDLE-Zustand, bereite neu vor")
                        try {
                            player.prepare()
                            player.playWhenReady = true
                        } catch (error: Exception) {
                            Log.w(TAG, "Neuvorbereitung fehlgeschlagen, baue Player neu auf", error)
                            releaseBackgroundMusicPlayerLocked()
                            startBackgroundMusic(resId, volume)
                        }
                    }
                    player.playbackState == Player.STATE_READY && !player.isPlaying -> {
                        Log.w(TAG, "Hintergrundmusik bereit, spielt aber nicht - stoße Wiedergabe an")
                        player.playWhenReady = true
                        player.play()
                    }
                }
            }
        }
    }

    fun stopBackgroundMusic() {
        mainScope.launch {
            musicWatchdogJob?.cancel()
            musicWatchdogJob = null
            releaseBackgroundMusicPlayerLocked()
        }
    }

    private fun releaseBackgroundMusicPlayerLocked() {
        try {
            backgroundMusicPlayer?.release()
        } catch (error: Exception) {
            Log.w(TAG, "Fehler beim Freigeben der Hintergrundmusik", error)
        }
        backgroundMusicPlayer = null
    }

    /** Gibt alle Ressourcen frei. Muss beim Verlassen des Screens aufgerufen werden. */
    fun release() {
        isReleased = true
        musicWatchdogJob?.cancel()
        musicWatchdogJob = null
        backgroundScope.cancel()
        try {
            backgroundMusicPlayer?.release()
        } catch (error: Exception) {
            Log.w(TAG, "Fehler beim Freigeben der Hintergrundmusik", error)
        }
        backgroundMusicPlayer = null
        mainScope.cancel()
        try {
            soundPool.release()
        } catch (error: Exception) {
            Log.w(TAG, "Fehler beim Freigeben des SoundPools", error)
        }
    }

    companion object {
        private const val MAX_STREAMS = 2
        const val DEFAULT_BACKGROUND_MUSIC_VOLUME = 0.3f
        private const val LOAD_WAIT_TIMEOUT_MS = 3_000L
        private const val LOAD_POLL_INTERVAL_MS = 50L
        private const val MUSIC_WATCHDOG_INTERVAL_MS = 2_000L
    }
}
