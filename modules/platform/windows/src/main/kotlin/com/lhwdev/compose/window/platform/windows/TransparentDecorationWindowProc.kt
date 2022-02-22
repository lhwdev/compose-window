// Copyright 2020 Kalkidan Betre Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:Suppress("SpellCheckingInspection")

package com.lhwdev.compose.window.platform.windows

import com.lhwdev.compose.window.platform.windows.win32.User32Ex
import com.lhwdev.compose.window.platform.windows.win32.User32Ex.Companion.WM_NCCALCSIZE
import com.lhwdev.compose.window.platform.windows.win32.User32Ex.Companion.WM_NCHITTEST
import com.lhwdev.compose.window.platform.windows.win32.user32
import com.sun.jna.platform.win32.BaseTSD
import com.sun.jna.platform.win32.WinDef.*
import com.sun.jna.platform.win32.WinUser.*


/**
 * Some workaround where child window blocks the area of parent window causing some events like HTCAPTION is swallowed.
 */
internal class TransparentDecorationWindowProc(hwnd: HWND, val parent: CustomDecorationWindowProc) : WindowProc {
	val defWndProc: BaseTSD.LONG_PTR =
		if(is64Bit()) user32.SetWindowLongPtr(hwnd, User32Ex.GWLP_WNDPROC, this)
		else user32.SetWindowLong(hwnd, User32Ex.GWLP_WNDPROC, this)
	
	
	init {
		user32.SetWindowPos(
			hwnd, hwnd, 0, 0, 0, 0,
			SWP_NOMOVE or SWP_NOSIZE or SWP_NOZORDER or SWP_FRAMECHANGED
		)
	}
	
	override fun callback(hwnd: HWND, uMsg: Int, wparam: WPARAM, lparam: LPARAM): LRESULT {
		return when(uMsg) {
			WM_NCCALCSIZE -> LRESULT(0)
			WM_NCHITTEST -> {
				val lresult = parent.params.hitTest(hwnd)
				when(lresult.toInt()) {
					0 -> user32.CallWindowProc(
						defWndProc,
						hwnd,
						uMsg,
						wparam,
						lparam
					) // HTNOWHERE -> deliver to this child window
					1 -> { // HTCLIENT -> swallow... but hitTest never returns this?! why I made this
						println("if I see this message, preserve this line")
						lresult
					}
					else -> LRESULT(-1) // HTTRANSPARENT -> pass to parent window
				}
			}
			WM_DESTROY -> {
				if(is64Bit()) user32.SetWindowLongPtr(
					hwnd,
					User32Ex.GWLP_WNDPROC,
					defWndProc
				) else user32.SetWindowLong(hwnd, User32Ex.GWLP_WNDPROC, defWndProc)
				LRESULT(0)
			}
			else -> user32.CallWindowProc(defWndProc, hwnd, uMsg, wparam, lparam)
		}
	}
}
