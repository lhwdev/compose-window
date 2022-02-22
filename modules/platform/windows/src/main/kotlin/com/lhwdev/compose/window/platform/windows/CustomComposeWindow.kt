package com.lhwdev.compose.window.platform.windows

import androidx.compose.ui.awt.ComposeWindow


fun WindowsCustomComposeWindow(): ComposeWindow = ComposeWindow().apply {
	initCustomComposeWindow()
}
