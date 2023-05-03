package korlibs.korge.mascots

import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.dragonbones.KorgeDbArmatureDisplay
import korlibs.korge.dragonbones.KorgeDbFactory

object KorgeMascotsAnimations {
    val IDLE = "idle"
    val WALK = "walk"
}

suspend fun KorgeDbFactory.loadKorgeMascots(res: VfsFile = resourcesVfs) {
    loadSkeletonAndAtlas(res["korge-mascots/Koral_ske.dbbin"], res["korge-mascots/Koral_tex.json"])
    loadSkeletonAndAtlas(res["korge-mascots/Gest_ske.dbbin"], res["korge-mascots/Gest_tex.json"])
}

fun KorgeDbFactory.buildArmatureDisplayGest(): KorgeDbArmatureDisplay? = buildArmatureDisplay("Gest")
fun KorgeDbFactory.buildArmatureDisplayKoral(): KorgeDbArmatureDisplay? = buildArmatureDisplay("Koral")