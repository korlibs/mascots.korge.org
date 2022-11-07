import com.soywiz.korge.gradle.*

plugins {
    id("com.soywiz.korge") version "3.3.1"
}

korge {
	id = "com.sample.demo"

// To enable all targets at once

	//targetAll()

// To enable targets based on properties/environment variables
	//targetDefault()

// To selectively enable targets

	targetJvm()
	targetJs()
	targetDesktop()
	//targetIos()
	//targetAndroidIndirect() // targetAndroidDirect()

	serializationJson()
	//targetAndroidDirect()
}


dependencies {
    add("commonMainApi", project(":deps"))
    //add("commonMainApi", project(":korge-dragonbones"))
}

