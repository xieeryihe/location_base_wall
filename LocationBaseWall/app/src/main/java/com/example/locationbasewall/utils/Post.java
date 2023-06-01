package com.example.locationbasewall.utils;

// 帖子类
public class Post {
    private String id;
    private String uid;
    private String title;
    private String text;
    private int content_type;
    private double location_x;
    private double location_y;
    private byte[] mediaData;
    private String address;

    public Post(String id, String uid, String title, String text, int content_type, double location_x, double location_y, byte[] mediaData, String address) {
        this.id = id;
        this.uid = uid;
        this.title = title;
        this.text = text;
        this.content_type = content_type;
        this.location_x = location_x;
        this.location_y = location_y;
        this.mediaData = mediaData;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }


    public String getTitle(){
        return title;
    }
    public String getText() {
        return text;
    }

    public int getContent_type() {
        return content_type;
    }

    public double getLocation_x() {
        return location_x;
    }

    public double getLocation_y() {
        return location_y;
    }

    public byte[] getMediaData() {
        return mediaData;
    }

    public String getAddress() {
        return address;
    }
}

