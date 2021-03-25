package com.thatcakeid.zrytezene.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thatcakeid.zrytezene.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class HomeItemsRecyclerViewAdapter extends RecyclerView.Adapter<HomeItemsRecyclerViewAdapter.ViewHolder> {

    private ArrayList<HashMap<String, Object>> items;
    private HashMap<String, String> users, comments_count;

    public HomeItemsRecyclerViewAdapter(ArrayList<HashMap<String, Object>> items,
                                        HashMap<String, String> users, HashMap<String, String> comments_count) {
        this.items = items;
        this.users = users;
        this.comments_count = comments_count;
    }

    public void updateItems(ArrayList<HashMap<String, Object>> items,
                            HashMap<String, String> users, HashMap<String, String> comments_count) {
        this.items = items;
        this.users = users;
        this.comments_count = comments_count;
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
        //holder.uploader_item.setText(users.get(items.get(position).get("author").toString())
        //        .isEmpty() ? "Deleted User" : users.get(items.get(position).get("author").toString()));
        holder.uploader_item.setText(items.get(position).get("author").toString());

        //holder.date_text.setText(new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss")
        //        .format(items.get(position).get("time")));
        //holder.comments_count_text.setText(comments_count.get(items.get(position).get("title").toString()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Items inside 'item_top_part'
        ImageView image_overlay;
        TextView musicname_item;
        TextView uploader_item;

        // Outer items
        ImageView fav_item;
        TextView date_text;
        TextView comments_count_text;
        ImageView comment_item;

        public ViewHolder(View itemView) {
            super(itemView);

            // Items inside 'item_top_part'
            image_overlay = itemView.findViewById(R.id.image_overlay);
            musicname_item = itemView.findViewById(R.id.musicname_item);
            uploader_item = itemView.findViewById(R.id.uploader_item);

            // Outer items
            fav_item = itemView.findViewById(R.id.fav_item);
            date_text = itemView.findViewById(R.id.date_text);
            comments_count_text = itemView.findViewById(R.id.comments_count_text);
            comment_item = itemView.findViewById(R.id.comment_item);
        }
    }
}
