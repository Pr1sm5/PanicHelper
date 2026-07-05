package com.example.panichelper.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.annotation.RawRes

/**
 * Universeller Sound-Player fürs ganze Projekt: kurze Hinweis-Clips über
 * einen [SoundPool] (Zufallsauswahl innerhalb einer [SoundCategory], nie
 * zweimal hintereinander dieselbe Variante) sowie optional eine leise
 * Hintergrundmusik in Endlosschleife über [MediaPlayer].
 *
 * Kennt keine bildschirmspezifischen Inhalte – nur [SoundCategory]. Jeder
 * Screen-Bereich definiert sein eigenes Kategorien-Enum (siehe
 * `BreathingSoundCategory` für ein Beispiel inkl. Auswahl-Logik) und kann
 * dieselbe Engine wiederverwenden, z. B. später für Sounds im Ablenkungs-Screen.
 *
 * Eine Instanz gehört zu genau einem Screen-Aufenthalt (per `remember {}`
 * erzeugt, per [release] beim Verlassen aufgeräumt).
 *
 * @param isEnabled Wird vor jeder Wiedergabe geprüft (z. B. eine per
 * `collectAsState()` gebundene Einstellung) – so kann Audio global aus- und
 * eingeschaltet werden, ohne dass jede Aufrufstelle das selbst prüfen muss.
 */
class SoundPlayer(
    context: Context,
    private val isEnabled: () -> Boolean = { true }
) {

    private val appContext = context.applicationContext

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        .build()

    // Lädt beim Erstellen alle Varianten der bereits bekannten Kategorien vor.
    // load(category) ergänzt weitere Kategorien bei Bedarf nachträglich.
    private val soundIdsByResId = mutableMapOf<Int, Int>()

    // Zuletzt gespielte Variante je Kategorie, um Wiederholungen zu vermeiden.
    private val lastPlayedResId = mutableMapOf<SoundCategory, Int>()

    private var backgroundMusicPlayer: MediaPlayer? = null

    /** Lädt die Varianten einer Kategorie vor (asynchron), falls noch nicht geschehen. */
    fun preload(category: SoundCategory) {
        for (resId in category.variants) {
            soundIdsByResId.getOrPut(resId) { soundPool.load(appContext, resId, 1) }
        }
    }

    /** Spielt eine zufällige Variante der Kategorie, nie dieselbe wie beim letzten Mal. */
    fun play(category: SoundCategory) {
        if (!isEnabled()) return
        preload(category)

        val variants = category.variants
        if (variants.isEmpty()) return
        val previous = lastPlayedResId[category]
        val choices = if (variants.size > 1) variants.filterNot { it == previous } else variants
        val chosenResId = choices.random()
        lastPlayedResId[category] = chosenResId

        val soundId = soundIdsByResId[chosenResId] ?: return
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    /** Startet eine leise Hintergrundmusik in Endlosschleife (kein Effekt, falls schon aktiv). */
    fun startBackgroundMusic(@RawRes resId: Int, volume: Float = DEFAULT_BACKGROUND_MUSIC_VOLUME) {
        if (!isEnabled() || backgroundMusicPlayer != null) return
        backgroundMusicPlayer = MediaPlayer.create(appContext, resId)?.apply {
            isLooping = true
            setVolume(volume, volume)
            start()
        }
    }

    fun stopBackgroundMusic() {
        backgroundMusicPlayer?.release()
        backgroundMusicPlayer = null
    }

    /** Gibt alle Ressourcen frei. Muss beim Verlassen des Screens aufgerufen werden. */
    fun release() {
        stopBackgroundMusic()
        soundPool.release()
    }

    companion object {
        private const val MAX_STREAMS = 2
        const val DEFAULT_BACKGROUND_MUSIC_VOLUME = 0.3f
    }
}
