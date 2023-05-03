import korlibs.korge.gradle.*

plugins {
    id("com.soywiz.korge") version "4.0.0-rc4"
	//id("com.soywiz.korge") version "999.0.0.999"
}

korge {
	id = "org.korge.samples.mascots"

	targetJvm()
	targetJs()
	targetDesktop()
	targetIos()
	targetAndroid()

	serializationJson()
}

dependencies {
    add("commonMainApi", project(":deps"))
}

