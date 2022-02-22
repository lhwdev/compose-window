package com.lhwdev.build

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget


// all common

fun KotlinProjectExtension.setupCommon() {
	sourceSets {
		all {
			languageSettings.apply {
				enableLanguageFeature("InlineClasses")
				optIn("kotlin.RequiresOptIn")
				optIn("kotlin.ExperimentalUnsignedTypes")
			}
		}
		
		val testSourceSet = if(this@setupCommon is KotlinMultiplatformExtension) "commonTest" else "test"
		
		named(testSourceSet) {
			dependencies {
				implementation(kotlin("test-common"))
				implementation(kotlin("test-annotations-common"))
			}
		}
	}
}


// jvm

private fun KotlinProjectExtension.setupJvmCommon(name: String?) {
	sourceSets {
		named(sourceSetNameFor(name, "test")) {
			dependencies {
				implementation(kotlin("test-junit"))
			}
		}
	}
}

fun KotlinJvmProjectExtension.setup(init: (KotlinSetup<KotlinWithJavaTarget<KotlinJvmOptions>>.() -> Unit)? = null) {
	setupCommon()
	setupJvmCommon(null)
	
	target.compilations.all {
		kotlinOptions.jvmTarget = "1.8"
	}
	
	init?.invoke(KotlinSetup(target, null, sourceSets))
}


// mpp

fun KotlinMultiplatformExtension.dependencies(name: String = "commonMain", block: KotlinDependencyHandler.() -> Unit) {
	sourceSets {
		named(name) {
			dependencies(block)
		}
	}
}

fun KotlinMultiplatformExtension.library() {
	setupCommon()
	setupJvm()
	// js {
	// 	browser()
	// 	nodejs()
	// }
	
	// val hostOs = System.getProperty("os.name")
	// val isMingwX64 = hostOs.startsWith("Windows")
	// when {
	// 	hostOs == "Mac OS X" -> macosX64("native")
	// 	hostOs == "Linux" -> linuxX64("native")
	// 	isMingwX64 -> mingwX64("native")
	// 	else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
	// }
}

fun KotlinMultiplatformExtension.setupJvm(
	name: String = "jvm",
	init: (KotlinSetup<KotlinJvmTarget>.() -> Unit)? = null
): KotlinSetup<KotlinJvmTarget> {
	val target = jvm(name) {
		compilations.all {
			kotlinOptions.jvmTarget = "1.8"
		}
	}
	
	setupJvmCommon(name)
	return KotlinSetup(target, name, sourceSets).also { init?.invoke(it) }
}

fun KotlinMultiplatformExtension.setupJs(
	name: String = "js",
	init: (KotlinSetup<KotlinJsTargetDsl>.() -> Unit)? = null
): KotlinSetup<KotlinJsTargetDsl> {
	val target = js(name)
	
	return KotlinSetup(target, name, sourceSets).apply {
		testDependencies {
			
			implementation(kotlin("test-js"))
		}
		init?.invoke(this)
	}
}

// fun KotlinMultiplatformExtension.setupAndroid(
// 	project: Project,
// 	name: String = "android",
// 	init: (KotlinSetup<KotlinAndroidTarget>.() -> Unit)? = null
// ): KotlinSetup<KotlinAndroidTarget> {
// 	val target = android(name) {
// 		compilations.all {
// 			kotlinOptions.jvmTarget = "1.8"
// 		}
// 	}
//
// 	setupJvmCommon(name)
//	val setup = KotlinSetup(target, name, sourceSets)
// 	init?.invoke(setup)
//
// 	project.extensions.getByType<BaseExtension>().sourceSets.all {
// 		val directory = "src/$name${this.name.firstToUpperCase()}"
// 		setRoot(directory)
// 	}
//
// 	return setup
// }


private fun String.firstToUpperCase() = replaceRange(0, 1, first().toUpperCase().toString())

private fun sourceSetNameFor(name: String?, type: String) =
	if(name == null) type else "$name${type.firstToUpperCase()}"


class KotlinSetup<Target : KotlinTarget>(
	val target: Target,
	val name: String?,
	val sourceSet: NamedDomainObjectContainer<KotlinSourceSet>
) {
	fun target(block: Target.() -> Unit) {
		target.block()
	}
	
	fun dependsOn(other: KotlinSetup<*>) {
		// not lazy intentionally: 'Project#afterEvaluate(Action) cannot be executed in the current context.'
		mainSourceSet.get().dependsOn(other.mainSourceSet.get())
	}
	
	fun sourceSet(type: String): NamedDomainObjectProvider<KotlinSourceSet> =
		sourceSet.named(sourceSetNameFor(name, type))
	
	val mainSourceSet get() = sourceSet("main")
	val testSourceSet get() = sourceSet("test")
	
	private fun dependencies(type: String, block: KotlinDependencyHandler.() -> Unit) {
		(sourceSet(type)) {
			dependencies(block)
		}
	}
	
	fun dependencies(block: KotlinDependencyHandler.() -> Unit) {
		dependencies("main", block)
	}
	
	fun testDependencies(block: KotlinDependencyHandler.() -> Unit) {
		dependencies("test", block)
	}
}
