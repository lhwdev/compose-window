import com.lhwdev.build.*


plugins {
	kotlin("multiplatform")
	id("org.jetbrains.compose")
	
	id("common-plugin")
}

kotlin {
	setupCommon()
	setupJvm("desktop")
	
	dependencies {
		implementation(compose.desktop.currentOs)
	}
}

