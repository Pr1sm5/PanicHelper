package com.example.panichelper.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import androidx.annotation.RawRes
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "SoundPlayer"

/**
 * Universeller Sound-Player fürs ganze Projekt: kurze Hinweis-Clips über
 * einen [SoundPool] (Zufallsauswahl innerhalb einer [SoundCategory], nie
 * zweimal hintereinander dieselbe Variante) sowie optional eine leise
 * Hintergrundmusik in Endlosschleife über [MediaPlayer].
 *
 * [SoundPool.load] und [MediaPlayer.create] öffnen Dateien und bereiten die
 * Dekodierung synchron vor - auf dem Hauptthread aufgerufen (z. B. direkt aus
 * `remember {}`/`LaunchedEffect`) blockiert das die UI spürbar, gerade beim
 * Vorladen mehrerer Kategorien auf einmal beim Betreten eines Screens. Diese
 * Klasse verlagert deshalb jedes Laden/Erstellen auf einen eigenen
 * IO-Dispatcher-Scope; nur die schnellen Freigabe-Aufrufe in [release]
 * laufen synchron.
 *
 * Sowohl einzelne Ladevorgänge als auch der Scope insgesamt fangen Fehler ab
 * und loggen sie nur, statt die App abstürzen zu lassen - manche Geräte/
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

    // Eigener Hintergrund-Scope fürs Laden/Erstellen, damit das nie den
    // Hauptthread (Compose-Komposition/Recomposition) blockiert. Der
    // ExceptionHandler ist ein zusätzliches Sicherheitsnetz: Sollte trotz der
    // try/catches unten irgendwo eine Ausnahme durchrutschen, wird sie
    // geloggt statt den Prozess abstürzen zu lassen.
    private val backgroundScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, error ->
            Log.w(TAG, "Unerwarteter Fehler in einer Audio-Hintergrundaufgabe", error)
        }
    )
    private val musicMutex = Mutex()

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

    private var backgroundMusicPlayer: MediaPlayer? = null

    init {
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

    /** Spielt eine zufällige Variante der Kategorie, nie dieselbe wie beim letzten Mal. */
    fun play(category: SoundCategory) {
        if (!isEnabled()) return
        val variants = category.variants
        if (variants.isEmpty()) return

        val previous = lastPlayedResId[category]
        val choices = if (variants.size > 1) variants.filterNot { it == previous } else variants
        val chosenResId = choices.random()
        lastPlayedResId[category] = chosenResId

        val soundId = soundIdsByResId[chosenResId]
        if (soundId == null) {
            // Variante war noch nicht (fertig) geladen - im Hintergrund nachladen,
            // damit zumindest die nächste Wiedergabe dieser Kategorie klappt,
            // statt den Aufrufer-Thread für den Ladevorgang zu blockieren.
            preload(category)
            return
        }
        try {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } catch (error: Exception) {
            Log.w(TAG, "Konnte Sound-ID $soundId nicht abspielen", error)
        }
    }

    /** Startet eine leise Hintergrundmusik in Endlosschleife (kein Effekt, falls schon aktiv). */
    fun startBackgroundMusic(@RawRes resId: Int, volume: Float = DEFAULT_BACKGROUND_MUSIC_VOLUME) {
        if (!isEnabled() || isReleased) return
        backgroundScope.launch {
            val player = try {
                MediaPlayer.create(appContext, resId)
            } catch (error: Exception) {
                Log.w(TAG, "Konnte Hintergrundmusik nicht erstellen", error)
                null
            } ?: return@launch

            musicMutex.withLock {
                if (isReleased || backgroundMusicPlayer != null || !isEnabled()) {
                    player.release()
                    return@withLock
                }
                try {
                    player.isLooping = true
                    player.setVolume(volume, volume)
                    player.start()
                    backgroundMusicPlayer = player
                } catch (error: Exception) {
                    Log.w(TAG, "Konnte Hintergrundmusik nicht starten", error)
                    player.release()
                }
            }
        }
    }

    fun stopBackgroundMusic() {
        backgroundScope.launch {
            musicMutex.withLock {
                releaseBackgroundMusicPlayerLocked()
            }
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
        backgroundScope.cancel()
        try {
            backgroundMusicPlayer?.release()
        } catch (error: Exception) {
            Log.w(TAG, "Fehler beim Freigeben der Hintergrundmusik", error)
        }
        backgroundMusicPlayer = null
        try {
            soundPool.release()
        } catch (error: Exception) {
            Log.w(TAG, "Fehler beim Freigeben des SoundPools", error)
        }
    }

    companion object {
        private const val MAX_STREAMS = 2
        const val DEFAULT_BACKGROUND_MUSIC_VOLUME = 0.3f
    }
}
