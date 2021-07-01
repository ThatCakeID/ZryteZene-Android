package com.thatcakeid.zrytezene.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.thatcakeid.zrytezene.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class HomeItemsRecyclerViewAdapter extends RecyclerView.Adapter<HomeItemsRecyclerViewAdapter.ViewHolder> {

    private ArrayList<HashMap<String, Object>> items;
    private HashMap<String, String> users;
    private Context mContext;

    private static ClickListener clickListener;

    public HomeItemsRecyclerViewAdapter(Context mContext, ArrayList<HashMap<String, Object>> items,
                                        HashMap<String, String> users) {
        this.mContext = mContext;
        this.items = items;
        this.users = users;
    }

    public void updateItems(Context mContext, ArrayList<HashMap<String, Object>> items,
                            HashMap<String, String> users) {
        this.mContext = mContext;
        this.items = items;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.musicname_item.setText(items.get(position).get("title").toString());
        holder.uploader_item.setText(users.containsKey(items.get(position).get("author").toString()) ?
                users.get(items.get(position).get("author").toString()) :
                items.get(position).get("author").toString());
        holder.date_text.setText(new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss")
                .format(((Timestamp)items.get(position).get("time")).toDate()));
        if (items.get(position).get("thumb").toString().equals("")) {
            holder.image_overlay.setImageResource(R.drawable.ic_zrytezene);
        } else {
             Glide.with(mContext).load(items.get(position).get("thumb").toString())
                     .into(holder.image_overlay);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView image_overlay;
        TextView musicname_item;
        TextView uploader_item;
        TextView date_text;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            image_overlay = itemView.findViewById(R.id.image_overlay);
            musicname_item = itemView.findViewById(R.id.musicname_item);
            uploader_item = itemView.findViewById(R.id.uploader_item);
            date_text = itemView.findViewById(R.id.date_text);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }

        @Override
        public boolean onLongClick(View v) {
            clickListener.onItemLongClick(getAdapterPosition(), v);
            return false;
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        HomeItemsRecyclerViewAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }
}
