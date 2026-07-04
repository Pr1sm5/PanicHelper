package com.example.panichelper.ui.screens.breathing

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi

private const val STEP_MS = 50L
private const val AMP_MIN = 30
private const val AMP_MAX = 255

fun triggerBreathingHaptic(
    context: Context,
    visualState: BreathingVisualState,
    durationMillis: Long,
    intensity: Float = 1f
) {
    val vibrator = getVibrator(context)
    val scaledMin = (AMP_MIN * intensity).toInt().coerceIn(1, AMP_MAX)
    val scaledMax = (AMP_MAX * intensity).toInt().coerceIn(scaledMin, AMP_MAX)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val hasAmplitude = vibrator.hasAmplitudeControl()
        val effect = when (visualState) {
            BreathingVisualState.EXPAND ->
                if (hasAmplitude) rampWaveform(durationMillis, rising = true, scaledMin, scaledMax)
                else VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
            BreathingVisualState.CONTRACT ->
                if (hasAmplitude) rampWaveform(durationMillis, rising = false, scaledMin, scaledMax)
                else VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
            BreathingVisualState.HOLD_LARGE, BreathingVisualState.HOLD_SMALL ->
                VibrationEffect.createOneShot(200, scaledMax)
        }
        vibrator.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(durationMillis)
    }
}

fun cancelBreathingHaptic(context: Context) {
    getVibrator(context).cancel()
}

private fun getVibrator(context: Context): Vibrator =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

@RequiresApi(Build.VERSION_CODES.O)
private fun rampWaveform(
    durationMillis: Long,
    rising: Boolean,
    ampMin: Int,
    ampMax: Int
): VibrationEffect {
    val steps = (durationMillis / STEP_MS).toInt().coerceAtLeast(1)
    val timings = LongArray(steps + 1)
    val amplitudes = IntArray(steps + 1)
    timings[0] = 0L
    amplitudes[0] = 0
    for (i in 0 until steps) {
        timings[i + 1] = STEP_MS
        val progress = i.toFloat() / (steps - 1).coerceAtLeast(1)
        amplitudes[i + 1] = if (rising) {
            (ampMin + (ampMax - ampMin) * progress).toInt()
        } else {
            (ampMax - (ampMax - ampMin) * progress).toInt()
        }
    }
    return VibrationEffect.createWaveform(timings, amplitudes, -1)
}
