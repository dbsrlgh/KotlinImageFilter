package com.example.kotlinimagefilter.Interface

interface EditImageFragmentListener {
    fun onBrightnessChanged(brightness:Int)
    fun onSaturationChanged(saturation:Float)
    fun onContrastChanged(contrast:Float)
    fun onEditStarted()
    fun onEditCompleted()
}