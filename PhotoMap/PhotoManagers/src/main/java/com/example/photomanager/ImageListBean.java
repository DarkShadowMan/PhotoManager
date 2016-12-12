package com.example.photomanager;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ImageListBean {
    @SerializedName("name")
    private String name;
    @SerializedName("_id")
    final String _id;
    @SerializedName("image_list")
    private List<ImageBean> imageList = new ArrayList<ImageBean>();
    @SerializedName("description")
    private String description;

    public ImageListBean(String name, List<ImageBean> imageList, String description) {
        this.name = name;
        this.imageList = imageList;
        this.description = description;
        this._id = UUID.randomUUID().toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public List<ImageBean> getImageList() {
        return imageList;
    }

    public String getDescription() { return description; }

    public String getId() {
        return _id;
    }

    public boolean isEmpty() {
        if(imageList.size() > 0) {
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public String toString() {
        return "ID: " + _id + " NAME: " + name + "\n" + description;
    }
}
