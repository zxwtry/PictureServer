package com.zxwtry.pictureServer.entity;

import com.google.gson.Gson;

public class PictureInfo {
    private int id;
    private Integer largeIndex;
    private Integer largeOffset;
    private Integer largeLength;
    private Integer wap180Index;
    private Integer wap180Offset;
    private Integer wap180Length;
    private Integer bmiddleIndex;
    private Integer bmiddleOffset;
    private Integer bmiddleLength;
    private String fileName;
    private short status;
    public int getId() {
        return id;
    }
    public Integer getLargeLength() {
        return largeLength;
    }
    public void setLargeLength(Integer largeLength) {
        this.largeLength = largeLength;
    }
    public Integer getWap180Length() {
        return wap180Length;
    }
    public void setWap180Length(Integer wap180Length) {
        this.wap180Length = wap180Length;
    }
    public Integer getBmiddleLength() {
        return bmiddleLength;
    }
    public void setBmiddleLength(Integer bmiddleLength) {
        this.bmiddleLength = bmiddleLength;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Integer getLargeIndex() {
        return largeIndex;
    }
    public void setLargeIndex(Integer largeIndex) {
        this.largeIndex = largeIndex;
    }
    public Integer getLargeOffset() {
        return largeOffset;
    }
    public void setLargeOffset(Integer largeOffset) {
        this.largeOffset = largeOffset;
    }
    public Integer getWap180Index() {
        return wap180Index;
    }
    public void setWap180Index(Integer wap180Index) {
        this.wap180Index = wap180Index;
    }
    public Integer getWap180Offset() {
        return wap180Offset;
    }
    public void setWap180Offset(Integer wap180Offset) {
        this.wap180Offset = wap180Offset;
    }
    public Integer getBmiddleIndex() {
        return bmiddleIndex;
    }
    public void setBmiddleIndex(Integer bmiddleIndex) {
        this.bmiddleIndex = bmiddleIndex;
    }
    public Integer getBmiddleOffset() {
        return bmiddleOffset;
    }
    public void setBmiddleOffset(Integer bmiddleOffset) {
        this.bmiddleOffset = bmiddleOffset;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public short getStatus() {
        return status;
    }
    public void setStatus(short status) {
        this.status = status;
    }
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
