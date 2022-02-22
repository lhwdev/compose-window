package com.lhwdev.compose.window.platform.windows.win32

import com.sun.jna.Structure


data class MARGINS(
	@JvmField
	val cxLeftWidth: Int,
	@JvmField
	val cxRightWidth: Int,
	@JvmField
	val cyTopHeight: Int,
	@JvmField
	val cyBottomHeight: Int
) : Structure() {
	override fun getFieldOrder() = listOf("cxLeftWidth", "cxRightWidth", "cyTopHeight", "cyBottomHeight")
}

