package com.evsuite.swipe.update

import android.content.Context
/**
 * OTA is suspended while the suite safety and legal audit is open.
 * Keep this flavour seam inert until a reviewed change explicitly re-enables it.
 */
object UpdateHook {
    fun isSupported(): Boolean = false

    /** Fire-and-forget check. Network work runs off the main thread. */
    @JvmStatic
    @JvmOverloads
    fun checkInBackground(
        @Suppress("UNUSED_PARAMETER") context: Context,
        @Suppress("UNUSED_PARAMETER") userInitiated: Boolean = false
    ) = Unit
}
