import korlibs.korge.gradle.*

plugins {
	alias(libs.plugins.korge)
}

korge {
	id = "org.korge.samples.mascots"

	targetJvm()
	targetJs()
	targetIos()
	targetAndroid()

	serializationJson()
}

dependencies {
    add("commonMainApi", project(":deps"))
}

