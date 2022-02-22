package com.lhwdev.compose.window.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import com.lhwdev.compose.window.CustomWindow


fun main() = application {
	CustomWindow(onCloseRequest = ::exitApplication, undecorated = true) {
		Box(Modifier.background(Color.Blue).size(100.dp))
	}
}
