package com.mariodev.novelapp.models;

@SuppressWarnings("CanBeFinal")
public class StoryTypeModel {
    private String typeName;
    private int typeImg;


    public StoryTypeModel(String typeName, int typeImg) {
        this.typeName = typeName;
        this.typeImg = typeImg;
    }


    public String getTypeName() {
        return typeName;
    }


    public int getTypeImg() {
        return typeImg;
    }


}
