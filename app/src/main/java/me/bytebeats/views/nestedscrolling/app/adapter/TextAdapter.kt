package me.bytebeats.views.nestedscrolling.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import me.bytebeats.views.nestedscrolling.app.R

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/9 19:29
 * @Version 1.0
 * @Description TO-DO
 */

class TextAdapter(private val context: Context) : BaseAdapter() {
    private val data = mutableListOf<String>()

    fun set(list: List<String>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    fun add(list: List<String>) {
        data.addAll(list)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = data.size

    override fun getItem(position: Int): String = data[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View
        var holder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_text, null)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }
        holder.bind(getItem(position))
        return view
    }

    private class ViewHolder(val view: View) {
        private val text = view.findViewById<TextView>(R.id.text)

        fun bind(data: String) {
            text.text = data
        }
    }
}