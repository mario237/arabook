package com.mariodev.novelapp.models;

@SuppressWarnings("ALL")
public class PoemModel {
    private String userId;
    private String poemId;
    private String poemTitle;
    private String poemText;
    private String dateTime;

    public PoemModel() {
    }

    public PoemModel(String userId, String poemId, String poemTitle, String poemText, String dateTime) {
        this.userId = userId;
        this.poemId = poemId;
        this.poemTitle = poemTitle;
        this.poemText = poemText;
        this.dateTime = dateTime;

    }

    public String getUserId() {
        return userId;
    }


    public String getPoemId() {
        return poemId;
    }


    public String getPoemTitle() {
        return poemTitle;
    }


    public String getPoemText() {
        return poemText;
    }


    public String getDateTime() {
        return dateTime;
    }



}
