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
		implementation(projects.platform.common)
		
		// platforms
		implementation(projects.platform.windows)
		
		implementation(compose.desktop.currentOs)
	}
}

