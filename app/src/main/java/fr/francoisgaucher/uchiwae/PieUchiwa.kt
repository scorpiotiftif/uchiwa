package fr.francoisgaucher.uchiwae

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import java.util.*


class PieUchiwa(val index: Int, private val numbersPies: Int, var icone: Bitmap? = null) : Parcelable {

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val paintIcone = Paint(Paint.ANTI_ALIAS_FLAG)
    val paintAnimation = Paint(Paint.ANTI_ALIAS_FLAG)
    var paintSelected: Paint? = null


    val rect = RectF()
    val newRect = RectF()
    var centerRect = RectF()

    var startAngleINIT: Float = 0f
    var sweetAngleINIT: Float = 0f
    var endAngleINIT: Float = 0f

    var startAngle: Float = 0f
        set(value) {
            if (value > Uchiwa.MAX_DEGREE) {
                field = value % Uchiwa.MAX_DEGREE
            } else {
                field = value
            }
            if (field == 360f) {
                field = 0f
            }
            startAngleScale = field - (VALUE_TO_ADD / 2)
            if (startAngleScale < 0) {
                startAngleScale = Uchiwa.MAX_DEGREE + startAngleScale
            }
            if (startAngleScale == 360f) {
                startAngleScale = 0f
            }

            updateEndAngle()
        }

    var sweetAngle: Float = 0f
        set(value) {
            field = value
            sweetAngleScale = field + VALUE_TO_ADD
            if (sweetAngleScale > Uchiwa.MAX_DEGREE) {
                sweetAngleScale %= Uchiwa.MAX_DEGREE
            }
            updateEndAngle()
        }

    var startAngleScale: Float = 0f
    var sweetAngleScale: Float = 0f

    var endAngle: Float = 0f
    var distanceBetweenStartAndEndAngle: Float = 0f
    var middleAngle: Float = 0f
        set(value) {
            field = if (value >= Uchiwa.MAX_DEGREE) {
                value % Uchiwa.MAX_DEGREE
            } else {
                value
            }
        }
    var id: String = ""
    var padding = 0f

    private var currentState = PieUchiwaEnum.OPPENED

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable(Bitmap::class.java.classLoader)
    ) {
        centerRect = parcel.readParcelable(RectF::class.java.classLoader)
        startAngle = parcel.readFloat()
        sweetAngle = parcel.readFloat()
        startAngleINIT = parcel.readFloat()
        sweetAngleINIT = parcel.readFloat()
        endAngleINIT = parcel.readFloat()
        startAngleScale = parcel.readFloat()
        sweetAngleScale = parcel.readFloat()
        endAngle = parcel.readFloat()
        distanceBetweenStartAndEndAngle = parcel.readFloat()
        middleAngle = parcel.readFloat()
        id = parcel.readString()
        padding = parcel.readFloat()
    }

    init {
        id = UUID.randomUUID().toString() + " / INDEX = $index"
        initValues()
        startAngleINIT = startAngle
        sweetAngleINIT = sweetAngle
        endAngleINIT = endAngle

        paint.color = Color.rgb(160 + (10 * index), 10, 10)
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        paint.alpha = INIT_ALPHA

        paintAnimation.apply {
            strokeWidth = 2f
            color = Color.WHITE
            style = Paint.Style.STROKE
        }

        paintIcone.apply {
            alpha = 255
        }
    }

    private fun initValues() {
        startAngle =
                if ((Uchiwa.START_DEGREE + ((Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies) * index) + padding) > Uchiwa.MAX_DEGREE) {
                    ((Uchiwa.START_DEGREE + ((Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies) * index) + padding) % Uchiwa.MAX_DEGREE)
                } else {
                    (Uchiwa.START_DEGREE + ((Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies) * index) + padding)
                }


        sweetAngle = Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies - padding

        updateEndAngle()
    }

    private fun updateEndAngle() {
        endAngle = if ((startAngle + sweetAngle) > Uchiwa.MAX_DEGREE) {
            (startAngle + sweetAngle) % Uchiwa.MAX_DEGREE
        } else {
            (startAngle + sweetAngle)
        }
        if (icone != null) {
            calculateDistanceBetweenStartAndEndAngle()
            calculateMiddleAngle()
        }
    }

    private fun scaleDownAndVisibilityIcone() {
        if ((paintIcone.alpha - (VALUE_TO_ADD * (sweetAngleINIT / VALUE_TO_ADD).toInt())) >= 0) {
            paintIcone.alpha -= (VALUE_TO_ADD * (sweetAngleINIT / VALUE_TO_ADD).toInt())
        } else {
            paintIcone.alpha = 0
        }
    }

    private fun scaleUpAndVisibilityIcone() {
        if ((paintIcone.alpha + (VALUE_TO_ADD * (sweetAngleINIT / VALUE_TO_ADD).toInt())) <= 255) {
            paintIcone.alpha += (VALUE_TO_ADD * (sweetAngleINIT / VALUE_TO_ADD).toInt())
        } else {
            paintIcone.alpha = 255
        }
    }

    private fun calculateMiddleAngle() {
        if (startAngle >= Uchiwa.TOP_DEGREE &&
            startAngle <= 360 &&
            endAngle <= Uchiwa.BOTTOM_DEGREE &&
            endAngle >= 0
        ) {
            val newEndAngle = Uchiwa.MAX_DEGREE + endAngle
            middleAngle = (startAngle + ((newEndAngle - startAngle).div(2)))
        } else {
            middleAngle = (startAngle + ((endAngle - startAngle).div(2)))
        }
        if (middleAngle > Uchiwa.ALF_DEGREE) {
            var newAngleConverted = middleAngle % Uchiwa.ALF_DEGREE
            middleAngle = (Uchiwa.ALF_DEGREE - newAngleConverted) * -1
        }
        if (rect.width() > 0) {
            calculateCenterPie()
        }
    }

    private fun calculateCenterPie() {
        val radMiddleAngle = Math.toRadians(middleAngle.toDouble())
        val xCenter = rect.centerX() + rect.width().div(3) * Math.cos(radMiddleAngle)
        val yCenter = rect.centerY() + rect.width().div(3) * Math.sin(radMiddleAngle)

        centerRect.set(
            (xCenter).toFloat(),
            (yCenter).toFloat(),
            (xCenter).toFloat(),
            (yCenter).toFloat()
        )
    }

    private fun calculateDistanceBetweenStartAndEndAngle() {
        val radStartAngle = Math.toRadians(startAngle.toDouble())
        val xPointStart = rect.width().div(2) * Math.cos(radStartAngle)
        val yPointStart = rect.height().div(2) * Math.sin(radStartAngle)

        val radEndAngle = Math.toRadians(endAngle.toDouble())
        val xPointEnd = rect.width().div(2) * Math.cos(radEndAngle)
        val yPointEnd = rect.height().div(2) * Math.sin(radEndAngle)

        distanceBetweenStartAndEndAngle =
                Math.sqrt(Math.pow((xPointStart - xPointEnd), 2.0) + Math.pow((yPointStart - yPointEnd), 2.0)).toFloat()
    }

    fun movingUp(endAngle: Float? = null) {
        if (endAngle != null) {
            startAngle = endAngle
        } else {
            startAngle -= VALUE_TO_ADD
            if (startAngle < Uchiwa.TOP_DEGREE) {
                startAngle = Uchiwa.TOP_DEGREE
            }
        }
    }

    fun movingDown(endAngle: Float? = null) {
        if (endAngle != null) {
            startAngle = endAngle + padding
        } else {
            startAngle += VALUE_TO_ADD

            if (startAngle > Uchiwa.START_DEGREE + padding) {
                startAngle = Uchiwa.START_DEGREE + padding
            }
        }

    }

    fun isClosed(): Boolean {
        return currentState == PieUchiwaEnum.CLOSED
    }

    fun isOpenned(): Boolean {
        return currentState == PieUchiwaEnum.OPPENED
    }

    fun closing() {
        currentState = PieUchiwaEnum.CLOSING
        sweetAngle -= VALUE_TO_ADD
        scaleDownAndVisibilityIcone()
        if (sweetAngle <= 0f) {
            sweetAngle = 0f
            currentState = PieUchiwaEnum.CLOSED
        }
    }

    fun opening() {
        currentState = PieUchiwaEnum.OPPENING
        sweetAngle += VALUE_TO_ADD
        scaleUpAndVisibilityIcone()
        if (sweetAngle >= (Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies) - padding) {
            sweetAngle = (Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies) - padding
            currentState = PieUchiwaEnum.OPPENED
        }
    }

    fun selecting() {
        currentState = PieUchiwaEnum.OPPENED
        paintSelected = Paint(Paint.ANTI_ALIAS_FLAG)
        paintSelected?.apply {
            strokeWidth = 2f
            color = Color.WHITE
            style = Paint.Style.STROKE
        }
        paint.alpha = 255
    }

    fun unselected() {
        currentState = PieUchiwaEnum.OPPENED
        paintSelected = null
        paint.alpha = INIT_ALPHA
    }

    fun updateMeasure(rect: RectF, newRect: RectF) {
        this.rect.set(rect)
        this.newRect.set(newRect)
        initValues()
    }

    fun updatePadding(newPadding: Float) {
        padding = newPadding
        initValues()
    }

    fun isSweepLessThanNormal(): Boolean = sweetAngle < (Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies - padding)

    fun isSweepGreaterThanMin(): Boolean = sweetAngle > MIN_ANGLE_PIE

    fun growUpAngle() {
        sweetAngle += VALUE_TO_ADD
        if (sweetAngle > (Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies - padding)) {
            sweetAngle = (Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies - padding)
        }
    }

    fun finalizeAngle() {
        sweetAngle -= VALUE_TO_ADD
        if (sweetAngle < MIN_ANGLE_PIE) {
            sweetAngle = MIN_ANGLE_PIE
        }

        if (sweetAngle <= 0f) {
            sweetAngle = 0f
        }
    }

    fun copyItClosed(): PieUchiwa {
        val pieCopy = PieUchiwa(index, numbersPies)
        pieCopy.id = id
        pieCopy.centerRect = centerRect
        pieCopy.updateMeasure(rect, newRect)
        pieCopy.updatePadding(padding)
        pieCopy.sweetAngle = 0f
        pieCopy.currentState = PieUchiwaEnum.CLOSED
        pieCopy.icone = icone
        pieCopy.distanceBetweenStartAndEndAngle = distanceBetweenStartAndEndAngle
        pieCopy.middleAngle = middleAngle
        return pieCopy
    }

    fun copy(): PieUchiwa {
        val pieCopy = PieUchiwa(index, numbersPies)
        pieCopy.id = id
        pieCopy.centerRect = centerRect
        pieCopy.updateMeasure(rect, newRect)
        pieCopy.updatePadding(padding)
        pieCopy.currentState = currentState
        pieCopy.icone = icone
        pieCopy.distanceBetweenStartAndEndAngle = distanceBetweenStartAndEndAngle
        pieCopy.middleAngle = middleAngle
        return pieCopy
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PieUchiwa

        if (index != other.index) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + id.hashCode()
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(index)
        parcel.writeInt(numbersPies)
        parcel.writeParcelable(icone, flags)
        parcel.writeParcelable(centerRect, flags)
        parcel.writeFloat(startAngle)
        parcel.writeFloat(sweetAngle)
        parcel.writeFloat(startAngleINIT)
        parcel.writeFloat(sweetAngleINIT)
        parcel.writeFloat(endAngleINIT)
        parcel.writeFloat(startAngleScale)
        parcel.writeFloat(sweetAngleScale)
        parcel.writeFloat(endAngle)
        parcel.writeFloat(distanceBetweenStartAndEndAngle)
        parcel.writeFloat(middleAngle)
        parcel.writeString(id)
        parcel.writeFloat(padding)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PieUchiwa> {
        private const val INIT_ALPHA = 190
        private const val VALUE_TO_ADD = 2
        private const val MIN_ANGLE_PIE = 20f

        override fun createFromParcel(parcel: Parcel): PieUchiwa {
            return PieUchiwa(parcel)
        }

        override fun newArray(size: Int): Array<PieUchiwa?> {
            return arrayOfNulls(size)
        }
    }
}

enum class PieUchiwaEnum {
    OPPENED,
    CLOSED,
    OPPENING,
    CLOSING
}