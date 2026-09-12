package com.remote.tvremote

/**
 * Standard Android KeyEvent codes. These are the same codes `adb shell input
 * keyevent <code>` expects, so we don't need any extra mapping.
 */
object KeyCodes {
    const val HOME = 3
    const val BACK = 4
    const val UP = 19
    const val DOWN = 20
    const val LEFT = 21
    const val RIGHT = 22
    const val CENTER = 23
    const val VOLUME_UP = 24
    const val VOLUME_DOWN = 25
    const val POWER = 26
    const val MENU = 82
    const val PLAY_PAUSE = 85
    const val REWIND = 89
    const val FAST_FORWARD = 90
    const val MUTE = 164
}
