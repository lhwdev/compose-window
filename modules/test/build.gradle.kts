import com.lhwdev.build.*


plugins {
	kotlin("jvm")
	id("org.jetbrains.compose")
	
	id("common-plugin")
}

kotlin {
	setupCommon()
	
	dependencies {
		implementation(projects.composeWindow)
		implementation(compose.desktop.currentOs)
	}
}

