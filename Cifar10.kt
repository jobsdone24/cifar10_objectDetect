package com.example.choison_project

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.choison_project.databinding.ActivityCifar10Binding
import com.example.choison_project.ml.Cifar10
import com.example.choison_project.ml.ConvertedModel
import com.example.choison_project.ml.Shlast
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class Cifar10 : AppCompatActivity() {
    val paint = Paint()
    lateinit var btn: Button
    lateinit var imageView: ImageView
    lateinit var bitmap: Bitmap
    var colors = listOf<Int>(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
        Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
    )
    lateinit var labels: List<String>
    lateinit var model1: ConvertedModel
    lateinit var model2: Cifar10
    lateinit var model3: Shlast
    val imageProcessor =
        ImageProcessor.Builder().add(ResizeOp(32, 32, ResizeOp.ResizeMethod.BILINEAR)).build()
    private lateinit var binding: ActivityCifar10Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCifar10Binding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        labels = FileUtil.loadLabels(this, "cifarlabels.txt")
        model1 = ConvertedModel.newInstance(this)
        model2 = Cifar10.newInstance(this)
        model3 = Shlast.newInstance(this)

        paint.setColor(Color.BLUE)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5.0f
//        paint.textSize = paint.textSize*3

        Log.d("labels", labels.toString())

        val intent = Intent()
        intent.setAction(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")

        btn = findViewById(R.id.btn)
        imageView = findViewById(R.id.imaegView)

        btn.setOnClickListener {
            startActivityForResult(intent, 101)
        }
        binding.button1.setOnClickListener {
            get_predictions(1)
        }

        binding.button2.setOnClickListener {
            get_predictions(2)
        }

        binding.button3.setOnClickListener {
            get_predictions(3)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            var uri = data?.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            imageView.setImageBitmap(bitmap)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        model1.close()
        model2.close()
    }

    fun get_predictions(buttonIndex: Int) {
        var image = TensorImage(DataType.FLOAT32)
        image.load(bitmap)
        image = imageProcessor.process(image)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 32, 32, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(image.getBuffer())

        var outputFeature0: TensorBuffer? = null
        when (buttonIndex) {
            1 -> {
                val outputs = model1.process(inputFeature0)
                outputFeature0 = outputs.outputFeature0AsTensorBuffer
            }

            2 -> {
                val outputs = model2.process(inputFeature0)
                outputFeature0 = outputs.outputFeature0AsTensorBuffer
            }

            3 ->{
                val outputs = model3.process(inputFeature0)
                outputFeature0 = outputs.outputFeature0AsTensorBuffer
            }
            else -> {
                val outputs = model2.process(inputFeature0)
                outputFeature0 = outputs.outputFeature0AsTensorBuffer
            }
        }

        Log.d("imtesting", outputFeature0.floatArray.contentToString())
        Log.d("imtesting", labels[getMax(outputFeature0.floatArray)])

        imageView.setImageBitmap(bitmap)
        binding.resultbtn.text = labels[getMax(outputFeature0.floatArray)]
    }

    fun getMax(arr: FloatArray): Int {
        var max = 0
        for (i in arr.indices) {
            if (arr[i] > arr[max]) {
                max = i
            }
        }
        return max
    }
}
