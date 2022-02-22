plugins {
	alias(libs.plugins.kotlinMultiplatform) apply false
	alias(libs.plugins.kotlinJvm) apply false
	alias(libs.plugins.kotlinSerialization) apply false
	
	alias(libs.plugins.compose) apply false
}

allprojects {
	repositories {
		google()
		mavenCentral()
		
		// compose dev
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
	}
}
