package com.mariodev.novelapp.models;

public class ChapterModel {

    private String storyId;
    private String chapterId;
    private String chapterImage;
    private String chapterName;
    private String chapterText;
    private String chapterDateTime;
    private String wordCounter;

    public ChapterModel() {
    }


    public ChapterModel(String storyId, String chapterId, String chapterImage, String chapterName, String chapterText, String chapterDateTime, String wordCounter) {
        this.storyId = storyId;
        this.chapterId = chapterId;
        this.chapterImage = chapterImage;
        this.chapterName = chapterName;
        this.chapterText = chapterText;
        this.chapterDateTime = chapterDateTime;
        this.wordCounter = wordCounter;

    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterImage() {
        return chapterImage;
    }

    public void setChapterImage(String chapterImage) {
        this.chapterImage = chapterImage;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public String getChapterText() {
        return chapterText;
    }

    public void setChapterText(String chapterText) {
        this.chapterText = chapterText;
    }

    public String getChapterDateTime() {
        return chapterDateTime;
    }

    public void setChapterDateTime(String chapterDateTime) {
        this.chapterDateTime = chapterDateTime;
    }

    public String getWordCounter() {
        return wordCounter;
    }

    public void setWordCounter(String wordCounter) {
        this.wordCounter = wordCounter;
    }


}
