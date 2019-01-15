package fr.francoisgaucher.uchiwae

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.os.Parcelable
import android.support.annotation.AttrRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import fr.francoisgaucher.uchiwa.R
import java.util.concurrent.ConcurrentLinkedDeque


class Uchiwa : View {

    val rect = RectF()
    val newRect = RectF()
    var handlerAnimation: Handler


    private var pies: MutableList<PieUchiwa> = mutableListOf()
    private var piesCopy: MutableList<PieUchiwa> = mutableListOf()
    private var previusPieSelected: PieUchiwa? = null
    private var pieSelected: PieUchiwa? = null
    private var currentStep = UchiwaEnum.SELECTION_STEP
    private var lastIndicePieGot = 0
    private var paddingPie: Float = 0f

    @Volatile
    private var forceStopAnimation: Boolean = false

    private var threadClosingAnimation: Thread? = null
    private var threadOpenningAnimation: Thread? = null
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
        isSaveEnabled = true
        if (attrs == null) {
            paddingPie = 0f
            return
        }

        val ta = context.obtainStyledAttributes(attrs, R.styleable.Uchiwa)
        paddingPie = ta.getDimension(R.styleable.Uchiwa_pie_padding, 0f)
        ta.recycle()
        updatePadding()
    }

    init {
        val numberPies = 4
        var i = 0
        val bitmap = drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.ic_all_inclusive_black_24dp)!!)

        pies.add(PieUchiwa(i++, numberPies, bitmap))
        pies.add(PieUchiwa(i++, numberPies, bitmap))
//
        pies.add(PieUchiwa(i++, numberPies, bitmap))
        pies.add(PieUchiwa(i++, numberPies, bitmap))
////
//        pies.add(PieUchiwa(i++, numberPies, bitmap))
//        pies.add(PieUchiwa(i++, numberPies, bitmap))
//
//
//        pies.add(PieUchiwa(i++, numberPies, bitmap))
//        pies.add(PieUchiwa(i++, numberPies, bitmap))

        pies.forEach {
            piesCopy.add(it.copy())
        }
        // ############################################################################
        // ############## DEBUG #######################################################
        paintDebug.color = Color.WHITE
        paintDebug.style = Paint.Style.STROKE
        paintDebug.strokeWidth = 3f
        // ############################################################################

        handlerAnimation = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message?) {
                msg?.data?.let {
                    val isOppenning = it.getBoolean(UchiwaEnum.OPENNING_STEP.name)
                    var isIndiceChanged: Boolean
                    if (isOppenning) {
                        if (pieSelected!!.startAngle < (START_DEGREE + paddingPie) && piesCopy.size == 1) {
                            pieSelected!!.movingDown()
                            postInvalidate()
                        } else {
                            if (pieSelected!!.isSweepLessThanNormal()) {
                                pieSelected!!.growUpAngle()
                                postInvalidate()
                            } else {
                                do {
                                    isIndiceChanged = false
                                    var lastIndicePieGotSafe = lastIndicePieGot
                                    synchronized(lastIndicePieGot) {
                                        if (lastIndicePieGotSafe < pies.size && pieSelected != pies[lastIndicePieGotSafe]) {
                                            if (piesCopy.contains(pies[lastIndicePieGotSafe]).not()) {
                                                val pieTmp = pies[lastIndicePieGotSafe].copyItClosed()
                                                piesCopy.add(lastIndicePieGotSafe, pieTmp)
                                                pieTmp.opening()
                                                if (pieTmp.index < pieSelected!!.index) {
                                                    pieSelected!!.movingDown(pieTmp.endAngle)
                                                }
                                                postInvalidate()
                                            } else {
                                                if (lastIndicePieGotSafe < piesCopy.size) {
                                                    if (piesCopy[lastIndicePieGotSafe].isOpenned()) {
                                                        lastIndicePieGotSafe++
                                                        isIndiceChanged = true
                                                    } else {
                                                        piesCopy[lastIndicePieGotSafe].opening()
                                                        if (piesCopy[lastIndicePieGotSafe].index < pieSelected!!.index) {
                                                            pieSelected!!.movingDown(piesCopy[lastIndicePieGotSafe].endAngle)
                                                        }
                                                        postInvalidate()
                                                    }
                                                }

                                            }
                                        } else {
                                            if (lastIndicePieGotSafe >= pies.size) {
                                                currentStep = UchiwaEnum.SELECTION_STEP
                                                stopAllTrhreads()

                                                lastIndicePieGotSafe = 0
                                                postInvalidate()
                                            } else {
                                                lastIndicePieGotSafe++
                                                isIndiceChanged = true
                                            }
                                        }
                                    }
                                    lastIndicePieGot = lastIndicePieGotSafe
                                } while (isIndiceChanged)
                            }
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
                                            // DERNIERE ANIMATION
                                            // SI LA PIE CHART EST PLUS GRANDE QUE LA VALEUR MIN CONFIG DANS PIEUCHIWA
                                            // ALORS ON DIMINUE L'ANGLE DU PIE
                                            if (pieSelected!!.isSweepGreaterThanMin()) {
                                                pieSelected!!.finalizeAngle()
                                            } else {
                                                currentStep = UchiwaEnum.CLOSED_STEP
                                                stopAllTrhreads()
                                            }
                                        }

                                        postInvalidate()
                                    }
                                }
                            } else {
                                currentStep = UchiwaEnum.CLOSED_STEP
                                stopAllTrhreads()

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

    fun drawableToBitmap(drawable: Drawable): Bitmap {

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    private fun updatePadding() {
        pies.forEach {
            it.updatePadding(paddingPie)
        }
        postInvalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        updateMeasure()
    }

    private fun updateMeasure() {
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

        pies.forEach { pieUchiwa ->
            run {
                pieUchiwa.updateMeasure(rect, newRect)
                val index = piesCopy.indexOfFirst { pie -> pie.index == pieUchiwa.index }
                if (index >= 0) {
                    piesCopy[index] = pieUchiwa.copy()
                }
            }
        }

        pieSelected?.let {
            it.updateMeasure(rect, newRect)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (currentStep != UchiwaEnum.CLOSING_STEP
            && currentStep != UchiwaEnum.OPENNING_STEP
        ) {
            if (currentStep == UchiwaEnum.CLOSED_STEP ||
                currentStep == UchiwaEnum.SELECTING_CLOSED_STEP
            ) {
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
        if (threadClosingAnimation == null) {
            threadClosingAnimation = Thread(Runnable {
                try {
                    while (forceStopAnimation.not() && currentStep == UchiwaEnum.CLOSING_STEP && !Thread.currentThread().isInterrupted) {
                        Thread.sleep(TIME_REFRESH)
                        val message = Message()
                        message.data.putBoolean(UchiwaEnum.OPENNING_STEP.name, false)
                        handlerAnimation.dispatchMessage(message)
                    }
                } catch (interruptedException: InterruptedException) {
                }
            })
            threadClosingAnimation?.start()
        }
    }

    private fun runOpeningAnimation() {
        if (threadOpenningAnimation == null) {
            threadOpenningAnimation = Thread(Runnable {
                try {
                    while (forceStopAnimation.not() && currentStep == UchiwaEnum.OPENNING_STEP && !Thread.currentThread().isInterrupted) {
                        Thread.sleep(TIME_REFRESH)
                        val message = Message()
                        message.data.putBoolean(UchiwaEnum.OPENNING_STEP.name, true)
                        handlerAnimation.dispatchMessage(message)
                    }
                } catch (interruptedException: InterruptedException) {
                }
            })
            threadOpenningAnimation?.start()
        }
    }

    private fun checkIfPieIsClicked(vararg values: Float) {
        val pieSelected = checkPoint((rect.width() / 2), values[0], values[1])

        // SI L'UTILISATEUR A SELECTIONNÉE UNE PORTION DE L'EVANTAIL
        if (pieSelected != null) {
            // SI LA PORTION QU'A SELECTIONNÉE L'UTILISATEUR EST LA MEME QUE L'ACTUELLE
            // PAS BESOIN DE FAIRE QUOI QUE CE SOIT
            if (this.pieSelected == pieSelected) {
                this.previusPieSelected = this.pieSelected!!.copy()
                return
            }
            // DANS LE CAS CONTRAIRE, SI ACTUELLEMENT NOUS AVONS UNE PORTION DE SELECTIONNÉE
            // ALORS NOUS DEVONS LA DESELECTIONNER "unselect" ET SELECTIONNER "select" LA NOUVELLE
            if (this.pieSelected != null) {
                previusPieSelected = this.pieSelected!!.copy()
                previusPieSelected!!.unselected()
                this.pieSelected = pieSelected.copy()
                this.pieSelected!!.selecting()
            } else {
                // SI ACTUELLEMENT NOUS N'AVONS PAS DE PORTION SELECTIONNÉE
                // ALORS ON PLACE CETTE NOUVELLE PORTION ET NOUS LA SELECTIONNONS "select"
                this.pieSelected = pieSelected.copy()
                this.pieSelected!!.selecting()
            }
        } else {
            // SINON
            //
            // SOIT UNE PIECE AVAIT ETE PRECEDEMMENT SELECTIONNÉE ET DONC NOUS DEVONS LA DESELECTIONNER "unselect"
            // ET RAZ LES DONNEES
            if (this.pieSelected != null) {
                this.pieSelected!!.unselected()
                this.previusPieSelected = this.pieSelected!!.copy()
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
        }
        // DANS LE CAS OU LA PIE UCHIWA COMMENCE SON ANGLE AVANT L'ANGLE 0 (EXEMPLE 355)
        // ET QUE SON ANGLE DE FIN CE TROUVE SOUS CE MEME ANGLE (EXEMPLE 3)
        // ON TRICHE UN PEU ON RAJOUTE LA VALEUR DE L'ANGLE DE FIN A 360
        // POUR AVOIR EXEMPLE 363
        else if (pieSelected.sweetAngle > QUART_ANGLE && (angle >= pieSelected.startAngle && angle <= (MAX_DEGREE + pieSelected.endAngle) && polarradius < radius)) {
            true
        } else {
            false
        }
    }

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
            UchiwaEnum.SELECTING_CLOSED_STEP -> {
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
        drawPies(canvas, controlPieSelected = false)
    }

    private fun drawUchiwaOpenning(canvas: Canvas?) {
        drawPiesCopies(canvas)
        drawPieSelected(canvas)
    }

    private fun drawUchiwaClosing(canvas: Canvas?) {
        drawPiesCopies(canvas)
        drawPieSelected(canvas)
    }

    private fun drawUchiwaClosed(canvas: Canvas?) {
        drawPieSelected(canvas)
    }

    private fun drawUchiwaSelecting(canvas: Canvas?) {
        drawPies(canvas)
        drawPieSelected(canvas, isScaled = true)
    }

    private fun drawPies(canvas: Canvas?, controlPieSelected: Boolean = true) {
        val piesConcurrent = ConcurrentLinkedDeque(pies)
        val iterators = piesConcurrent.iterator()
        while (iterators.hasNext()) {
            val pie = iterators.next()
            if ((controlPieSelected && pie != pieSelected) || !controlPieSelected) {
                canvas?.drawArc(pie.rect, pie.startAngle, pie.sweetAngle, true, pie.paint)
                pie.icone?.let {
                    canvas?.drawBitmap(
                        it,
                        pie.centerRect.left - it.width.div(2),
                        pie.centerRect.top - it.height.div(2),
                        pie.paintIcone
                    )
                }
            }
        }
    }

    private fun drawPiesCopies(canvas: Canvas?) {
        val piesConcurrent = ConcurrentLinkedDeque(piesCopy)
        val iterators = piesConcurrent.iterator()
        while (iterators.hasNext()) {
            val pieCopy = iterators.next()
            if (pieCopy != pieSelected) {
                canvas?.drawArc(pieCopy.rect, pieCopy.startAngle, pieCopy.sweetAngle, true, pieCopy.paint)
                pieCopy.icone?.let {
                    canvas?.drawBitmap(
                        it,
                        pieCopy.centerRect.left - it.width.div(2),
                        pieCopy.centerRect.top - it.height.div(2),
                        pieCopy.paintIcone
                    )
                }
            }
        }
    }

    private fun drawPieSelected(canvas: Canvas?, isScaled: Boolean = false) {
        pieSelected?.let { pieTmp ->
            if (isScaled) {
                canvas?.drawArc(pieTmp.newRect, pieTmp.startAngleScale, pieTmp.sweetAngleScale, true, pieTmp.paint)
                pieTmp.paintSelected?.let { borderPaint ->
                    canvas?.drawArc(pieTmp.newRect, pieTmp.startAngleScale, pieTmp.sweetAngleScale, true, borderPaint)
                }
            } else {
                canvas?.drawArc(pieTmp.rect, pieTmp.startAngle, pieTmp.sweetAngle, true, pieTmp.paint)
                pieTmp.paintAnimation.let { borderPaint ->
                    canvas?.drawArc(pieTmp.rect, pieTmp.startAngle, pieTmp.sweetAngle, true, borderPaint)
                }
            }
            pieTmp.icone?.let {
                canvas?.drawBitmap(
                    it,
                    pieTmp.centerRect.left - it.width.div(2),
                    pieTmp.centerRect.top - it.height.div(2),
                    pieTmp.paintIcone
                )
            }
        }
    }

    private fun drawCircleHelpSelection(canvas: Canvas?) {
        if (currentStep == UchiwaEnum.SELECTING_STEP ||
            currentStep == UchiwaEnum.SELECTING_CLOSED_STEP
        ) {
            canvas?.drawArc(xPoint - 10, yPoint - 10, xPoint + 10, yPoint + 10, 0f, 360f, false, paintDebug)
            canvas?.drawCircle(xPoint, yPoint, 1f, paintDebug)
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) //onResume called
        {
            if (currentStep == UchiwaEnum.CLOSING_STEP) {
                forceStopAnimation = false
                runClosingAnimation()
            } else if (currentStep == UchiwaEnum.OPENNING_STEP) {
                forceStopAnimation = false

                runOpeningAnimation()
            }
        } else // onPause() called
        {
            if (currentStep == UchiwaEnum.CLOSING_STEP ||
                currentStep == UchiwaEnum.OPENNING_STEP
            ) {
                stopAllTrhreads()

                forceStopAnimation = true
            }
        }

    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus) //onresume() called
        {

            if (currentStep == UchiwaEnum.CLOSING_STEP) {
                forceStopAnimation = false
                runClosingAnimation()
            } else if (currentStep == UchiwaEnum.OPENNING_STEP) {
                forceStopAnimation = false
                runOpeningAnimation()
            }
        } else // onPause() called
        {
            if (currentStep == UchiwaEnum.CLOSING_STEP ||
                currentStep == UchiwaEnum.OPENNING_STEP
            ) {
                stopAllTrhreads()

                forceStopAnimation = true
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

    }

    private fun stopAllTrhreads() {
        threadClosingAnimation?.interrupt()
        threadOpenningAnimation?.interrupt()
        threadOpenningAnimation = null
        threadClosingAnimation = null
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SaveStateUchiwa(superState)
        ss.paddingPie = paddingPie
        ss.pieSelected = pieSelected
        ss.pies = pies
        ss.piesCopy = piesCopy
        ss.uchiwaEnum = currentStep
        ss.lastIndicePieGot = lastIndicePieGot
        ss.forceStopAnimation = forceStopAnimation

        stopAllTrhreads()
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val ss = state as SaveStateUchiwa
        super.onRestoreInstanceState(ss.getSuperState())
        ss.pies?.let {
            pies = it.toMutableList()
        }
        ss.piesCopy?.let {
            piesCopy = it.toMutableList()
        }
//        updateMeasure()
        ss.pieSelected?.let {
            pieSelected = it
//            if(rect.width() > 0){
//                pieSelected!!.updateMeasure(rect, newRect)
//            }
        }
        ss.paddingPie?.let {
            paddingPie = it
        }
        ss.lastIndicePieGot?.let {
            lastIndicePieGot = it
        }

        ss.forceStopAnimation?.let{
            forceStopAnimation = it
        }

        ss.uchiwaEnum?.let {
            currentStep = it
            if (currentStep == UchiwaEnum.CLOSING_STEP) {
                runClosingAnimation()
            } else if (currentStep == UchiwaEnum.OPENNING_STEP) {
                runOpeningAnimation()
            }
        }
    }

    companion object {
        // PERCENT
        private const val MAX_SIZE_CAMEMBERT = 1f

        private const val POSITION_HELP_CIRCLE = 130
                private const val TIME_REFRESH = 15L
//        private const val TIME_REFRESH = 350L

        private const val QUART_ANGLE = 90f

        const val UCHIWA_ANGLE: Float = 30f
        const val ALF_DEGREE: Float = 180f
        const val UCHIWA_RADIUS: Float = UCHIWA_ANGLE / 2
        const val TOP_DEGREE: Float = 270f
        const val MAX_DEGREE: Float = 360f
        const val BOTTOM_DEGREE: Float = 90f
        const val START_DEGREE: Float = TOP_DEGREE + UCHIWA_RADIUS
        const val LAST_DEGREE: Float = BOTTOM_DEGREE - UCHIWA_RADIUS

        const val UCHIWA_FINAL_DEGREE = ALF_DEGREE - UCHIWA_ANGLE
    }
}

enum class UchiwaEnum {
    SELECTION_STEP, // L'evantail propose son panel d'option
    OPENNING_STEP, // l'evantail est en train de s'ouvrir (CLOSED_STEP -> SELECTION_STEP)
    CLOSING_STEP, // l'evantail est en train de se fermer (SELECTION_STEP -> CLOSED_STEP)
    CLOSED_STEP, // L'evantail n'affiche plus qu'une seule et unique option (celle que l'utilisateur a sélectionné)
    SELECTING_STEP, // L'utilisateur a toujours le doigt sur un element
    SELECTING_CLOSED_STEP, // L'utilisateur a le doigt sur l'ecran mais a qu'un seul element d'affiche
}