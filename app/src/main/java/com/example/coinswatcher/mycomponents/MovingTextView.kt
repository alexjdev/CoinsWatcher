package com.example.coinswatcher.mycomponents

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import androidx.annotation.Nullable
import androidx.core.graphics.toColor
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.concurrent.timerTask

class MovingTextView : View {

    private var paint =  Paint()

    private val textSize: Float = 32f

    private var offsetXForDrawText: Float = 0f

    private val offsetYForDrawText: Float = 52f

    private var fullBuildTextRowSize: Float = 0f

    private var widthMyDeviceScreenSize: Float = 0f

    private var currentX: Float = 0f

    private var currentY: Float = 0f

    private var paramList = ArrayList<OneParam>()

    //    private var separatorStr: String = " * "
    private var separatorStr: String = "\u0020 * \u0020"

    private var orientMove: MoveOrientation = MoveOrientation.MoveToLeft //MoveOrientation.MoveToRight

    enum class MoveOrientation { MoveToRight, MoveToLeft }

    val bounds = Rect()

    var str: String = ""

    private var isInitializedMyScreenWidth: Boolean = false

    private val TIMER_TASK_DELAY: Long = 20L

    private val TIMER_TASK_PERIOD: Long = 25L

    private val ROW_SPEED = 5

    enum class ColorMode { Light, Dark }

    private var colorMode = ColorMode.Light


    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }


    private fun init(@Nullable attrs: AttributeSet?) {

        setupColorScheme()

        paramList.add(0, OneParam("My own component: ", Color.BLUE.toColor(), "COLORED RUNNING STRING", Color.RED.toColor()))
        paramList.add(1, OneParam("Empty param: ", Color.GREEN.toColor(), "", Color.RED.toColor()))
        paramList.add(2, OneParam("Hello ", Color.BLUE.toColor(), "World", Color.GREEN.toColor()))
        paramList.add(3, OneParam("Green color is: ", Color.GREEN.toColor(), "GREEN", Color.GREEN.toColor()))
        paramList.add(4, OneParam("To do: ", Color.BLUE.toColor(), "Add setup (add/remove) options for MovingTextView component", Color.RED.toColor()))
        paramList.add(5, OneParam("Today is: ", Color.BLUE.toColor(), "Very nice day !!!", Color.BLUE.toColor()))

        val paintTmp =  Paint()
        paintTmp.textSize = textSize
        val boundsTmp = Rect()

        fullBuildTextRowSize = 0f
        paramList.forEach {

            paint.getTextBounds(it.name, 0, it.name.length, boundsTmp)
            fullBuildTextRowSize += boundsTmp.width()
            paint.getTextBounds(it.value, 0, it.value.length, boundsTmp)
            fullBuildTextRowSize += boundsTmp.width()
            paint.getTextBounds(separatorStr, 0, separatorStr.length, boundsTmp)
            fullBuildTextRowSize += boundsTmp.width()
        }
        

        Timer().scheduleAtFixedRate(
            timerTask() {

                when {
                    (orientMove == MoveOrientation.MoveToLeft) -> moveRowToLeft()

                    (orientMove == MoveOrientation.MoveToRight) -> moveRowToRight()
                }

            }, TIMER_TASK_DELAY,
            TIMER_TASK_PERIOD)


    }

    private fun setupColorScheme() {
        //TODO read preferences and set color
        //colorMode = ColorMode.Light ?
        // val
    }

    private fun moveRowToLeft() {
        //println("moveRowToLeft currentX= $currentX  widthMyDeviceScreenSize= $widthMyDeviceScreenSize")
        currentX -= ROW_SPEED
        if(currentX + fullBuildTextRowSize - widthMyDeviceScreenSize/2 < 0) {
            currentX = 0f
        }

        postInvalidate()
    }

    private fun moveRowToRight() {
        //println("moveRowToRight currentX= $currentX  widthMyDeviceScreenSize= $widthMyDeviceScreenSize")
        currentX += ROW_SPEED
        if(currentX > widthMyDeviceScreenSize) {
            currentX = 0 - fullBuildTextRowSize - widthMyDeviceScreenSize/2
        }

        postInvalidate()
    }

    override fun onDraw(canvas: Canvas?) {

        //println("onDraw canvas width= ${canvas?.width}")

        //KOSTYL
        if(!isInitializedMyScreenWidth) {
            widthMyDeviceScreenSize = canvas?.width!!.toFloat()
            //isInitializedMyScreenWidth = true
        }

        paint.textSize = textSize
        offsetXForDrawText = currentX + widthMyDeviceScreenSize //0f
        paramList.forEach {

            str = it.name
            paint.setColor(it.nameColor.toArgb())
            canvas?.drawText(str, offsetXForDrawText, offsetYForDrawText, paint)
            paint.getTextBounds(str, 0, str.length, bounds)
            offsetXForDrawText += bounds.width()


            str = it.value
            paint.setColor(it.valueColor.toArgb())
            canvas?.drawText(str, offsetXForDrawText, offsetYForDrawText, paint)
            paint.getTextBounds(str, 0, str.length, bounds)
            offsetXForDrawText += bounds.width()

            str = separatorStr
            paint.setColor(Color.LTGRAY)
            canvas?.drawText(str, offsetXForDrawText, offsetYForDrawText, paint)
            paint.getTextBounds(str, 0, str.length, bounds)
            offsetXForDrawText += bounds.width() * 3

        }

        //KOSTYL
        if(!isInitializedMyScreenWidth) {
            println("KOSTYL:ROW SIZE: $fullBuildTextRowSize")
            //paint.getTextBounds(str, 0, str.length, bounds)
            fullBuildTextRowSize += offsetXForDrawText // + bounds.width() * 3
            isInitializedMyScreenWidth = true
            println("KOSTYL:ROW SIZE: $fullBuildTextRowSize   offsetXForDrawText: $offsetXForDrawText") // KOSTYL:ROW SIZE: 4060.0
            println("KOSTYL:separatorStr.length: ${separatorStr.length}") // KOSTYL:separatorStr.length: 5
        }
    }

}