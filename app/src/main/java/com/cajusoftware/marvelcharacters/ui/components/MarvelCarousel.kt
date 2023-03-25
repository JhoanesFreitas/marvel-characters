package com.cajusoftware.marvelcharacters.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.helper.widget.Carousel
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.R
import kotlin.math.max

@SuppressLint("Recycle", "CustomViewStyleable")
class MarvelCarousel(context: Context, attrs: AttributeSet) : Carousel(context, attrs) {

    private var nextState = -1
    private var previousState = -1

    init {
        val marvelAttrs = context.obtainStyledAttributes(attrs, R.styleable.Carousel)
        val indexCount = marvelAttrs.indexCount
        for (i in 0 until indexCount) {
            when (val attr = marvelAttrs.getIndex(i)) {
                R.styleable.Carousel_carousel_nextState -> {
                    this.nextState = marvelAttrs.getResourceId(attr, nextState)
                }
                R.styleable.Carousel_carousel_previousState -> {
                    previousState = marvelAttrs.getResourceId(attr, previousState)
                }
            }
        }
    }

    override fun transitionToIndex(index: Int, delay: Int) {
        val lastItem = count - 1

        when (currentIndex) {
            0 -> if (index == 1) (parent as? MotionLayout)?.transitionToState(
                nextState,
                max(0, delay)
            ) else {
                (parent as? MotionLayout)?.transitionToState(
                    previousState,
                    max(0, delay)
                )
            }
            lastItem -> if (index == lastItem - 1) (parent as? MotionLayout)?.transitionToState(
                previousState,
                max(0, delay)
            ) else {
                (parent as? MotionLayout)?.transitionToState(
                    nextState,
                    max(0, delay)
                )
            }
            else -> {
                if (index < currentIndex) {
                    (parent as? MotionLayout)?.transitionToState(previousState, max(0, delay))
                } else {
                    (parent as? MotionLayout)?.transitionToState(nextState, max(0, delay))
                }
            }
        }
    }
}