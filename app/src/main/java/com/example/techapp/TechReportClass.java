package com.example.techapp;

import android.graphics.Bitmap;

public class TechReportClass {

    private String mWebTitle;
    private String mType;
    private String mWebUrl;
    private String mDate;
    private String mSection;
    private Bitmap image;
    private String authorName;

    public TechReportClass(String mWebTitle, String mType, String mWebUrl, String mDate, String mSection, Bitmap image, String authorName) {
        this.mWebTitle = mWebTitle;
        this.mType = mType;
        this.mWebUrl = mWebUrl;
        this.mDate = mDate;
        this.mSection = mSection;
        this.image = image;
        this.authorName = authorName;
    }

    public int getIntYear() {

        String[] arrDate = mDate.split("-", 3);
        return Integer.parseInt(arrDate[0]);
    }

    public int getIntMonth() {
        String[] arrDate = mDate.split("-", 3);
        return Integer.parseInt(arrDate[1]);
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public int getIntDay() {
        String[] arr = mDate.split("T", 2);
        String[] arrDate = arr[0].split("-", 3);
        return Integer.parseInt(arrDate[2]);
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public void setmSection(String mSection) {
        this.mSection = mSection;
    }

    public String getmDate() {
        return mDate;
    }

    public String getmSection() {
        return mSection;
    }

    public void setmWebTitle(String mWebTitle) {
        this.mWebTitle = mWebTitle;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }

    public void setmWebUrl(String mWebUrl) {
        this.mWebUrl = mWebUrl;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getmWebTitle() {
        return mWebTitle;
    }

    public String getmType() {
        return mType;
    }

    public String getmWebUrl() {
        return mWebUrl;
    }

    public String getDate() {
        return mDate;
    }
}
