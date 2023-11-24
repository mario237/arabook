package com.mariodev.novelapp.models;



public class StoryModel{

    private String storyId;
    private String storyWriter;
    private String storyImage;
    private String storyName;
    private String storyDescription;
    private String storyType;
    private String tags;
    private String days;
    private boolean isForAdult;
    private boolean isCompleted;
    private boolean discover;
    private int storyViews;
    private int chaptersCount;
    private int storyFollowers;
    private int storyVotes;
    private int storyComments;
    private String storySecurity;
    private boolean selected;

    public StoryModel() {
    }

    public StoryModel(String storyId, String storyWriter, String storyImage, String storyName, String storyDescription, String storyType, String tags, String days, boolean isForAdult, boolean isCompleted, boolean discover, int storyViews, int chaptersCount, int storyFollowers, int storyVotes, int storyComments, String storySecurity) {
        this.storyId = storyId;
        this.storyWriter = storyWriter;
        this.storyImage = storyImage;
        this.storyName = storyName;
        this.storyDescription = storyDescription;
        this.storyType = storyType;
        this.tags = tags;
        this.days = days;
        this.isForAdult = isForAdult;
        this.isCompleted = isCompleted;
        this.discover = discover;
        this.storyViews = storyViews;
        this.chaptersCount = chaptersCount;
        this.storyFollowers = storyFollowers;
        this.storyVotes = storyVotes;
        this.storyComments = storyComments;
        this.storySecurity = storySecurity;
    }

    public String getStoryId() {
        return storyId;
    }


    public String getStoryWriter() {
        return storyWriter;
    }


    public String getStoryImage() {
        return storyImage;
    }


    public String getStoryName() {
        return storyName;
    }


    public String getStoryDescription() {
        return storyDescription;
    }


    public String getStoryType() {
        return storyType;
    }


    public boolean isForAdult() {
        return isForAdult;
    }


    public boolean isCompleted() {
        return isCompleted;
    }


    public String getTags() {
        return tags;
    }


    public String getDays() {
        return days;
    }


    public boolean isDiscover() {
        return discover;
    }


    public int getStoryViews() {
        return storyViews;
    }


    public int getStoryVotes() {
        return storyVotes;
    }


    public int getChaptersCount() {
        return chaptersCount;
    }


    public String getStorySecurity() {
        return storySecurity;
    }


    public int getStoryFollowers() {
        return storyFollowers;
    }


    public int getStoryComments() {
        return storyComments;
    }


    public void setDateTime(String dateTime) {
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
