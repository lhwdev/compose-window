package com.lhwdev.compose.window

import androidx.compose.ui.awt.ComposeWindow
import com.lhwdev.compose.window.platform.windows.WindowsCustomComposeWindow


fun CustomComposeWindow(): ComposeWindow {
	val hostOs = System.getProperty("os.name")
	val isMingwX64 = hostOs.startsWith("Windows")
	
	return when {
		// hostOs == "Mac OS X" -> 
		// hostOs == "Linux" -> linuxX64("native")
		isMingwX64 -> WindowsCustomComposeWindow()
		else -> error("Host OS is not supported yet.")
	}
}
