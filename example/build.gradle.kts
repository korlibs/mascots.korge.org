import com.soywiz.korge.gradle.*

plugins {
    id("com.soywiz.korge") version "4.0.0-alpha-2"
}

korge {
	id = "org.korge.samples.mascots"

	targetJvm()
	targetJs()
	targetDesktop()
	targetIos()
	targetAndroidDirect()
	serializationJson()
}

dependencies {
    add("commonMainApi", project(":deps"))
}

