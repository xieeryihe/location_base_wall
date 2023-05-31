package com.example.locationbasewall.utils;

import java.io.Serializable;

// 帖子类
// 实现序列化接口，便于数据传递
public class Post implements Serializable {
    private String title;
    private String content;

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
