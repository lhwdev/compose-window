package com.lhwdev.compose.window.platform.windows.win32


enum class HitTest(val value: Int) {
	nowhere(0),
	transparent(-1),
	
	topLeft(13),
	top(12),
	caption(2),
	topRight(14),
	left(10),
	right(11),
	bottomLeft(16),
	bottom(15),
	bottomRight(17),
	sysMenu(3),
}
