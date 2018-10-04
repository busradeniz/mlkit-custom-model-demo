package com.busradeniz.android_mlkit_custom_demo

interface InterpreterListener {

    fun onSuccess(result: String, probability: Float)

    fun onFail(e: Exception)
}
