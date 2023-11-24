package com.mariodev.novelapp.models;

@SuppressWarnings("ALL")
public class CommentModel {

    private String commentId;
    private String userId;
    private String commentText;
    private String commentDateTime;
    private String chapterId;
    private String storyId;
    private String poemOrPostId;
    private Boolean spoilersState;


    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public void setCommentDateTime(String commentDateTime) {
        this.commentDateTime = commentDateTime;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public void setPoemOrPostId(String poemOrPostId) {
        this.poemOrPostId = poemOrPostId;
    }

    public void setSpoilersState(Boolean spoilersState) {
        this.spoilersState = spoilersState;
    }

    public String getCommentId() {
        return commentId;
    }


    public String getUserId() {
        return userId;
    }


    public String getCommentText() {
        return commentText;
    }


    public String getCommentDateTime() {
        return commentDateTime;
    }


    public String getChapterId() {
        return chapterId;
    }


    public String getStoryId() {
        return storyId;
    }


    public Boolean getSpoilersState() {
        return spoilersState;
    }


    public String getPoemOrPostId() {
        return poemOrPostId;
    }

}
