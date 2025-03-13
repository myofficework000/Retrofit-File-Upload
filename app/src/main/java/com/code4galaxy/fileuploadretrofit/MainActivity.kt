package com.code4galaxy.fileuploadretrofit

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.code4galaxy.fileuploadretrofit.databinding.ActivityMainBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MainActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private val clientId = "Client-ID 843c11ac969867c"
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissions()

        binding.imageView.setOnClickListener {
            pickImage()
        }

        binding.uploadButton.setOnClickListener {
            selectedImageUri?.let { uri ->
                uploadImage(uri)
            } ?: Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, neededPermissions.toTypedArray(), 100)
        }
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                Glide.with(this).load(it).into(binding.imageView)
            }
        }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun uploadImage(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE

        val file = File(getRealPathFromURI(uri))
        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestBody)

        ImgurApi.service.uploadImage(clientId, multipartBody)
            .enqueue(object : Callback<ImgurUploadJson> {
                override fun onResponse(
                    call: Call<ImgurUploadJson>,
                    response: Response<ImgurUploadJson>
                ) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.success == true) {
                        val imageUrl = response.body()?.getImageLink()
                        Toast.makeText(
                            this@MainActivity,
                            "Uploaded Successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        Glide.with(this@MainActivity).load(imageUrl).into(binding.imageView)
                    } else {
                        Toast.makeText(this@MainActivity, "Upload Failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<ImgurUploadJson>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun getRealPathFromURI(contentUri: Uri): String {
        var result = ""
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                result = it.getString(columnIndex)
            }
            it.close()
        }
        return result
    }
}
