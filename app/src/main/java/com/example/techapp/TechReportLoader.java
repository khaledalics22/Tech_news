package com.example.techapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TechReportLoader extends AsyncTaskLoader<List<TechReportClass>> {

    private String mUrl;

    public TechReportLoader(@NonNull Context context, String Url) {
        super(context);
        mUrl = Url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public List<TechReportClass> loadInBackground() {
        List<TechReportClass> list = new ArrayList<>();
        try {
            list = QueryUtils.makeHttpConnection(getContext(), mUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
