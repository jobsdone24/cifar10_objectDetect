package com.example.choison_project

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.choison_project.databinding.ActivityGalleryBinding
import com.example.choison_project.ml.AutoModel1
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GalleryActivity : AppCompatActivity() {
    val paint = Paint()
    lateinit var btn: Button
    lateinit var imageView: ImageView
    lateinit var bitmap: Bitmap
    private lateinit var currentPhotoPath: String
    var colors = listOf<Int>(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY,Color.BLACK,
        Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED)
    lateinit var labels: List<String>
    lateinit var model: AutoModel1
    val imageProcessor = ImageProcessor.Builder().add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)).build()
    private lateinit var binding :ActivityGalleryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        labels = FileUtil.loadLabels(this, "labels.txt")
        model =  AutoModel1.newInstance(this)

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
        binding.photobtn.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
           startActivity(takePictureIntent)
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 101){
            var uri = data?.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            get_predictions()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    fun get_predictions(){

        var image = TensorImage.fromBitmap(bitmap)
        image = imageProcessor.process(image)
        val outputs = model.process(image)
        val locations = outputs.locationsAsTensorBuffer.floatArray
        val classes = outputs.classesAsTensorBuffer.floatArray
        val scores = outputs.scoresAsTensorBuffer.floatArray
        val numberOfDetections = outputs.numberOfDetectionsAsTensorBuffer.floatArray
        val indexesToCheck = setOf(6, 4, 15, 16, 17, 18, 10, 11)


        val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        var canvas = Canvas(mutable)
        var h = mutable.height
        var w = mutable.width


        paint.textSize = h/20f


        paint.strokeWidth = h/110f
        scores.forEachIndexed { index, fl ->
            if(fl > 0.65 && classes.get(index).toInt() in indexesToCheck){
                var x = index
                x *= 4
                paint.setColor(colors.get(index))
                paint.style = Paint.Style.STROKE
                canvas.drawRect(RectF(locations.get(x+1)*w, locations.get(x)*h, locations.get(x+3)*w, locations.get(x+2)*h), paint)
                paint.style = Paint.Style.FILL
                canvas.drawText(labels[classes.get(index).toInt()] + " " + (fl/2+0.5).toString(), locations.get(x+1)*w, locations.get(x)*h- h/30f, paint)
            }
        }

        imageView.setImageBitmap(mutable)

    }
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.your.package.name.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

}
private val REQUEST_IMAGE_CAPTURE = 101