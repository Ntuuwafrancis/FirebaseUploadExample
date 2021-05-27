package com.francosoft.firebaseuploadexample

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.MimeTypeMap
import android.widget.*
import com.bumptech.glide.Glide
import com.francosoft.firebaseuploadexample.databinding.ActivityMainBinding
import com.google.android.gms.common.util.concurrent.HandlerExecutor
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private val TAG = MainActivity::class.simpleName
    }

    private lateinit var btnChooseImg: Button
    private lateinit var btnUpload: Button
    private lateinit var tvShowUploads: TextView
    private lateinit var etFileName: EditText
    private lateinit var progressBar: ProgressBar
    private var imageUri: Uri? = null

    private lateinit var storageRef: StorageReference
    private lateinit var databaseRef: DatabaseReference

    private  var uploadTask: StorageTask<UploadTask.TaskSnapshot>? = null

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.apply {
            this@MainActivity.btnChooseImg = btnChooseImg
            this@MainActivity.btnUpload = btnUpload
            this@MainActivity.tvShowUploads = tvShowUploads
            this@MainActivity.etFileName = etFileName
            this@MainActivity.progressBar = progressBar
        }

        storageRef = FirebaseStorage.getInstance().getReference("uploads")
        databaseRef = FirebaseDatabase.getInstance().getReference("uploads")

        btnChooseImg.setOnClickListener {
            openFileChooser()
        }

        btnUpload.setOnClickListener {
            if (uploadTask != null && uploadTask!!.isInProgress) {
                Toast.makeText(this, "Upload in progress", Toast.LENGTH_SHORT).show()
            } else if (imageUri == null){
                Toast.makeText(this, "Choose image to upload", Toast.LENGTH_SHORT).show()
            }
            else {
                uploadFile()
            }

        }

        tvShowUploads.setOnClickListener {
            openImagesActivity()
        }
    }

    private fun openImagesActivity() {
        val intent = Intent(this, ImagesActivity::class.java)
        startActivity(intent)
    }

    private fun getFileExtension(uri: Uri): String? {

        val cR: ContentResolver = contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    private fun uploadFile() {

        val currentTime = System.currentTimeMillis()
        val fileExt = imageUri?.let { getFileExtension(it) }
        val fileReference: StorageReference = storageRef.child("$currentTime.$fileExt")

        uploadTask = imageUri?.let {imageUploadUri ->
            fileReference.putFile(imageUploadUri)
                .addOnSuccessListener {

                    val handler = Handler(Looper.myLooper() ?: return@addOnSuccessListener)
                    handler.postDelayed(
                        { progressBar.progress = 0 },
                        500
                    )
                    Toast.makeText(this, "upload successful", Toast.LENGTH_LONG).show()

                    // Adding Image Upload info to the database
                    val fileName = if(etFileName.text.toString().trim().isEmpty()){
                        "No Name"
                    } else {
                        etFileName.text.toString().trim()
                    }

                    fileReference.downloadUrl.addOnSuccessListener {imageDownloadUri ->
                        val upload = Upload(
                            fileName,
                            imageDownloadUri.toString()
                            //                    fileReference.downloadUrl.toString()
                        )
                        val uploadId = databaseRef.push().key
                        if (uploadId != null) {
                            databaseRef.child(uploadId).setValue(upload)
                        }
                    }

                }
                .addOnFailureListener {
                    Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
                }
                .addOnProgressListener {
                    val progress = (100.0 * it.bytesTransferred / it.totalByteCount)
                    /**
                     * ProgressDialog is deprecated because it blocks the main thread
                     */
                    progressBar.progress = progress.toInt()
                }
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.data != null) {
            data.data?.let {
                imageUri = it
            }

            Glide.with(this)
                .load(imageUri)
                .into(binding.imgView)

        }
    }
}