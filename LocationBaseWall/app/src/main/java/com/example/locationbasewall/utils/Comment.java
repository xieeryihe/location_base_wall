package com.example.locationbasewall.utils;
public class Comment {
    private String id;  // 评论id
    private String uid;  // 评论者的账户id
    private String content_type;  // 评论类型

    private String username;
    private String ip;
    private String content;
    private double locationX;
    private double locationY;
    private String imageUrl;

    public Comment(String username, String ip, String content, double locationX, double locationY, String imageUrl) {
        this.username = username;
        this.ip = ip;
        this.content = content;
        this.locationX = locationX;
        this.locationY = locationY;
        this.imageUrl = imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getIp() {
        return ip;
    }

    public String getContent() {
        return content;
    }

    public double getLocationX() {
        return locationX;
    }

    public double getLocationY() {
        return locationY;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
