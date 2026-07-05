package com.example.panichelper.audio

/**
 * Eine Gruppe austauschbarer Sound-Varianten (z. B. "Einatmen"-Hinweise mit
 * mehreren Sprecher-Aufnahmen, jeweils als @RawRes-Ressourcen-ID). Jeder
 * Screen-Bereich definiert dafür sein eigenes Enum, das dieses Interface
 * implementiert (siehe z. B. `BreathingSoundCategory`) – [SoundPlayer] selbst
 * kennt keine bildschirmspezifischen Inhalte, nur diesen Vertrag.
 */
interface SoundCategory {
    val variants: List<Int>
}
