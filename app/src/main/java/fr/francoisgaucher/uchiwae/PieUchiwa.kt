package fr.francoisgaucher.uchiwae

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import java.util.*


class PieUchiwa(val index: Int, private val numbersPies: Int) : Parcelable{

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val paintAnimation = Paint(Paint.ANTI_ALIAS_FLAG)
    var paintSelected: Paint? = null

    val rect = RectF()
    val newRect = RectF()

    var startAngleINIT: Float = 0f
    var sweetAngleINIT: Float = 0f
    var endAngleINIT: Float = 0f

    var startAngle: Float = 0f
    var sweetAngle: Float = 0f

    var startAngleScale: Float = 0f
    var sweetAngleScale: Float = 0f

    var endAngle: Float = 0f
    var id: String = ""
    var padding = 0f

    private var currentState = PieUchiwaEnum.OPPENED

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    ) {
        startAngleINIT = parcel.readFloat()
        sweetAngleINIT = parcel.readFloat()
        endAngleINIT = parcel.readFloat()
        startAngle = parcel.readFloat()
        sweetAngle = parcel.readFloat()
        startAngleScale = parcel.readFloat()
        sweetAngleScale = parcel.readFloat()
        endAngle = parcel.readFloat()
        id = parcel.readString()
        padding = parcel.readFloat()
    }


    init {
        id = UUID.randomUUID().toString() + " / INDEX = $index"
        initValues()
    }

    private fun initValues() {
        startAngle =
                if ((Uchiwa.START_DEGREE + ((Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies) * index) + padding) > Uchiwa.MAX_DEGREE) {
                    ((Uchiwa.START_DEGREE + ((Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies) * index) + padding) % Uchiwa.MAX_DEGREE)
                } else {
                    (Uchiwa.START_DEGREE + ((Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies) * index) + padding)
                }
        startAngleScale = startAngle - (VALUE_TO_ADD / 2)
        if (startAngleScale == 360f) {
            startAngleScale = 0f
        }
        if (startAngle == 360f) {
            startAngle = 0f
        }
        startAngleINIT = startAngle

        sweetAngle = Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies - padding
        sweetAngleINIT = sweetAngle
        sweetAngleScale = sweetAngle + VALUE_TO_ADD

        endAngle = if ((startAngle + sweetAngle) > Uchiwa.MAX_DEGREE) {
            (startAngle + sweetAngle) % Uchiwa.MAX_DEGREE
        } else {
            (startAngle + sweetAngle)
        }
        endAngleINIT = endAngle

        paint.color = Color.rgb(160 + (10 * index), 10, 10)

        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        paint.alpha = INIT_ALPHA


        paintAnimation?.apply {
            strokeWidth = 2f
            color = Color.WHITE
            style = Paint.Style.STROKE
        }
    }

    fun movingUp(endAngle: Float? = null) {
        if (endAngle != null) {
            startAngle = endAngle
            this.endAngle = if ((startAngle + sweetAngle) > Uchiwa.MAX_DEGREE) {
                (startAngle + sweetAngle) % Uchiwa.MAX_DEGREE
            } else {
                (startAngle + sweetAngle)
            }
        } else {
            startAngle -= VALUE_TO_ADD
            if (startAngle < Uchiwa.TOP_DEGREE) {
                startAngle = Uchiwa.TOP_DEGREE
            }
            this.endAngle = if ((startAngle + sweetAngle) > Uchiwa.MAX_DEGREE) {
                (startAngle + sweetAngle) % Uchiwa.MAX_DEGREE
            } else {
                (startAngle + sweetAngle)
            }
        }
    }

    fun movingDown(endAngle: Float? = null) {
        if (endAngle != null) {
            startAngle = endAngle + padding

            this.endAngle = if ((startAngle + sweetAngle) > Uchiwa.MAX_DEGREE) {
                (startAngle + sweetAngle) % Uchiwa.MAX_DEGREE
            } else {
                (startAngle + sweetAngle)
            }
        } else {
            startAngle += VALUE_TO_ADD

            if (startAngle > Uchiwa.START_DEGREE + padding) {
                startAngle = Uchiwa.START_DEGREE + padding
            }

            this.endAngle = if ((startAngle + sweetAngle) > Uchiwa.MAX_DEGREE) {
                (startAngle + sweetAngle) % Uchiwa.MAX_DEGREE
            } else {
                (startAngle + sweetAngle)
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

        endAngle = if ((startAngle + sweetAngle) > Uchiwa.MAX_DEGREE) {
            (startAngle + sweetAngle) % Uchiwa.MAX_DEGREE
        } else {
            (startAngle + sweetAngle)
        }

        if (sweetAngle <= 0f) {
            sweetAngle = 0f
            endAngle = if ((startAngle + sweetAngle) > Uchiwa.MAX_DEGREE) {
                (startAngle + sweetAngle) % Uchiwa.MAX_DEGREE
            } else {
                (startAngle + sweetAngle)
            }
            currentState = PieUchiwaEnum.CLOSED
        }

    }

    fun opening() {
        currentState = PieUchiwaEnum.OPPENING
        sweetAngle += VALUE_TO_ADD
        if (sweetAngle >= (Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies) - padding) {
            sweetAngle = (Uchiwa.UCHIWA_FINAL_DEGREE / numbersPies) - padding
            currentState = PieUchiwaEnum.OPPENED
        }
        endAngle = if ((startAngle + sweetAngle) > Uchiwa.MAX_DEGREE) {
            (startAngle + sweetAngle) % Uchiwa.MAX_DEGREE
        } else {
            (startAngle + sweetAngle)
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

        endAngle = if ((startAngle + sweetAngle) > Uchiwa.MAX_DEGREE) {
            (startAngle + sweetAngle) % Uchiwa.MAX_DEGREE
        } else {
            (startAngle + sweetAngle)
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
        endAngle = if ((startAngle + sweetAngle) > Uchiwa.MAX_DEGREE) {
            (startAngle + sweetAngle) % Uchiwa.MAX_DEGREE
        } else {
            (startAngle + sweetAngle)
        }
    }

    fun copyItClosed(): PieUchiwa {
        val pieCopy = PieUchiwa(index, numbersPies)
        pieCopy.id = id
        pieCopy.updateMeasure(rect, newRect)
        pieCopy.updatePadding(padding)
        pieCopy.sweetAngle = 0f
        pieCopy.currentState = PieUchiwaEnum.CLOSED
        return pieCopy
    }

    fun copy(): PieUchiwa {
        val pieCopy = PieUchiwa(index, numbersPies)
        pieCopy.id = id
        pieCopy.updateMeasure(rect, newRect)
        pieCopy.updatePadding(padding)
        pieCopy.currentState = currentState
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
        parcel.writeFloat(startAngleINIT)
        parcel.writeFloat(sweetAngleINIT)
        parcel.writeFloat(endAngleINIT)
        parcel.writeFloat(startAngle)
        parcel.writeFloat(sweetAngle)
        parcel.writeFloat(startAngleScale)
        parcel.writeFloat(sweetAngleScale)
        parcel.writeFloat(endAngle)
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