package com.busradeniz.android_mlkit_custom_demo

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.util.Log
import com.google.firebase.ml.custom.*
import com.google.firebase.ml.custom.model.FirebaseCloudModelSource
import com.google.firebase.ml.custom.model.FirebaseLocalModelSource
import com.google.firebase.ml.custom.model.FirebaseModelDownloadConditions


class NumberInterpreter {

    companion object {
        private const val KERAS_MODEL = "tf-first-project-keras"
        private const val GOOGLE_MNIST = "tf-first-project"
        private const val SIMPLIFIED_MNIST = "tf-first-project-pre-build"
        private const val CURRENT_MODEL = SIMPLIFIED_MNIST

        const val CLASS_COUNT = 10
        const val IMG_WIDTH = 28
        const val IMG_HEIGHT = 28
    }

    private var interpreter: FirebaseModelInterpreter?
    private val options = FirebaseModelInputOutputOptions.Builder()
            .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, IMG_WIDTH, IMG_HEIGHT, 1))
            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, CLASS_COUNT))
            .build()
    private val imagePixels = IntArray(IMG_WIDTH * IMG_HEIGHT)

    init {
        createLocalSource()
        createRemoteSource()

        val options = FirebaseModelOptions.Builder()
                .setCloudModelName(CURRENT_MODEL)
//                .setLocalModelName(CURRENT_MODEL)
                .build()

        interpreter = FirebaseModelInterpreter.getInstance(options)

    }

    fun interpret(bitmap: Bitmap, listener: InterpreterListener) {
        val inputs = FirebaseModelInputs.Builder()
                .add(convertToVector(bitmap))
                .build()

        interpreter?.let {
            it.run(inputs, options)
                    .addOnSuccessListener { output ->
                        output.map().entries.maxBy { it.value }?.also {
                            listener.onSuccess(it.key.toString(), it.value)
                        }
                    }
                    .addOnFailureListener { e ->
                        listener.onFail(e)
                    }
        }

    }

    private fun convertToVector(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        bitmap.getPixels(imagePixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return Array(1) {
            Array(IMG_HEIGHT) { y ->
                Array(IMG_WIDTH) { x ->
                    floatArrayOf(imagePixels[x + (y * IMG_WIDTH)].convertToGreyScale())
                }
            }
        }
    }

    private fun Int.convertToGreyScale(): Float =
            1f - ((Color.red(this) + Color.green(this) + Color.blue(this)).toFloat() / 3f / 255f)

    private fun createRemoteSource() {
        val initialConditions = FirebaseModelDownloadConditions.Builder()
                .build()

        var conditionsBuilder: FirebaseModelDownloadConditions.Builder = FirebaseModelDownloadConditions.Builder().requireWifi()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conditionsBuilder = conditionsBuilder
                    .requireCharging()
                    .requireDeviceIdle()
        }
        val updateConditions = conditionsBuilder.build()

        val cloudSource = FirebaseCloudModelSource.Builder(CURRENT_MODEL)
                .enableModelUpdates(true)
                .setInitialDownloadConditions(initialConditions)
                .setUpdatesDownloadConditions(updateConditions)
                .build()

        FirebaseModelManager.getInstance().registerCloudModelSource(cloudSource)

    }

    private fun createLocalSource() {
        val localSource = FirebaseLocalModelSource.Builder(CURRENT_MODEL)
                .setAssetFilePath(CURRENT_MODEL)
                .build()

        FirebaseModelManager.getInstance().registerLocalModelSource(localSource)
    }

    private fun FirebaseModelOutputs.map(): Map<Int, Float> {
        return getOutput<Array<FloatArray>>(0)[0].mapIndexed { index, fl -> index to fl }.toMap()
    }
}