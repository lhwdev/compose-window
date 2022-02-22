@file:Suppress("SpellCheckingInspection")

package com.lhwdev.compose.window.platform.windows.win32

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinNT.HRESULT
import com.sun.jna.win32.W32APIOptions


interface DesktopWindowManager : Library {
	companion object {
		val instance = Native.load("dwmapi", DesktopWindowManager::class.java, W32APIOptions.DEFAULT_OPTIONS)
	}
	
	fun DwmExtendFrameIntoClientArea(hwnd: HWND, pMarInset: MARGINS): HRESULT
}


val dwm = DesktopWindowManager.instance
