import korlibs.event.Key
import korlibs.image.color.Colors
import korlibs.image.vector.*
import korlibs.image.vector.format.readSVG
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.Korge
import korlibs.korge.animate.animator
import korlibs.korge.animate.block
import korlibs.korge.animate.tween
import korlibs.korge.animate.wait
import korlibs.korge.dragonbones.KorgeDbFactory
import korlibs.korge.input.keys
import korlibs.korge.mascots.KorgeMascotsAnimations
import korlibs.korge.mascots.buildArmatureDisplayGest
import korlibs.korge.mascots.buildArmatureDisplayKoral
import korlibs.korge.mascots.loadKorgeMascots
import korlibs.korge.scene.Scene
import korlibs.korge.scene.sceneContainer
import korlibs.korge.tween.V2Callback
import korlibs.korge.tween.V2Lazy
import korlibs.korge.tween.get
import korlibs.korge.ui.UIView
import korlibs.korge.ui.clicked
import korlibs.korge.ui.uiButton
import korlibs.korge.view.*
import korlibs.korge.view.filter.DropshadowFilter
import korlibs.korge.view.filter.filters
import korlibs.math.geom.Anchor
import korlibs.math.geom.Point
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size
import korlibs.math.interpolation.Easing
import korlibs.time.milliseconds
import korlibs.time.seconds
import kotlin.math.absoluteValue

suspend fun main() = Korge(windowSize = Size(512, 512), backgroundColor = Colors["#2b2b2b"]) {

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
        val SCALE = 0.6
        val factory = KorgeDbFactory()
        val res = resourcesVfs
        factory.loadKorgeMascots(res)

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
        val bubbleAnchor = Anchor(bubbleAnchorPoint.x / bubbleShape.width, bubbleAnchorPoint.y / bubbleShape.height)
        //println("bubbleAnchor=$bubbleAnchor")
        val ninePatch = bubbleShape.toNinePatchFromGuides(guideColor = Colors.FUCHSIA)

        val textContainer = container().xy(300, 300).scale(1.0).alpha(0.0)
        val textBubble = textContainer.ninePatchShapeView(ninePatch, renderer = GraphicsRenderer.SYSTEM).size(10, 10)
            .filters(DropshadowFilter(dropX = 0f, dropY = 0f))
        val text = textContainer.text("hello", color = Colors.RED, textSize = 24f).xy(10, 10)

        val gest = factory.buildArmatureDisplayGest()!!.position(256, 490).scale(SCALE).addTo(this).also {
            it.animation.play(KorgeMascotsAnimations.IDLE)
        }

        val gestHeight = gest.scaledHeight
        val koral = factory.buildArmatureDisplayKoral()!!.position(100, 490).scale(SCALE).addTo(this).also {
            it.animation.play(KorgeMascotsAnimations.IDLE)
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
                gest.scaleX = gest.scaleX.absoluteValue * if (right) +1f else -1f
                gest.x += if (right) +3 else -3
                if (!moving) gest.animation.fadeIn(KorgeMascotsAnimations.WALK, 0.3.seconds)
                moving = true
            } else {
                if (moving) gest.animation.fadeIn(KorgeMascotsAnimations.IDLE, 0.3.seconds)
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
        //println(gest.animation.animationNames)

        val animator = animator()
        //tween(textContainer::scale[1.0], time = 0.1.seconds)
        fun appearText(str: String) {
            //animator.complete()
            animator.tween(
                text::scaleAvg[0.0],
                text::alpha[0.0],
                time = 0.025.seconds,
                easing = Easing.EASE,
            )
            animator.block(name = "setText") {
                //println("OLD: ${text.textBounds} : ${text.getLocalBounds()} : ${text.width}")
                text.text = str
                //println("NEW: ${text.textBounds} : ${text.getLocalBounds()} : ${text.width}")
                text.autoScaling = true
                //text._renderInternal(null)
            }
            if (true) {
            //if (false) {
                animator.sequenceLazy {
                //animator.sequence {
                    parallel {
                        //tween(text::pos.incr(Point(10, 10)), time = 0.5.seconds)
                        //tween(text::pos.incr(0, 30), time = 0.5.seconds)
                        //tween(text::rotation.incr(30.degrees), time = 0.5.seconds)
                        //rotateBy(text, 30.degrees, time = 0.seconds)
                        //moveInPathWithSpeed(text, buildVectorPath {
                        //    circle(0, 0, 50)
                        //})
                        //tween(text::rotation.incr(30.degrees), time = 0.seconds)
                        //tween(textContainer::scale.incr(+0.1), time = 0.5.seconds)
                        //textContainer.rotateBy(15.degrees)
                        tween(
                            textContainer::scaleAvg[1.0],
                            textContainer::alpha[1.0],
                            text::scaleAvg[0.0, 1.0],
                            text::alpha[1.0],
                            textBubble::width[text.width + 32],
                            textBubble::height[text.height + 32],
                            V2Callback { updateTextContainerPos() },
                            time = 0.5.seconds,
                            easing = Easing.EASE,
                            name = "appearText"
                        )
                    }
                    block {
                        println("DONE APPEAR TEXT!")
                    }
                }
            } else {
                animator.tween(
                    textContainer::scaleAvg[1.0],
                    textContainer::alpha[1.0],
                    text::scaleAvg[0.0, 1.0],
                    text::alpha[1.0],
                    V2Lazy { textBubble::width[text.width + 32] },
                    V2Lazy { textBubble::height[text.height + 32] },
                    V2Callback { updateTextContainerPos() },
                    time = 0.5.seconds,
                    easing = Easing.EASE,
                    name = "appearText"
                )
            }
        }
        fun disappearText(fast: Boolean = false) {
            animator.tween(
                textContainer::scaleAvg[0.01],
                textContainer::alpha[0.01],
                textBubble::width[0.01],
                textBubble::height[0.01],
                text::scaleAvg[0.01],
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
            animator.block { println("APPEAR TEXT DONE") }
            animator.wait(2.seconds)
            animator.block { println("WAIT DONE") }
            disappearText()
            animator.block { println("DISAPPEAR TEXT DONE") }
        }

        var nn = 0
        fun randomTalk() {
            val texts = listOf(
                "HELLO WORLD 2!",
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

        talk("Cool 2!\nThe new animator\nworks just great!\nMove me with <- and -> arrows,\nand press RETURN to talk")
        //animator.awaitComplete()

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
) : UIView(shape.size), Anchorable {
    private val graphics = graphics(shape.shape, renderer = renderer)
    var boundsIncludeStrokes: Boolean by graphics::boundsIncludeStrokes
    var antialiased: Boolean by graphics::antialiased
    var smoothing: Boolean by graphics::smoothing
    var autoScaling: Boolean by graphics::autoScaling
    override var anchor: Anchor by graphics::anchor
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

    override fun getLocalBoundsInternal(): Rectangle {
        return graphics.getLocalBoundsInternal()
    }
}
