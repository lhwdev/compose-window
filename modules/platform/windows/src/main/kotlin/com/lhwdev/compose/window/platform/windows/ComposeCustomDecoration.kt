@file:Suppress("SpellCheckingInspection")

package com.lhwdev.compose.window.platform.windows

import androidx.compose.ui.awt.ComposeWindow
import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef.HWND
import java.awt.Container
import java.awt.event.WindowEvent
import java.awt.event.WindowListener


internal fun ComposeWindow.initCustomComposeWindow() {
	addWindowListener(object : WindowListener {
		var decorationRef: Any? = null // long living reference preventing GC
		var decorationRef2: Any? = null // long living reference preventing GC
		
		override fun windowOpened(e: WindowEvent?) {
			// ComposeWindow.windowHandle:
			// > Retrieve underlying platform-specific operating system handle for the root window where ComposeWindow is
			//   rendered. Currently returns HWND on Windows ...
			// @Suppress("ExplicitThis")
			// val windowHandle = HWND(Pointer(this.windowHandle))
			val windowHandle = HWND(Native.getComponentPointer(this@initCustomComposeWindow))
			
			// As skia internally uses AWT canvas, (java.awt.Canvas) it should do the same thing to that child window.
			// As you may know, AWT stands for Abstract **Window** Toolkit.
			val contentWindowHandle = HWND(
				Native.getComponentPointer(
					((contentPane.getComponent(0) as Container) // ComposeWindowDelegate.pane: JLayeredPane
						.getComponent(0) as Container) // pane.add(layer.component, ...) <- ComposeLayer.component: SkiaLayer
						.getComponent(0) // SkiaLayer.backedLayer: HardwareLayer which extends java.awt.Canvas
				)
			)
			// contentPane.getComponent(0) as Container
			// val composeHWND =
			// 	((window.window.contentPane.getComponent(0) as Container).getComponent(0) as Container).getComponent(0).asHWND()
			println("oh")
			val params = CustomDecorationParametersOld(
				controlBoxWidth = 0,
				iconWidth = 0
			)
			val decoration = CustomDecorationWindowProc(windowHandle, params)
			decorationRef = decoration
			
			val contentDecoration = TransparentDecorationWindowProc(contentWindowHandle, parent = decoration)
			decorationRef2 = contentDecoration
		}
		
		override fun windowClosing(e: WindowEvent?) {}
		override fun windowClosed(e: WindowEvent?) {}
		override fun windowIconified(e: WindowEvent?) {}
		override fun windowDeiconified(e: WindowEvent?) {}
		override fun windowActivated(e: WindowEvent?) {}
		override fun windowDeactivated(e: WindowEvent?) {}
	})
}


/*fun Component.asHWND(): HWND {
	val hwnd = HWND()
	hwnd.pointer = Native.getComponentPointer(this)
	return hwnd
}

fun initCustomDecoration(window: AppWindow): Any? {
	val windowHWND = window.window.asHWND()
	val composeHWND =
		((window.window.contentPane.getComponent(0) as Container).getComponent(0) as Container).getComponent(0).asHWND()
	
	window.window.addWindowListener(object : WindowListener {
		private var windowDecoration: Any? = null
		
		override fun windowOpened(e: WindowEvent) {
			val params = CustomDecorationParameters()
			windowDecoration = CustomDecorationWindowProc(windowHWND, params) to
				TransparentDecorationWindowProc(composeHWND, params)
			// windowDecoration = CustomDecorationWindowProc(windowHWND, params)
		}
		
		override fun windowClosing(e: WindowEvent) {
		}
		
		override fun windowClosed(e: WindowEvent) {
		}
		
		override fun windowIconified(e: WindowEvent) {
		}
		
		override fun windowDeiconified(e: WindowEvent) {
		}
		
		override fun windowActivated(e: WindowEvent) {
		}
		
		override fun windowDeactivated(e: WindowEvent) {
		}
	})
	
	return null
}*/

