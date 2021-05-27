package com.francosoft.firebaseuploadexample

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.francosoft.firebaseuploadexample.databinding.ActivityImagesBinding
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ImagesActivity : AppCompatActivity(), ImageAdapter.OnItemClickListener{
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var progressImgDownload: ProgressBar
    private var uploads: MutableList<Upload> = arrayListOf()

    private lateinit var databaseRef: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var dBListener: ValueEventListener

    private val binding: ActivityImagesBinding by lazy {
        ActivityImagesBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            this@ImagesActivity.recyclerView = recyclerView
            this@ImagesActivity.progressImgDownload = progressImgDownload
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        imageAdapter = ImageAdapter(this@ImagesActivity)
        recyclerView.adapter = imageAdapter
        imageAdapter.setOnItemClickListener(this@ImagesActivity)

        storage = FirebaseStorage.getInstance()
        databaseRef = FirebaseDatabase.getInstance().getReference("uploads")

        dBListener = databaseRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                uploads.clear()

                for (postSnapshot: DataSnapshot in snapshot.children){
                    val upload: Upload = postSnapshot.getValue(Upload::class.java) as Upload
                    upload.key = postSnapshot.key
                    uploads.add(upload)
                }

                imageAdapter.submitList(uploads)
                imageAdapter.notifyDataSetChanged()

                progressImgDownload.visibility = View.INVISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ImagesActivity, error.message, Toast.LENGTH_SHORT).show()
                progressImgDownload.visibility = View.INVISIBLE
            }

        })
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(this, "Normal Click at position: $position", Toast.LENGTH_SHORT).show()
    }

    override fun onWhatEverClick(position: Int) {
        Toast.makeText(this, "Whatever Click at position: $position", Toast.LENGTH_SHORT).show()

    }

    override fun onDeleteClick(position: Int) {
        val selectedItem: Upload = uploads[position]
        val selectedKey = selectedItem.key

        val imgRef:  StorageReference = storage.getReferenceFromUrl(selectedItem.imageUrl!!)
        imgRef.delete().addOnSuccessListener {
            if (selectedKey != null) {
                databaseRef.child(selectedKey).removeValue()
                imageAdapter.notifyItemRemoved(position)
                Toast.makeText(this, "Item deleted!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        databaseRef.removeEventListener(dBListener)
    }
}