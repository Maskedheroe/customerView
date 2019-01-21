package com.example.hou.tmepView

import android.animation.TypeEvaluator

public class ReverseEvaluator : TypeEvaluator<Int>{
    override fun evaluate(fraction: Float, startValue: Int?, endValue: Int?): Int {
        val startInt  = startValue
        return (endValue!! - fraction * (endValue - startInt!!)).toInt()
    }

}