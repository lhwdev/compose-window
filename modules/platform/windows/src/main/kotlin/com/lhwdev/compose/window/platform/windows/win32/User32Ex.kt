// Copyright 2020 Kalkidan Betre Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:Suppress("SpellCheckingInspection")

package com.lhwdev.compose.window.platform.windows.win32

import com.sun.jna.Native
import com.sun.jna.platform.win32.BaseTSD.LONG_PTR
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.*
import com.sun.jna.platform.win32.WinUser.WindowProc
import com.sun.jna.win32.W32APIOptions


interface User32Ex : User32 {
	companion object {
		const val GWLP_WNDPROC = -4
		
		const val WM_NCCALCSIZE = 0x0083
		const val WM_NCHITTEST = 0x0084
		
		val instance = Native.load("user32", User32Ex::class.java, W32APIOptions.DEFAULT_OPTIONS)
	}
	
	fun SetWindowLong(hWnd: HWND, nIndex: Int, wndProc: WindowProc): LONG_PTR
	fun SetWindowLong(hWnd: HWND, nIndex: Int, wndProc: LONG_PTR): LONG_PTR
	fun SetWindowLongPtr(hWnd: HWND, nIndex: Int, wndProc: WindowProc): LONG_PTR
	fun SetWindowLongPtr(hWnd: HWND, nIndex: Int, wndProc: LONG_PTR): LONG_PTR
	fun CallWindowProc(proc: LONG_PTR, hWnd: HWND, uMsg: Int, uParam: WPARAM, lParam: LPARAM): LRESULT
}


val user32 = User32Ex.instance
