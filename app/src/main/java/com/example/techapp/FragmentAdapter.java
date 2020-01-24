package com.example.techapp;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class FragmentAdapter extends FragmentStatePagerAdapter {
    private final int TECH = 0;
    private  final int Culture = 1;
    private int fragsNum;
    String [] pageTitle = {"Tech", "Culture"};
    FragmentAdapter(FragmentManager manager, int fragsNum) {
        super(manager);
        this.fragsNum = fragsNum;
    }

    @Override
    public int getCount() {
        return fragsNum;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitle[position];
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case TECH:
                return new TechFrag();
            case Culture:
                return new CultureNews();
        }
        return null;
    }
}
