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

    public NasaImage(Parcel source) {
        parsedFileName = source.readString();
        title = source.readString();
        filePath = source.readString();
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
        parcel.writeString(parsedFileName);
        parcel.writeString(title);
        parcel.writeString(filePath);
    }
    public static Parcelable.Creator<NasaImage> CREATOR = new MyCreator();
}

class MyCreator implements Parcelable.Creator<NasaImage> {

    @Override
    public NasaImage createFromParcel(Parcel source) {
        return new NasaImage(source);
    }

    @Override
    public NasaImage[] newArray(int size) {
        return new NasaImage[size];
    }

}