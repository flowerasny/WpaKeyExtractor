package com.kiksoft.ml

import android.R.attr.label
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSelectImage.setOnClickListener {
            Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            }.let {
                startActivityForResult(Intent.createChooser(it, "Select Picture"), PICK_IMAGE)
            }
        }

        btnCopy.setOnClickListener {
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("WPA-key", tvWpaKey.text))
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            ivPreview.setImageURI(data!!.data!!)

            val recognizer = TextRecognition.getClient()
            recognizer.process(InputImage.fromFilePath(this, data.data!!))
                    .addOnSuccessListener {
                        val results = it.textBlocks.mapNotNull {
                            it.lines.firstOrNull { it.text.contains("WPA") }
                        }
                        if(results.isNotEmpty()){
                            tvWpaKey.text = results.first().text.clearWpaKey()
                            btnCopy.visibility = VISIBLE
                        } else{
                            tvWpaKey.text = "No WPA key here! Try different orientation!"
                            btnCopy.visibility = GONE
                        }
                    }
                    .addOnFailureListener {
                        Log.d("FoundText", it.message!!)
                    }
        }
    }

    companion object {
        const val PICK_IMAGE = 1
    }
}

fun String.clearWpaKey() = this.replace(" ", "")
        .takeLast(26)