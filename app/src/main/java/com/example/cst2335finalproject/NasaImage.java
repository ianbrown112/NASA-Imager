package com.example.cst2335finalproject;

import android.os.Parcel;
import android.os.Parcelable;

public class NasaImage implements Parcelable {

    private String parsedFileName;
    private String title;
    private String filePath;

    NasaImage(String title, String parsedFileName) {
        this.title = title;
        this.parsedFileName = parsedFileName;
    }

    public String getParsedFileName() {
        return parsedFileName;
    }

    public String getTitle() {
        return title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setParsedFileName(String parsedFileName) {
        this.parsedFileName = parsedFileName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
