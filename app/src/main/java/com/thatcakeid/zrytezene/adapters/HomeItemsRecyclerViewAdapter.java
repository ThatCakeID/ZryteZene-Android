package com.thatcakeid.zrytezene.adapters;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.thatcakeid.zrytezene.R;
import com.thatcakeid.zrytezene.models.HomeItem;
import com.thatcakeid.zrytezene.models.HomeItemMusic;
import com.thatcakeid.zrytezene.models.HomeItemTitle;

import java.util.ArrayList;

public class HomeItemsRecyclerViewAdapter extends RecyclerView.Adapter<HomeItemsRecyclerViewAdapter.ViewHolder> {

    private ArrayList<HomeItem> items;

    public HomeItemsRecyclerViewAdapter(ArrayList<HomeItem> items) {
        this.items = items;
    }

    public void updateItems(ArrayList<HomeItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomeItem homeItem = items.get(position);

        if (homeItem instanceof HomeItemTitle) {
            // Hide the music
            holder.music_item.setVisibility(View.GONE);

            holder.title_text.setText(((HomeItemTitle) homeItem).title);
            holder.subtitle_text.setText(((HomeItemTitle) homeItem).subtitle);

        } else if (homeItem instanceof HomeItemMusic) {
            // Hide the title
            holder.title_item.setVisibility(View.GONE);

            // ((HomeItemMusic) homeItem).music_id;

            holder.music_name.setText(((HomeItemMusic) homeItem).music_name);
            holder.music_uploader.setText(((HomeItemMusic) homeItem).uploader_name);

            holder.music_upload_date.setText(((HomeItemMusic) homeItem).upload_date);

            holder.music_comment_count.setText(((HomeItemMusic) homeItem).comments_count);

            if ((((HomeItemMusic) homeItem).isLiked)) {
                // TODO: DO THIS
                // holder.music_like.setImageTintList(new ColorStateList());
            }

            if (((HomeItemMusic) homeItem).isDisliked) {
                // TODO: ALSO DO THIS
                // holder.music_dislike.setImageTintList(new ColorStateList());
            }

            // TODO: ADD TOTAL LIKES TEXTVIEW
            // holder.music_total_likes.setText(((HomeItemMusic) homeItem).total_likes);

            // uhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh
            holder.music_top_part.setBackground(
                    new BitmapDrawable(
                            ((HomeItemMusic) homeItem).music_image
                    )
            );
        }
        // else { /* Impossible */ }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Music Item ==============================================================================
        ViewGroup music_item;

        TextView music_name;
        TextView music_uploader;

        TextView music_upload_date;
        TextView music_comment_count;

        ImageView music_like;

        ImageView music_comment;

        ConstraintLayout music_top_part;  // Placeholder for the music image
        // Music Item ==============================================================================

        // Title Item ==============================================================================
        ViewGroup title_item;

        TextView title_text;
        TextView subtitle_text;
        // Title Item ==============================================================================

        public ViewHolder(View itemView) {
            super(itemView);

            // Music Item ==============================================================================
            music_item = itemView.findViewById(R.id.item_music);
            music_name = itemView.findViewById(R.id.musicname_item);
            music_uploader = itemView.findViewById(R.id.uploader_item);

            music_upload_date = itemView.findViewById(R.id.textView5);
            music_comment_count = itemView.findViewById(R.id.textView4);

            music_like = itemView.findViewById(R.id.like_item);

            music_comment = itemView.findViewById(R.id.comment_item);

            music_top_part = itemView.findViewById(R.id.item_top_part);

            // Title Item ==============================================================================
            title_item = itemView.findViewById(R.id.item_title);

            title_text = itemView.findViewById(R.id.title_text_item);
            subtitle_text = itemView.findViewById(R.id.subtitle_text_item);
        }
    }
}
