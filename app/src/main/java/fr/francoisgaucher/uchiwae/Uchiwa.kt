package fr.francoisgaucher.uchiwae

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.RectF
import android.support.annotation.AttrRes
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import fr.francoisgaucher.uchiwa.BuildConfig


class Uchiwa : View {

    val paint = Paint(ANTI_ALIAS_FLAG)
    val rect = RectF()
    val newRect = RectF()


    private val pies: MutableList<PieUchiwa> = mutableListOf()
    private var previusPieSelected: PieUchiwa? = null
    private var pieSelected: PieUchiwa? = null
    private var currentStep = UchiwaEnum.SELECTION_STEP

    // ############################################################################
    // ############## DEBUG #######################################################
    var xPoint: Float = 0f
    var yPoint: Float = 0f
    val paintDebug = Paint(ANTI_ALIAS_FLAG)
    // ############################################################################

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr, 0) {

        setWillNotDraw(false)
    }

    init {
        pies.add(PieUchiwa(0, 8))
        pies.add(PieUchiwa(1, 8))

        pies.add(PieUchiwa(2, 8))
        pies.add(PieUchiwa(3, 8))

        pies.add(PieUchiwa(4, 8))
        pies.add(PieUchiwa(5, 8))


        pies.add(PieUchiwa(6, 8))
        pies.add(PieUchiwa(7, 8))

        // ############################################################################
        // ############## DEBUG #######################################################
        if (BuildConfig.DEBUG) {
            paintDebug.color = Color.WHITE
            paintDebug.style = Paint.Style.STROKE
            paintDebug.strokeWidth = 3f
        }
        // ############################################################################
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val maxWidth = measuredWidth.toFloat()
        val maxHeight = measuredHeight.toFloat()

        val newWidth: Float
        val newHeight: Float

        if (maxWidth <= maxHeight) {
            newWidth = maxWidth * (MAX_SIZE_CAMEMBERT)
            newHeight = (maxWidth * (MAX_SIZE_CAMEMBERT))
        } else {
            newWidth = maxHeight * (MAX_SIZE_CAMEMBERT)
            newHeight = (maxHeight * (MAX_SIZE_CAMEMBERT))
        }


        rect.left = if (newWidth < newHeight) {
            -newWidth + (newWidth / 2)
        } else {
            -newHeight + (newHeight / 2)
        }
        rect.right = if (newWidth < newHeight) {
            newWidth / 2
        } else {
            newHeight / 2
        }
        rect.top = if (newWidth < newHeight) {
            (maxWidth - newWidth) / 2
        } else {
            (maxHeight - newHeight) / 2
        }
        rect.bottom = if (newWidth < newHeight) {
            maxWidth - ((maxWidth - newWidth) / 2)
        } else {
            maxHeight - ((maxHeight - newHeight) / 2)
        }

        newRect.set(rect)
        newRect.inset(-20f, -20f)

        pies.forEach {
            it.updateMeasure(rect, newRect)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val relX = event.x
                val relY = if (BuildConfig.DEBUG) {
                    event.y - 70
                } else {
                    event.y
                }

                xPoint = relX
                yPoint = relY
                currentStep = UchiwaEnum.SELECTING_STEP
                checkIfPieIsClicked(relX, relY)
                pieSelected?.let {
                    it.selected()
                }
                if (BuildConfig.DEBUG) {
                    postInvalidate()
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val relX = event.x
                val relY = if (BuildConfig.DEBUG) {
                    event.y - 70
                } else {
                    event.y
                }

                xPoint = relX
                yPoint = relY

                currentStep = UchiwaEnum.SELECTING_STEP

                checkIfPieIsClicked(relX, relY)
                pieSelected?.let {
                    it.selected()
                }
                if (BuildConfig.DEBUG) {
                    postInvalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (pieSelected != null) {
                    pieSelected!!.unselected()
                    currentStep = UchiwaEnum.CLOSING_STEP
                    postInvalidate()
                } else {
                    currentStep = UchiwaEnum.SELECTION_STEP
                    postInvalidate()
                }
                return true
            }
            else -> {
                currentStep = UchiwaEnum.SELECTION_STEP
                postInvalidate()
                return super.onTouchEvent(event)
            }
        }
        return super.onTouchEvent(event)
    }

    private fun checkIfPieIsClicked(vararg values: Float) {
        val pieSelected = checkPoint((rect.width() / 2), values[0], values[1])

        // SI L'UTILISATEUR A SELECTIONNÉE UNE PORTION DE L'EVANTAIL
        if (pieSelected != null) {
            // SI LA PORTION QU'A SELECTIONNÉE L'UTILISATEUR EST LA MEME QUE L'ACTUELLE
            // PAS BESOIN DE FAIRE QUOI QUE CE SOIT
            if (this.pieSelected == pieSelected) {
                this.previusPieSelected = this.pieSelected
                return
            }
            // DANS LE CAS CONTRAIRE, SI ACTUELLEMENT NOUS AVONS UNE PORTION DE SELECTIONNÉE
            // ALORS NOUS DEVONS LA DESELECTIONNER "unselect" ET SELECTIONNER "select" LA NOUVELLE
            if (this.pieSelected != null) {
                previusPieSelected = this.pieSelected
                previusPieSelected!!.unselected()
                this.pieSelected = pieSelected
                this.pieSelected!!.selected()
            } else {
                // SI ACTUELLEMENT NOUS N'AVONS PAS DE PORTION SELECTIONNÉE
                // ALORS ON PLACE CETTE NOUVELLE PORTION ET NOUS LA SELECTIONNONS "select"
                this.pieSelected = pieSelected
                this.pieSelected!!.selected()
            }
        } else {
            // SINON
            //
            // SOIT UNE PIECE AVAIT ETE PRECEDEMMENT SELECTIONNÉE ET DONC NOUS DEVONS LA DESELECTIONNER "unselect"
            // ET RAZ LES DONNEES
            if (this.pieSelected != null) {
                this.pieSelected!!.unselected()
                this.previusPieSelected = this.pieSelected
                this.pieSelected = null
            } else {
                // SOIT RIEN DE PARTICULIER
            }
            return
        }
        // ############################################################################
        // ############## DEBUG #######################################################
//        if (BuildConfig.DEBUG && pieSelected != null) {
//            Toast.makeText(context, "UCHIWA CLICKED : pieSelected" + pieSelected?.id, Toast.LENGTH_SHORT).show()
//        }
        // ############################################################################
    }

    fun checkPoint(radius: Float, x: Float, y: Float): PieUchiwa? {

        // Calculate de la distance entre le centre du cercle et le point qui a ete toucher par l'utilisateur
        // la formule mathematique utilisée est le theoreme de pythagore
        // RACINE_CARRE(CARRE(x1-x2)+CARRE(y1-y2))
        val polarradius =
            Math.sqrt(Math.pow((x - rect.centerX()).toDouble(), 2.0) + Math.pow((y - rect.centerY()).toDouble(), 2.0))

        // Si la distance obtenu est plus grande que le rayon, nous n'essayons meme pas de faire le calcul,
        // nous savons que nous ne sommes pas dans le cercle
        if (polarradius > radius) {
            return null
        }

        var angle = Math.toDegrees(Math.atan2((y.toDouble() - rect.centerY()), (x.toDouble() - rect.centerX())))
        if (angle < 0) {
            angle += 360
        }
        // Check whether polarradius is
        // less then radius of circle
        // or not and Angle is between
        // startAngle and endAngle
        // or not
        var pieSelected: PieUchiwa? = null
        pies.forEach {
            //             if(pieSelected == null && (angle >= it.startAngle && angle <= it.endAngle && polarradius < radius)){
//                 pieSelected = it
//             }
            if ((angle >= it.startAngle && angle <= it.endAngle && polarradius < radius)) {
                pieSelected = it
            } else if (it.startAngle >= Uchiwa.START_DEGREE && it.endAngle <= Uchiwa.LAST_DEGREE && polarradius < radius) {
                if ((angle >= it.startAngle && angle <= Uchiwa.MAX_DEGREE) ||
                    (angle >= 0.0f && angle <= it.endAngle)
                ) {
                    pieSelected = it
                }
            }
        }
        return pieSelected
    }

    //
//
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)


        when (currentStep) {
            UchiwaEnum.SELECTION_STEP -> {
                drawUchiwaSelection(canvas)
            }
            UchiwaEnum.OPENNING_STEP -> {
                drawUchiwaOpenning(canvas)
            }
            UchiwaEnum.CLOSING_STEP -> {
                drawUchiwaClosing(canvas)
            }
            UchiwaEnum.CLOSED_STEP -> {
                drawUchiwaClosed(canvas)
            }
            UchiwaEnum.SELECTING_STEP -> {
                drawUchiwaSelecting(canvas)
            }
        }
        if (BuildConfig.DEBUG && currentStep == UchiwaEnum.SELECTING_STEP) {
            canvas?.drawArc(xPoint - 10, yPoint - 10, xPoint + 10, yPoint + 10, 0f, 360f, false, paintDebug)
            canvas?.drawCircle(xPoint, yPoint, 1f, paintDebug)
        }
    }

    private fun drawUchiwaSelection(canvas: Canvas?) {
        for (pie in pies) {
            canvas?.drawArc(pie.rect, pie.startAngle, pie.sweetAngle, true, pie.paint)
        }
    }

    private fun drawUchiwaOpenning(canvas: Canvas?) {
        for (pie in pies) {
            canvas?.drawArc(pie.rect, pie.startAngle, pie.sweetAngle, true, pie.paint)
        }
    }

    private fun drawUchiwaClosing(canvas: Canvas?) {
        for (pie in pies) {
            canvas?.drawArc(pie.rect, pie.startAngle, pie.sweetAngle, true, pie.paint)
        }
    }

    private fun drawUchiwaClosed(canvas: Canvas?) {

    }

    private fun drawUchiwaSelecting(canvas: Canvas?) {
        for (pie in pies) {
            if (pie != pieSelected) {
                canvas?.drawArc(pie.rect, pie.startAngle, pie.sweetAngle, true, pie.paint)
            }
        }
        pieSelected?.let { pieTmp ->
            canvas?.drawArc(pieTmp.newRect, pieTmp.startAngleScale, pieTmp.sweetAngleScale, true, pieTmp.paint)
            pieTmp.paintSelected?.let { borderPaint ->
                canvas?.drawArc(pieTmp.newRect, pieTmp.startAngleScale, pieTmp.sweetAngleScale, true, borderPaint)
            }
        }

    }

    companion object {
        // PERCENT
        private const val MAX_SIZE_CAMEMBERT = 0.90f


        const val UCHIWA_ANGLE: Float = 30f
        private const val ALF_DEGREE: Float = 180f
        const val UCHIWA_RADIUS: Float = UCHIWA_ANGLE / 2
        const val TOP_DEGREE: Float = 270f
        const val MAX_DEGREE: Float = 360f
        private const val BOTTOM_DEGREE: Float = 90f
        const val START_DEGREE: Float = TOP_DEGREE + UCHIWA_RADIUS
        const val LAST_DEGREE: Float = BOTTOM_DEGREE - UCHIWA_RADIUS

        const val UCHIWA_FINAL_DEGREE = ALF_DEGREE - UCHIWA_ANGLE
    }
}

private enum class UchiwaEnum {
    SELECTION_STEP, // L'evantail propose son panel d'option
    OPENNING_STEP, // l'evantail est en train de s'ouvrir (CLOSED_STEP -> SELECTION_STEP)
    CLOSING_STEP, // l'evantail est en train de se fermer (SELECTION_STEP -> CLOSED_STEP)
    CLOSED_STEP, // L'evantail n'affiche plus qu'une seule et unique option (celle que l'utilisateur a sélectionné)
    SELECTING_STEP // L'utilisateur a toujours le doigt sur un element
}