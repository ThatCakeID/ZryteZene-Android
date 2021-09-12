package com.thatcakeid.zrytezene.adapters

import android.content.Context
import com.thatcakeid.zrytezene.HelperClass.Companion.getPrettyPlaysCount
import com.thatcakeid.zrytezene.HelperClass.Companion.getPrettyDateFormat
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.HelperClass
import com.bumptech.glide.Glide
import android.view.View.OnLongClickListener
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.Timestamp
import com.thatcakeid.zrytezene.adapters.HomeItemsRecyclerViewAdapter
import com.thatcakeid.zrytezene.adapters.HomeItemsRecyclerViewAdapter.ClickListener
import java.util.ArrayList
import java.util.HashMap

class HomeItemsRecyclerViewAdapter(
    private var mContext: Context, private var items: ArrayList<HashMap<String, Any>>,
    private var users: HashMap<String, String?>
) : RecyclerView.Adapter<HomeItemsRecyclerViewAdapter.ViewHolder>() {
    fun updateItems(
        mContext: Context, items: ArrayList<HashMap<String, Any>>,
        users: HashMap<String, String?>
    ) {
        this.mContext = mContext
        this.items = items
        this.users = users
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bottomText =
            (if (users.containsKey(items[position]["author"])) users[items[position]["author"]] else items[position]["author"]).toString() + " • " +
                    getPrettyPlaysCount(items[position]["plays"] as Number?) + " • " +
                    getPrettyDateFormat((items[position]["time"] as Timestamp?)!!)
        holder.musicname_item.text = items[position]["title"] as String?
        holder.uploader_item.text = bottomText
        if (items[position]["thumb"] == "") {
            holder.image_overlay.setImageResource(R.drawable.ic_zrytezene)
        } else {
            Glide.with(mContext).load(items[position]["thumb"] as String?)
                .into(holder.image_overlay)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener,
        OnLongClickListener {
        var image_overlay: ImageView
        var musicname_item: TextView
        var uploader_item: TextView
        override fun onClick(v: View) {
            clickListener!!.onItemClick(adapterPosition, v)
        }

        override fun onLongClick(v: View): Boolean {
            clickListener!!.onItemLongClick(adapterPosition, v)
            return false
        }

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            image_overlay = itemView.findViewById(R.id.image_overlay)
            musicname_item = itemView.findViewById(R.id.musicname_item)
            uploader_item = itemView.findViewById(R.id.uploader_item)
        }
    }

    fun setOnItemClickListener(clickListener: ClickListener?) {
        Companion.clickListener = clickListener
    }

    interface ClickListener {
        fun onItemClick(position: Int, v: View?)
        fun onItemLongClick(position: Int, v: View?)
    }

    companion object {
        private var clickListener: ClickListener? = null
    }
}