package me.bytebeats.views.nestedscrolling.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import me.bytebeats.views.nestedscrolling.app.R

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/9 20:06
 * @Version 1.0
 * @Description TO-DO
 */

class ImageAdapter(private val context: Context) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    private val resources = intArrayOf(
        R.color.black,
        R.color.design_default_color_background,
        R.color.purple_200,
        R.color.purple_500,
        R.color.purple_700
    )

    private val data = mutableListOf<Int>()

    fun add(list: List<Int>) {
        data.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_image, parent, false))
    }

    @DrawableRes
    fun getRes(position: Int): Int = resources[data[position] % resources.size]

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getRes(position))
    }

    override fun getItemCount(): Int = data.size

    class ImageViewHolder(
        private val view: View
    ) : RecyclerView.ViewHolder(view) {
        private val image = view.findViewById<ImageView>(R.id.image)

        fun bind(@DrawableRes res: Int) {
            image.setImageResource(res)
        }

    }
}