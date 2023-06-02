package com.example.locationbasewall.utils;
public class Comment {
    private String id;  // 评论id
    private String user_id;  // 评论者的账户id
    private String username;
    private String user_picture;
    private String text;
    private String content_type;  // 评论类型
    private String media_url;
    private String ip_address;
    private String date;

    public Comment(String id, String user_id, String username, String user_picture, String text, String content_type, String media_url, String ip_address, String date) {
        this.id = id;
        this.user_id = user_id;
        this.username = username;
        this.user_picture = user_picture;
        this.text = text;
        this.content_type = content_type;
        this.media_url = media_url;
        this.ip_address = ip_address;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getUser_picture() {
        return user_picture;
    }

    public String getText() {
        return text;
    }

    public String getContent_type() {
        return content_type;
    }

    public String getMedia_url() {
        return media_url;
    }

    public String getIp_address() {
        return ip_address;
    }

    public String getDate() {
        return date;
    }
}
