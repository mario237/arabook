package com.mariodev.novelapp.models;

public class PostModel {

    private String userId;
    private String postId;
    private String postText;
    private String dateTime;
    boolean isShrink = true;

    public PostModel() {
    }

    public PostModel(String userId, String postId, String postText, String dateTime) {
        this.userId = userId;
        this.postId = postId;
        this.postText = postText;
        this.dateTime = dateTime;

    }

    public String getUserId() {
        return userId;
    }


    public String getPostId() {
        return postId;
    }


    public String getPostText() {
        return postText;
    }


    public String getDateTime() {
        return dateTime;
    }


    public boolean isShrink() {
        return isShrink;
    }

    public void setShrink(boolean shrink) {
        isShrink = shrink;
    }
}
