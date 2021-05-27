package com.francosoft.firebaseuploadexample

import android.content.Context
import android.view.*
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.francosoft.firebaseuploadexample.databinding.ImageItemBinding

class ImageAdapter(private val context: Context) :
    ListAdapter<Upload, ImageAdapter.ViewHolder>(DIFF_CALLBACK) {
    private lateinit var  onItemClickListener : OnItemClickListener

    companion object{
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<Upload> = object : DiffUtil.ItemCallback<Upload>() {
            override fun areItemsTheSame(oldItem: Upload, newItem: Upload): Boolean {
                return oldItem.imageUrl == newItem.imageUrl
            }

            override fun areContentsTheSame(oldItem: Upload, newItem: Upload): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ImageItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
//        holder.itemView.setOnClickListener {
//            if (position != RecyclerView.NO_POSITION) {
//                onItemClickListener.onItemClick(position)
//            }
//        }
    }

    inner class ViewHolder(private val binding: ImageItemBinding) : RecyclerView.ViewHolder(binding.root),
        MenuItem.OnMenuItemClickListener {

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(position)
                }
            }

            itemView.setOnCreateContextMenuListener { menu, v, menuInfo ->
                menu.setHeaderTitle("Select Action")
                val doWhatever = menu.add(Menu.NONE, 1, 1, "Do whatever")
                val delete = menu.add(Menu.NONE, 2, 2, "Delete")

                doWhatever.setOnMenuItemClickListener(this)
                delete.setOnMenuItemClickListener(this)
            }
        }

        fun bindTo(upload: Upload) {
            Glide.with(context)
                .load(upload.imageUrl)
                .placeholder(R.drawable.image_placeholder)
                .fitCenter()
                .into(binding.imgViewUpload)
            binding.txtViewName.text = upload.name

        }

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                if (item != null) {
                    when(item.itemId){
                        1 -> onItemClickListener.onWhatEverClick(position)
                        2 -> onItemClickListener.onDeleteClick(position)
                    }
                }
            }

            return false
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)

        fun onWhatEverClick(position: Int)

        fun onDeleteClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }




}