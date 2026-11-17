package com.example.model

enum class SleepTimerMode {
    OFF,
    COUNTDOWN,
    END_OF_TRACK
}

data class SleepTimerState(
    val mode: SleepTimerMode = SleepTimerMode.OFF,
    val remainingMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val isFadingOut: Boolean = false
) {
    val isActive: Boolean get() = mode != SleepTimerMode.OFF
}
