package com.example.locationbasewall.data;
public class Comment {
    private String username;
    private String ipLocation;
    private String content;
    private double locationX;
    private double locationY;
    private String portraitUrl;

    public Comment(String username, String ipLocation, String content, double locationX, double locationY, String portraitUrl) {
        this.username = username;
        this.ipLocation = ipLocation;
        this.content = content;
        this.locationX = locationX;
        this.locationY = locationY;
        this.portraitUrl = portraitUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getIpLocation() {
        return ipLocation;
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

    public String getPortraitUrl() {
        return portraitUrl;
    }
}
