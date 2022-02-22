import com.lhwdev.build.*


plugins {
	kotlin("jvm")
	id("org.jetbrains.compose")
	
	id("common-plugin")
}


kotlin {
	setupCommon()
}

dependencies {
	implementation(projects.platform.common)
	implementation(compose.desktop.currentOs)
	
	implementation("net.java.dev.jna:jna:5.10.0")
	implementation("net.java.dev.jna:jna-platform:5.10.0")
}
