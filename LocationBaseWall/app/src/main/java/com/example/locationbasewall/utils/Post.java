package com.example.locationbasewall.utils;

// 帖子类
public class Post {
    private String id;
    private String uid;
    private String username;
    private String imageUrl; // 头像链接
    private String title;
    private String text;
    private int content_type;
    private String mediaUrl;
    private String date;
    private double location_x;
    private double location_y;
    private String address;

    private String distance;

    private byte[] imageData;

    public Post(String id, String uid, String username, String imageUrl, String title, String text, int content_type, String mediaUrl,String date, double location_x, double location_y, String address, String distance) {
        this.id = id;
        this.uid = uid;
        this.username = username;
        this.imageUrl = imageUrl;
        this.title = title;
        this.text = text;
        this.content_type = content_type;
        this.mediaUrl = mediaUrl;
        this.date = date;
        this.location_x = location_x;
        this.location_y = location_y;
        this.address = address;
        this.distance = distance;

    }

    public String getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public int getContentType() {
        return content_type;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getDate() {
        return date;
    }

    public double getLocationX() {
        return location_x;
    }

    public double getLocationY() {
        return location_y;
    }

    public String getAddress() {
        return address;
    }
    public String getDistance() {
        return distance;
    }

    public void setImageData(byte[] imageData){
        this.imageData = imageData;
    }
    public byte[] getImageData(){
        return this.imageData;
    }
}
