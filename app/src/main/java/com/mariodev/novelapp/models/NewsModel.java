package com.mariodev.novelapp.models;

public class NewsModel {

    private String newsId;
    private String newsImage;
    private String newsTitle;
    private String newsText;
    private String newsDateTime;


    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }

    public void setNewsImage(String newsImage) {
        this.newsImage = newsImage;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }

    public void setNewsText(String newsText) {
        this.newsText = newsText;
    }

    public void setNewsDateTime(String newsDateTime) {
        this.newsDateTime = newsDateTime;
    }

    public String getNewsId() {
        return newsId;
    }


    public String getNewsImage() {
        return newsImage;
    }


    public String getNewsTitle() {
        return newsTitle;
    }


    public String getNewsText() {
        return newsText;
    }


    public String getNewsDateTime() {
        return newsDateTime;
    }



}
