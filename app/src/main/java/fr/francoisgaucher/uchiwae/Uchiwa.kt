package fr.francoisgaucher.uchiwae

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.RectF
import android.os.Handler
import android.os.Message
import android.os.Parcelable
import android.support.annotation.AttrRes
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class Uchiwa : View {

    val paint = Paint(ANTI_ALIAS_FLAG)
    val rect = RectF()
    val newRect = RectF()
    val handlerAnimation: Handler


    private val pies: MutableList<PieUchiwa> = mutableListOf()
    private val piesCopy: MutableList<PieUchiwa> = mutableListOf()
    private var previusPieSelected: PieUchiwa? = null
    private var pieSelected: PieUchiwa? = null
    private var currentStep = UchiwaEnum.SELECTION_STEP
    private var lastIndicePieGot = 0
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

        pies.forEach {
            piesCopy.add(it.copy())
        }
        // ############################################################################
        // ############## DEBUG #######################################################
        paintDebug.color = Color.WHITE
        paintDebug.style = Paint.Style.STROKE
        paintDebug.strokeWidth = 3f
        // ############################################################################

        handlerAnimation = object : Handler() {
            override fun handleMessage(msg: Message?) {
                msg?.data?.let {
                    val isOppenning = it.getBoolean(UchiwaEnum.OPENNING_STEP.name)
                    var isIndiceChanged: Boolean
                    if (isOppenning) {
                        if (pieSelected!!.startAngle < START_DEGREE && piesCopy.size == 1) {
                            pieSelected!!.movingDown()
                            postInvalidate()
                        } else {
                            do {
                                isIndiceChanged = false
                                if (lastIndicePieGot < pies.size && pieSelected != pies[lastIndicePieGot]) {
                                    if (piesCopy.contains(pies[lastIndicePieGot]).not()) {
                                        val pieTmp = pies[lastIndicePieGot].copyItClosed()
                                        piesCopy.add(lastIndicePieGot, pieTmp)
                                        pieTmp.opening()
                                        if (pieTmp.index < pieSelected!!.index) {
                                            pieSelected!!.movingDown(pieTmp.endAngle)
                                        }
                                        postInvalidate()
                                    } else {
                                        if (piesCopy[lastIndicePieGot].isOpenned()) {
                                            lastIndicePieGot++
                                            isIndiceChanged = true
                                        } else {
                                            piesCopy[lastIndicePieGot].opening()
                                            if (piesCopy[lastIndicePieGot].index < pieSelected!!.index) {
                                                pieSelected!!.movingDown(piesCopy[lastIndicePieGot].endAngle)
                                            }
                                            postInvalidate()
                                        }
                                    }
                                } else {
                                    if (lastIndicePieGot >= pies.size) {
                                        currentStep = UchiwaEnum.SELECTION_STEP
                                        lastIndicePieGot = 0
                                        postInvalidate()
                                    } else {
                                        lastIndicePieGot++
                                        isIndiceChanged = true
                                    }
                                }
                            } while (isIndiceChanged)
                        }
                    }
                    // FERMETURE DE L'EVANTAIL
                    else {
                        do {
                            isIndiceChanged = false
                            if (piesCopy.isNotEmpty()) {
                                if (pieSelected != piesCopy.last()) {
                                    if (piesCopy.last().isClosed()) {
                                        piesCopy.remove(piesCopy.last())
                                        isIndiceChanged = true
                                    } else {
                                        piesCopy.last().closing()
                                        postInvalidate()
                                    }
                                } else {
                                    if (piesCopy.size > 1) {
                                        val pie = piesCopy.get(piesCopy.lastIndex - 1)
                                        if (pie.isClosed()) {
                                            piesCopy.remove(pie)
                                            isIndiceChanged = true
                                        } else {
                                            pie.closing()
                                            pieSelected!!.movingUp(pie.endAngle)
                                            postInvalidate()
                                        }
                                    } else {
                                        // DERNIERE ETAPE !
                                        // MOVE UP ENCORE POUR ATTEINDRE LE BORD DE L'ECRAN
                                        if (pieSelected!!.startAngle > TOP_DEGREE) {
                                            pieSelected!!.movingUp()
                                        } else {
                                            currentStep = UchiwaEnum.CLOSED_STEP
                                        }

                                        postInvalidate()
                                    }
                                }
                            } else {
                                currentStep = UchiwaEnum.CLOSED_STEP
                                piesCopy.clear()
                                piesCopy.addAll(pies)
                                postInvalidate()
                            }
                        } while (isIndiceChanged)
                    }
                }
            }
        }
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

        pies.forEachIndexed { index, pieUchiwa ->
            run {
                pieUchiwa.updateMeasure(rect, newRect)
                piesCopy.set(index, pieUchiwa.copy())
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (currentStep != UchiwaEnum.CLOSING_STEP
            && currentStep != UchiwaEnum.OPENNING_STEP
        ) {
            if (currentStep == UchiwaEnum.CLOSED_STEP ||
                currentStep == UchiwaEnum.SELECTING_CLOSED_STEP) {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val relX = event.x
                        val relY = event.y - POSITION_HELP_CIRCLE


                        xPoint = relX
                        yPoint = relY
                        currentStep = UchiwaEnum.SELECTING_CLOSED_STEP

                        postInvalidate()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val relX = event.x
                        val relY = event.y - POSITION_HELP_CIRCLE

                        xPoint = relX
                        yPoint = relY

                        currentStep = UchiwaEnum.SELECTING_CLOSED_STEP

                        postInvalidate()
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        currentStep = UchiwaEnum.CLOSED_STEP
                        val relX = event.x
                        val relY = event.y - POSITION_HELP_CIRCLE


                        xPoint = relX
                        yPoint = relY

                        // SI L'UTILISATEUR A SELECTIONNÉE UNE PORTION DE L'EVANTAIL
                        if (checkIfPieIsClicked(relX, relY, pieSelected = pieSelected!!)) {
                            currentStep = UchiwaEnum.OPENNING_STEP
                            runOpeningAnimation()
                        }

                        return true
                    }
                    else -> {
                        return true
                    }
                }
            } else if (currentStep == UchiwaEnum.SELECTION_STEP ||
                currentStep == UchiwaEnum.SELECTING_STEP
            ) {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val relX = event.x
                        val relY = event.y - POSITION_HELP_CIRCLE


                        xPoint = relX
                        yPoint = relY
                        currentStep = UchiwaEnum.SELECTING_STEP
                        checkIfPieIsClicked(relX, relY)
                        pieSelected?.let {
                            it.selecting()
                        }
                        postInvalidate()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val relX = event.x
                        val relY = event.y - POSITION_HELP_CIRCLE

                        xPoint = relX
                        yPoint = relY

                        currentStep = UchiwaEnum.SELECTING_STEP

                        checkIfPieIsClicked(relX, relY)
                        pieSelected?.let {
                            it.selecting()
                        }
                        postInvalidate()
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (pieSelected != null) {
                            pieSelected!!.unselected()
                            currentStep = UchiwaEnum.CLOSING_STEP
                            runClosingAnimation()
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
            }

        }
        return super.onTouchEvent(event)
    }

    private fun runClosingAnimation() {
        Thread(Runnable {
            while (currentStep == UchiwaEnum.CLOSING_STEP) {
                Thread.sleep(50)
                val message = Message()
                message.data.putBoolean(UchiwaEnum.OPENNING_STEP.name, false)
                handlerAnimation.dispatchMessage(message)
            }
        }).start()
    }

    private fun runOpeningAnimation() {
        Thread(Runnable {
            while (currentStep == UchiwaEnum.OPENNING_STEP) {
                Thread.sleep(50)
                val message = Message()
                message.data.putBoolean(UchiwaEnum.OPENNING_STEP.name, true)
                handlerAnimation.dispatchMessage(message)
            }
        }).start()
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
                this.pieSelected!!.selecting()
            } else {
                // SI ACTUELLEMENT NOUS N'AVONS PAS DE PORTION SELECTIONNÉE
                // ALORS ON PLACE CETTE NOUVELLE PORTION ET NOUS LA SELECTIONNONS "select"
                this.pieSelected = pieSelected
                this.pieSelected!!.selecting()
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
    }

    private fun checkIfPieIsClicked(vararg values: Float, pieSelected: PieUchiwa): Boolean {
        return isPointOnPieSelected((rect.width() / 2), values[0], values[1], pieSelected)


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

    fun isPointOnPieSelected(radius: Float, x: Float, y: Float, pieSelected: PieUchiwa): Boolean {

        // Calculate de la distance entre le centre du cercle et le point qui a ete toucher par l'utilisateur
        // la formule mathematique utilisée est le theoreme de pythagore
        // RACINE_CARRE(CARRE(x1-x2)+CARRE(y1-y2))
        val polarradius =
            Math.sqrt(Math.pow((x - rect.centerX()).toDouble(), 2.0) + Math.pow((y - rect.centerY()).toDouble(), 2.0))

        // Si la distance obtenu est plus grande que le rayon, nous n'essayons meme pas de faire le calcul,
        // nous savons que nous ne sommes pas dans le cercle
        if (polarradius > radius) {
            return false
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

        return if ((angle >= pieSelected.startAngle && angle <= pieSelected.endAngle && polarradius < radius)) {
            true
        } else if (pieSelected.startAngle >= Uchiwa.START_DEGREE
            && pieSelected.endAngle <= Uchiwa.LAST_DEGREE
            && polarradius < radius
        ) {
            return (angle >= pieSelected.startAngle && angle <= Uchiwa.MAX_DEGREE) ||
                    (angle >= 0.0f && angle <= pieSelected.endAngle)
        } else {
            false
        }
    }

    //
//
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val saved = canvas?.save()
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
            UchiwaEnum.SELECTING_CLOSED_STEP ->{
                drawUchiwaClosed(canvas)
            }
            UchiwaEnum.SELECTING_STEP -> {
                drawUchiwaSelecting(canvas)
            }
        }
        drawCircleHelpSelection(canvas)
        canvas?.restoreToCount(saved!!)
    }

    private fun drawUchiwaSelection(canvas: Canvas?) {
        for (pie in pies) {
            canvas?.drawArc(pie.rect, pie.startAngle, pie.sweetAngle, true, pie.paint)
        }
    }

    private fun drawUchiwaOpenning(canvas: Canvas?) {
        for (pie in piesCopy) {
            if (pie != pieSelected) {
                canvas?.drawArc(pie.rect, pie.startAngle, pie.sweetAngle, true, pie.paint)
            }
        }
        pieSelected?.let { pieTmp ->
            canvas?.drawArc(pieTmp.rect, pieTmp.startAngle, pieTmp.sweetAngle, true, pieTmp.paint)
            pieTmp.paintAnimation?.let { borderPaint ->
                canvas?.drawArc(pieTmp.rect, pieTmp.startAngle, pieTmp.sweetAngle, true, borderPaint)
            }
        }
    }

    private fun drawUchiwaClosing(canvas: Canvas?) {
        for (pie in piesCopy) {
            if (pie != pieSelected) {
                canvas?.drawArc(pie.rect, pie.startAngle, pie.sweetAngle, true, pie.paint)
            }
        }
        pieSelected?.let { pieTmp ->
            canvas?.drawArc(pieTmp.rect, pieTmp.startAngle, pieTmp.sweetAngle, true, pieTmp.paint)
            pieTmp.paintAnimation?.let { borderPaint ->
                canvas?.drawArc(pieTmp.rect, pieTmp.startAngle, pieTmp.sweetAngle, true, borderPaint)
            }
        }
    }

    private fun drawUchiwaClosed(canvas: Canvas?) {
        pieSelected?.let { pieTmp ->
            canvas?.drawArc(pieTmp.rect, pieTmp.startAngle, pieTmp.sweetAngle, true, pieTmp.paint)
            pieTmp.paintAnimation?.let { borderPaint ->
                canvas?.drawArc(pieTmp.rect, pieTmp.startAngle, pieTmp.sweetAngle, true, borderPaint)
            }
        }
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

    private fun drawCircleHelpSelection(canvas: Canvas?) {
        if (currentStep == UchiwaEnum.SELECTING_STEP ||
            currentStep == UchiwaEnum.SELECTING_CLOSED_STEP    ) {
            canvas?.drawArc(xPoint - 10, yPoint - 10, xPoint + 10, yPoint + 10, 0f, 360f, false, paintDebug)
            canvas?.drawCircle(xPoint, yPoint, 1f, paintDebug)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }

    companion object {
        // PERCENT
        private const val MAX_SIZE_CAMEMBERT = 0.90f

        private const val POSITION_HELP_CIRCLE = 130

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
    SELECTING_STEP, // L'utilisateur a toujours le doigt sur un element
    SELECTING_CLOSED_STEP, // L'utilisateur a le doigt sur l'ecran mais a qu'un seul element d'affiche
}