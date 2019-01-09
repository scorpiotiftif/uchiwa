package fr.francoisgaucher.uchiwae

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import java.util.*


class PieUchiwa(val index: Int, private val numbersPies: Int) {

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var paintSelected: Paint? = null

    val rect = RectF()
    val newRect = RectF()

    var startAngle: Float = 0f
    var sweetAngle: Float = 0f

    var startAngleScale: Float = 0f
    var sweetAngleScale: Float = 0f

    var endAngle: Float = 0f
    var id: String = ""


    init {
        id = UUID.randomUUID().toString() + " / INDEX = $index"
        initValues()
    }

    private fun initValues() {
        startAngle =
                if ((Uchiwa.START_DEGREE + ((Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies) * index)) > Uchiwa.MAX_DEGREE) {
                    ((Uchiwa.START_DEGREE + ((Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies) * index)) % Uchiwa.MAX_DEGREE)
                } else {
                    (Uchiwa.START_DEGREE + ((Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies) * index))
                }
        startAngleScale = startAngle - VALUE_TO_ADD
        if (startAngleScale == 360f) {
            startAngleScale = 0f
        }
        if (startAngle == 360f) {
            startAngle = 0f
        }
        sweetAngle = Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies
        sweetAngleScale = sweetAngle + VALUE_TO_ADD

        endAngle = if ((startAngle + sweetAngle) > Uchiwa.MAX_DEGREE) {
            (startAngle + sweetAngle) % Uchiwa.MAX_DEGREE
        } else {
            (startAngle + sweetAngle)
        }

        paint.color = Color.RED

        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        paint.alpha = INIT_ALPHA

    }

    fun closing() {

    }

    fun opening() {

    }

    fun selected() {
        paintSelected = Paint(Paint.ANTI_ALIAS_FLAG)
        paintSelected?.apply {
            strokeWidth = 2f
            color = Color.WHITE
            style = Paint.Style.STROKE
        }
        paint.alpha = 255
    }

    fun unselected() {
        paintSelected = null
        paint.alpha = INIT_ALPHA
    }

    fun updateMeasure(rect: RectF, newRect: RectF) {
        this.rect.set(rect)
        this.newRect.set(newRect)
    }

    companion object {
        private const val INIT_ALPHA = 190
        private const val VALUE_TO_ADD = 2
    }
}