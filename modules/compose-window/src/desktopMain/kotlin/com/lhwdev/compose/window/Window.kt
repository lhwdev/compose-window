package com.lhwdev.compose.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import kotlin.math.roundToInt


/**
 * Composes custom window in the current composition. When Window enters the composition,
 * a new platform window will be created and receives the focus. When Window leaves the
 * composition, window will be disposed and closed.
 *
 * Only supports Windows so far, and TODO: fallback to custom decorated window.
 * This is useful when you want to customize the window frame but overriding all the native window behaviors like
 * snapping and resizing is not favorable. In Windows, this uses dwm api to customize window api and intercept window
 * events and implement hit test.
 *
 * Initial size of the window is controlled by [WindowState.size].
 * Initial position of the window is controlled by [WindowState.position].
 *
 * Usage in single-window application ([ApplicationScope.exitApplication] will close all the
 * windows and stop all effects defined in [application]):
 * ```
 * fun main() = application {
 *     Window(onCloseRequest = ::exitApplication)
 * }
 * ```
 *
 * or if it only needed to close the main window without closing all other opened windows:
 * ```
 * fun main() = application {
 *     val isOpen by remember { mutableStateOf(true) }
 *     if (isOpen) {
 *         Window(onCloseRequest = { isOpen = false })
 *     }
 * }
 * ```
 *
 * @param onCloseRequest Callback that will be called when the user closes the window.
 * Usually in this callback we need to manually tell Compose what to do:
 * - change `isOpen` state of the window (which is manually defined)
 * - close the whole application (`onCloseRequest = ::exitApplication` in [ApplicationScope])
 * - don't close the window on close request (`onCloseRequest = {}`)
 * @param state The state object to be used to control or observe the window's state
 * When size/position/status is changed by the user, state will be updated.
 * When size/position/status of the window is changed by the application (changing state),
 * the native window will update its corresponding properties.
 * If application changes, for example [WindowState.placement], then after the next
 * recomposition, [WindowState.size] will be changed to correspond the real size of the window.
 * If [WindowState.position] is not [WindowPosition.isSpecified], then after the first show on the
 * screen [WindowState.position] will be set to the absolute values.
 * @param visible Is [Window] visible to user.
 * If `false`:
 * - internal state of [Window] is preserved and will be restored next time the window
 * will be visible;
 * - native resources will not be released. They will be released only when [Window]
 * will leave the composition.
 * @param title Title in the titlebar of the window
 * @param icon Icon in the titlebar of the window (for platforms which support this).
 * On macOS individual windows can't have a separate icon. To change the icon in the Dock,
 * set it via `iconFile` in build.gradle
 * (https://github.com/JetBrains/compose-jb/tree/master/tutorials/Native_distributions_and_local_execution#platform-specific-options)
 * @param undecorated Disables or enables decorations for this window.
 * @param transparent Disables or enables window transparency. Transparency should be set
 * only if window is undecorated, otherwise an exception will be thrown.
 * @param resizable Can window be resized by the user (application still can resize the window
 * changing [state])
 * @param enabled Can window react to input events
 * @param focusable Can window receive focus
 * @param alwaysOnTop Should window always be on top of another windows
 * @param onPreviewKeyEvent This callback is invoked when the user interacts with the hardware
 * keyboard. It gives ancestors of a focused component the chance to intercept a [KeyEvent].
 * Return true to stop propagation of this event. If you return false, the key event will be
 * sent to this [onPreviewKeyEvent]'s child. If none of the children consume the event,
 * it will be sent back up to the root using the onKeyEvent callback.
 * @param onKeyEvent This callback is invoked when the user interacts with the hardware
 * keyboard. While implementing this callback, return true to stop propagation of this event.
 * If you return false, the key event will be sent to this [onKeyEvent]'s parent.
 * @param content Content of the window
 */
@Composable
fun CustomWindow(
	onCloseRequest: () -> Unit,
	state: WindowState = rememberWindowState(),
	visible: Boolean = true,
	title: String = "Untitled",
	icon: Painter? = null,
	undecorated: Boolean = false,
	transparent: Boolean = false,
	resizable: Boolean = true,
	enabled: Boolean = true,
	focusable: Boolean = true,
	alwaysOnTop: Boolean = false,
	onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
	onKeyEvent: (KeyEvent) -> Boolean = { false },
	content: @Composable FrameWindowScope.() -> Unit
) {
	val currentState by rememberUpdatedState(state)
	val currentTitle by rememberUpdatedState(title)
	val currentIcon by rememberUpdatedState(icon)
	val currentUndecorated by rememberUpdatedState(undecorated)
	val currentTransparent by rememberUpdatedState(transparent)
	val currentResizable by rememberUpdatedState(resizable)
	val currentEnabled by rememberUpdatedState(enabled)
	val currentFocusable by rememberUpdatedState(focusable)
	val currentAlwaysOnTop by rememberUpdatedState(alwaysOnTop)
	val currentOnCloseRequest by rememberUpdatedState(onCloseRequest)
	
	val updater = remember(::ComponentUpdater)
	
	// the state applied to the window. exist to avoid races between WindowState changes and the state stored inside the native window
	val appliedState = remember {
		object {
			var size: DpSize? = null
			var position: WindowPosition? = null
			var placement: WindowPlacement? = null
			var isMinimized: Boolean? = null
		}
	}
	
	Window(
		visible = visible,
		onPreviewKeyEvent = onPreviewKeyEvent,
		onKeyEvent = onKeyEvent,
		create = {
			CustomComposeWindow().apply {
				// close state is controlled by WindowState.isOpen
				defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
				addWindowListener(object : WindowAdapter() {
					override fun windowClosing(e: WindowEvent) {
						currentOnCloseRequest()
					}
				})
				addWindowStateListener {
					currentState.placement = placement
					currentState.isMinimized = isMinimized
					appliedState.placement = currentState.placement
					appliedState.isMinimized = currentState.isMinimized
				}
				addComponentListener(object : ComponentAdapter() {
					override fun componentResized(e: ComponentEvent) {
						// we check placement here and in windowStateChanged,
						// because fullscreen changing doesn't
						// fire windowStateChanged, only componentResized
						currentState.placement = placement
						currentState.size = DpSize(width.dp, height.dp)
						appliedState.placement = currentState.placement
						appliedState.size = currentState.size
					}
					
					override fun componentMoved(e: ComponentEvent) {
						currentState.position = WindowPosition(x.dp, y.dp)
						appliedState.position = currentState.position
					}
				})
			}
		},
		dispose = ComposeWindow::dispose,
		update = { window ->
			updater.update {
				set(currentTitle, window::setTitle)
				set(currentIcon, window::setIcon)
				set(currentUndecorated, window::setUndecoratedSafely)
				set(currentTransparent, window::isTransparent::set)
				set(currentResizable, window::setResizable)
				set(currentEnabled, window::setEnabled)
				set(currentFocusable, window::setFocusable)
				set(currentAlwaysOnTop, window::setAlwaysOnTop)
			}
			if(state.size != appliedState.size) {
				window.setSizeSafely(state.size)
				appliedState.size = state.size
			}
			if(state.position != appliedState.position) {
				window.setPositionSafely(state.position)
				appliedState.position = state.position
			}
			if(state.placement != appliedState.placement) {
				window.placement = state.placement
				appliedState.placement = state.placement
			}
			if(state.isMinimized != appliedState.isMinimized) {
				window.isMinimized = state.isMinimized
				appliedState.isMinimized = state.isMinimized
			}
		},
		content = content
	)
}

/// Other copied verbatim


internal class ComponentUpdater {
	private var updatedValues = mutableListOf<Any?>()
	
	fun update(body: UpdateScope.() -> Unit) {
		UpdateScope().body()
	}
	
	inner class UpdateScope {
		private var index = 0
		
		/**
		 * Compare [value] with the old one and if it is changed - store a new value and call
		 * [update]
		 */
		fun <T : Any?> set(value: T, update: (T) -> Unit) {
			if(index < updatedValues.size) {
				if(updatedValues[index] != value) {
					update(value)
					updatedValues[index] = value
				}
			} else {
				check(index == updatedValues.size)
				update(value)
				updatedValues.add(value)
			}
			
			index++
		}
	}
}


/**
 * Ignore size updating if window is maximized or in fullscreen.
 * Otherwise we will reset maximized / fullscreen state.
 */
internal fun ComposeWindow.setSizeSafely(size: DpSize) {
	if(placement == WindowPlacement.Floating) {
		(this as Window).setSizeSafely(size)
	}
}

/**
 * Ignore position updating if window is maximized or in fullscreen.
 * Otherwise we will reset maximized / fullscreen state.
 */
internal fun ComposeWindow.setPositionSafely(
	position: WindowPosition
) {
	if(placement == WindowPlacement.Floating) {
		(this as Window).setPositionSafely(position)
	}
}

/**
 * Limit the width and the height to a minimum of 0
 */
internal fun Window.setSizeSafely(size: DpSize) {
	val screenBounds by lazy { graphicsConfiguration.bounds }
	
	val isWidthSpecified = size.isSpecified && size.width.isSpecified
	val isHeightSpecified = size.isSpecified && size.height.isSpecified
	
	val width = if(isWidthSpecified) {
		size.width.value.roundToInt().coerceAtLeast(0)
	} else {
		screenBounds.width
	}
	
	val height = if(isHeightSpecified) {
		size.height.value.roundToInt().coerceAtLeast(0)
	} else {
		screenBounds.height
	}
	
	if(!isWidthSpecified || !isHeightSpecified) {
		preferredSize = Dimension(width, height)
		pack()
		// if we set null, getPreferredSize will return the default inner size determined by
		// the inner components (see the description of setPreferredSize)
		preferredSize = null
	}
	
	setSize(
		if(isWidthSpecified) width else preferredSize.width,
		if(isHeightSpecified) height else preferredSize.height,
	)
}

internal fun Window.setPositionSafely(
	position: WindowPosition
) = when(position) {
	WindowPosition.PlatformDefault -> setLocationByPlatformSafely(true)
	is WindowPosition.Aligned -> align(position.alignment)
	is WindowPosition.Absolute -> setLocation(
		position.x.value.roundToInt(),
		position.y.value.roundToInt()
	)
}

internal fun Window.align(alignment: Alignment) {
	val screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration)
	val screenBounds = graphicsConfiguration.bounds
	val size = IntSize(size.width, size.height)
	val screenSize = IntSize(screenBounds.width, screenBounds.height)
	val location = alignment.align(size, screenSize, LayoutDirection.Ltr)
	
	setLocation(
		screenInsets.left + location.x,
		screenInsets.top + location.y
	)
}

/**
 * We cannot call [Frame.setUndecorated] if window is showing - AWT will throw an exception.
 * But we can call [Frame.setUndecoratedSafely] if isUndecorated isn't changed.
 */
internal fun Frame.setUndecoratedSafely(value: Boolean) {
	if(this.isUndecorated != value) {
		this.isUndecorated = value
	}
}

/**
 * We cannot call [Frame.setLocation] if window is showing - AWT will throw an
 * exception.
 * But we can call [Frame.setLocationByPlatform] if isLocationByPlatform isn't changed.
 */
internal fun Window.setLocationByPlatformSafely(isLocationByPlatform: Boolean) {
	if(this.isLocationByPlatform != isLocationByPlatform) {
		this.isLocationByPlatform = isLocationByPlatform
	}
}

// In fact, this size doesn't affect anything on Windows/Linux, and isn't used by macOS (macOS
// doesn't have separate Window icons). We specify it to support Painter's with
// Unspecified intrinsicSize
private val iconSize = Size(32f, 32f)

internal fun Window.setIcon(painter: Painter?) {
	setIconImage(painter?.toAwtImage(density, layoutDirection, iconSize))
}



internal val Component.density: Density get() = graphicsConfiguration.density

private val GraphicsConfiguration.density: Density
	get() = Density(
		defaultTransform.scaleX.toFloat(),
		fontScale = 1f
	)

@Suppress("unused")
internal val Component.layoutDirection: LayoutDirection
	get() = LayoutDirection.Ltr
