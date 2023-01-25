pluginManagement { repositories {  mavenLocal(); mavenCentral(); google(); gradlePluginPortal()  }  }

plugins {
    //id("com.soywiz.kproject.settings") version "0.0.1-SNAPSHOT"
    id("com.soywiz.kproject.settings") version "0.0.6" apply false
}

kproject("./deps")

//include(":deps")
//include(":korge-dragonbones")
//project(":korge-dragonbones").projectDir = file("modules/korge-dragonbones")
//include(":korge-jitto")
//project(":korge-jitto").projectDir = file("modules/korge-jitto")
