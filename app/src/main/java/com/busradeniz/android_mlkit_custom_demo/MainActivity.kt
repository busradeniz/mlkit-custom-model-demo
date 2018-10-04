package com.busradeniz.android_mlkit_custom_demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), InterpreterListener {

    private val interpreter = NumberInterpreter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setClickListeners()
    }

    private fun setClickListeners() {
        btn_detect.setOnClickListener {
            val image = fpv_paint.exportToBitmap(
                    NumberInterpreter.IMG_WIDTH, NumberInterpreter.IMG_HEIGHT)

            interpreter.interpret(image, this)
        }
    }

    override fun onSuccess(result: String, probability: Float) {
        txt_result.text = "${result} (p: ${probability})"
        fpv_paint.clear()
    }

    override fun onFail(e : Exception) {
        txt_result.text = e.message
        e.printStackTrace()
        fpv_paint.clear()
    }

}
