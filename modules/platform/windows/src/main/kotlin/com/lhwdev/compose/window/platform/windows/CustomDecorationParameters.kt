// Copyright 2020 Kalkidan Betre Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:Suppress("NOTHING_TO_INLINE")

package com.lhwdev.compose.window.platform.windows

import androidx.compose.runtime.Immutable


// @Immutable
// data class CustomDecorationParameters()


@Immutable
data class CustomDecorationParametersOld(
	val titleBarHeight: Int = 27,
	val controlBoxWidth: Int = 150,
	val iconWidth: Int = 40,
	val extraLeftReservedWidth: Int = 0,
	val extraRightReservedWidth: Int = 0,
	val maximizedWindowFrameThickness: Int = 10,
	val frameResizeBorderThickness: Int = 4,
	val frameBorderThickness: Int = 1
)
