package com.example.techapp;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class FragmentAdapter extends FragmentStatePagerAdapter {
    private final int TECH = 0;
    private final int Culture = 1;
    private final int WEATHER = 2;
    private final int EDUCATION = 3;
    private final int Politics = 4;
    private int fragsNum;
    private String[] pageTitle = {"Tech", "Culture", "Weather","Education","Politics"};

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
            case WEATHER:
                return new WeatherFrag();
            case EDUCATION:
                return new EducationFrag();
            case Politics:
                return  new PoliticsFrag();
            }
        return null;
    }
}
