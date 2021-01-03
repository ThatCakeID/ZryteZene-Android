package com.thatcakeid.zrytezene.models;

import android.graphics.Bitmap;

public class HomeItemMusic extends HomeItem {
    public String music_id;

    public String music_name;
    public String uploader_name;

    public String upload_date;

    public int comments_count;

    public boolean isLiked;
    public boolean isDisliked;

    public int total_likes;

    public Bitmap music_image;
}
