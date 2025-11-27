package com.coljuegos.sivo.ui.common

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

class SignatureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val guidePaint = Paint().apply {
        color = Color.LTGRAY
        isAntiAlias = true
        strokeWidth = 2f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val path = Path()
    private val bitmap: Bitmap by lazy {
        createBitmap(width, height)
    }
    private val canvas: Canvas by lazy { Canvas(bitmap) }

    private var hasSignature = false

    init {
        setBackgroundColor(Color.WHITE)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dibuja la línea guía (no se guarda en el bitmap)
        val guideY = height * 0.6f
        canvas.drawLine(0f, guideY, width.toFloat(), guideY, guidePaint)

        // Dibuja la firma desde el bitmap
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        // Dibuja el path actual
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                hasSignature = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
            }
            MotionEvent.ACTION_UP -> {
                canvas.drawPath(path, paint)
                path.reset()
            }
            else -> return false
        }

        invalidate()
        return true
    }

    fun clear() {
        path.reset()
        bitmap.eraseColor(Color.TRANSPARENT)
        // No limpiar el canvas completamente para mantener el background blanco
        hasSignature = false
        invalidate()
    }

    fun hasSignature(): Boolean = hasSignature

    fun getSignatureBitmap(): Bitmap {
        val resultBitmap = createBitmap(width, height)
        val resultCanvas = Canvas(resultBitmap)
        resultCanvas.drawColor(Color.WHITE)
        resultCanvas.drawBitmap(bitmap, 0f, 0f, null)
        return resultBitmap
    }

    fun setSignatureBitmap(signatureBitmap: Bitmap) {
        // Validar que el view tenga dimensiones antes de escalar
        if (width <= 0 || height <= 0) {
            // Si el view no tiene dimensiones, esperar a que las tenga
            post {
                if (width > 0 && height > 0) {
                    setSignatureBitmap(signatureBitmap)
                }
            }
            return
        }

        val scaledBitmap = signatureBitmap.scale(width, height)
        canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
        hasSignature = true
        invalidate()
    }

}