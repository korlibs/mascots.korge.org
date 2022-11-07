import com.soywiz.klock.*
import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.dragonbones.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.ui.*
import com.soywiz.korge.view.*
import com.soywiz.korge.view.filter.*
import com.soywiz.korim.color.*
import com.soywiz.korim.vector.*
import com.soywiz.korim.vector.format.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*

suspend fun main() = Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
    val sceneContainer = sceneContainer()

    sceneContainer.changeTo({ MyScene() })
}

fun Shape.mapShape(map: (Shape) -> Shape): Shape {
    val result = map(this)
    return if (result is CompoundShape) {
        CompoundShape(
            result.components
                .map { it.mapShape(map) }
                .filter { it !is EmptyShape }
        )
    } else {
        result
    }
}

fun Shape.filterShape(filter: (Shape) -> Boolean): Shape = when {
    filter(this) -> {
        when (this) {
            is CompoundShape -> {
                CompoundShape(
                    this.components
                        .map { it.filterShape(filter) }
                        .filter { it !is EmptyShape }
                )
            }

            else -> this
        }
    }
    else -> EmptyShape
}

class MyScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        //Dragon
        //JittoViewExample.runInContainer(this)
        val SCALE = 0.3
        val factory = KorgeDbFactory()
        val res = resourcesVfs
        factory.loadSkeletonAndAtlas(res["Koral_ske.dbbin"], res["Koral_tex.json"])
        factory.loadSkeletonAndAtlas(res["Gest_ske.dbbin"], res["Gest_tex.json"])

        //val armatureDisplay = factory.buildArmatureDisplay("Koral")!!
        var bubbleAnchorPoint = Point()
        val bubbleShape = resourcesVfs["chat-bubble.svg"].readSVG().toShape()
            .filterShape {
                if (it is FillShape && it.paint == Colors.RED) {
                    bubbleAnchorPoint = it.path.getBounds().center
                    false
                } else {
                    true
                }
            }
        var bubbleAnchor = Anchor(bubbleAnchorPoint.x / bubbleShape.width, bubbleAnchorPoint.y / bubbleShape.height)
        //println("bubbleAnchor=$bubbleAnchor")
        val ninePatch = bubbleShape.toNinePatchFromGuides(guideColor = Colors.FUCHSIA)

        val textContainer = container().xy(300, 300).scale(1.0).alpha(0.0)
        val textBubble = textContainer.ninePatchShapeView(ninePatch).size(10, 10)
            .filters(DropshadowFilter(dropX = 0.0, dropY = 0.0))
        val text = textContainer.text("hello", color = Colors.RED, textSize = 24.0).xy(10, 10)

        val gest = factory.buildArmatureDisplay("Gest")!!.position(256, 490).scale(SCALE).addTo(this).also {
            it.animation.play("idle")
        }
        val gestHeight = gest.scaledHeight
        val koral = factory.buildArmatureDisplay("Koral")!!.position(100, 490).scale(SCALE).addTo(this).also {
            it.animation.play("idle")
        }

        var moving = false
        fun updateTextContainerPos() {
            //textContainer.x = gest.x - textBubble.width
            textContainer.x = gest.x - textBubble.width * bubbleAnchor.sx
            textContainer.y = gest.y - gestHeight - (textBubble.height * bubbleAnchor.sy)
            //textContainer.y = gest.y - 340.0 - (textBubble.height)
        }
        fun updated(right: Boolean, up: Boolean) {
            if (!up) {
                gest.scaleX = if (right) +0.3 else -0.3
                gest.x += if (right) +3 else -3
                if (!moving) gest.animation.fadeIn("walk", 0.3.seconds)
                moving = true
            } else {
                if (moving) gest.animation.fadeIn("idle", 0.3.seconds)
                moving = false
            }
            updateTextContainerPos()
        }
        keys {
            downFrame(Key.LEFT, dt = 16.milliseconds) { updated(right = false, up = false) }
            downFrame(Key.RIGHT, dt = 16.milliseconds) { updated(right = true, up = false) }
            up(Key.LEFT) { updated(right = false, up = true) }
            up(Key.RIGHT) { updated(right = false, up = true) }
        }
        updated(right = true, up = true)
        //armatureDisplay.animation.play("walk")
        //println(gest.animation.animationNames)

        val animator = newAnimator()
        //tween(textContainer::scale[1.0], time = 0.1.seconds)
        fun appearText(str: String) {
            animator.tween(
                //text::scale[0.0],
                text::alpha[0.0],
                time = 0.1.seconds,
                easing = Easing.EASE,
            )
            animator.block(name = "setText") {
                //println("OLD: ${text.textBounds} : ${text.getLocalBounds()} : ${text.width}")
                text.text = str
                //println("NEW: ${text.textBounds} : ${text.getLocalBounds()} : ${text.width}")
                text.autoScaling = true
                //text._renderInternal(null)
            }
            animator.tween(
                textContainer::scale[1.0],
                textContainer::alpha[1.0],
                text::scale[0.0, 1.0],
                text::alpha[1.0],
                V2Lazy { textBubble::width[text.width + 32] },
                V2Lazy { textBubble::height[text.height + 32] },
                V2Callback { updateTextContainerPos() },
                time = 0.5.seconds,
                easing = Easing.EASE,
                name = "appearText"
            )
        }
        fun disappearText(fast: Boolean = false) {
            animator.tween(
                textContainer::scale[0.01],
                textContainer::alpha[0.01],
                textBubble::width[0.01],
                textBubble::height[0.01],
                text::scale[0.01],
                V2Callback { updateTextContainerPos() },
                time = if (fast) 0.05.seconds else 0.2.seconds,
                easing = Easing.EASE,
                name = "disappearText"
            )

        }
        //disappearText()
        //appearText("HEY THERE!")

        fun talk(str: String) {
            //animator.complete()
            animator.cancel()
            //disappearText(fast = true)
            appearText(str)
            animator.wait(2.seconds)
            disappearText()
        }

        var nn = 0
        fun randomTalk() {
            val texts = listOf(
                "HELLO WORLD!",
                "HELLO WORLD!\nTHIS IS A TEST!",
                "AWESOME!",
                "THIS IS PURE\nAWESOMINESS",
                "LET'S TALK\nA BIT!",
                "OH!\nLOL!\nPURE\nAWESOME\nTEST",
            )
            talk(texts[nn])
            nn = (nn + 1) % texts.size
        }

        uiButton("Talk").clicked {
            randomTalk()
        }

        keys {
            down(Key.RETURN) {
                randomTalk()
            }
        }

        talk("WAY!")
        animator.awaitComplete()

        println("COMPLETED!")

        //armatureDisplay.animation.play("jump")
        //armatureDisplay.animation.play("idle")
        //scaleView(512, 512) {
    }
}

inline fun Container.ninePatchShapeView(
    shape: NinePatchShape,
    renderer: GraphicsRenderer = GraphicsRenderer.SYSTEM,
    callback: @ViewDslMarker NinePatchShapeView.() -> Unit = {}
): NinePatchShapeView = NinePatchShapeView(shape, renderer).addTo(this, callback)

class NinePatchShapeView(
    shape: NinePatchShape,
    renderer: GraphicsRenderer,
) : UIView(shape.size.width, shape.size.height), Anchorable {
    private val graphics = graphics(shape.shape, renderer = renderer)
    var boundsIncludeStrokes: Boolean by graphics::boundsIncludeStrokes
    var antialiased: Boolean by graphics::antialiased
    var smoothing: Boolean by graphics::smoothing
    var autoScaling: Boolean by graphics::autoScaling
    override var anchorX: Double by graphics::anchorX
    override var anchorY: Double by graphics::anchorY
    var renderer: GraphicsRenderer by graphics::renderer

    var shape: NinePatchShape = shape
        set(value) {
            if (field == value) return
            field = value
            onSizeChanged()
        }

    override fun onSizeChanged() {
        graphics.shape = shape.transform(Size(width, height))
    }

    override fun getLocalBoundsInternal(out: Rectangle) {
        graphics.getLocalBoundsInternal(out)
    }
}

private object V2CallbackSupport {
    var dummy: Unit = Unit
}

fun V2Callback(callback: (Double) -> Unit): V2<Unit> = V2(V2CallbackSupport::dummy, Unit, Unit, { ratio, _, _ -> callback(ratio) }, true)
fun <T> V2CallbackT(callback: (Double) -> Unit): V2<T> = V2Callback { callback(it) } as V2<T>

fun <T> V2Lazy(callback: () -> V2<T>): V2<T> {
    var value: V2<T>? = null
    return V2CallbackT {
        if (value == null) value = callback()
        value!!.set(it)
    }
}
