package com.example.creddit.Adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.creddit.Fragments.HomeFragment;
import com.example.creddit.Fragments.PopularFragment;
import com.example.creddit.Fragments.SavedPostsFragment;

public class SavedTabAdapter extends FragmentStatePagerAdapter {
    int counttab;

    public SavedTabAdapter(FragmentManager fm, int counttab) {
        super(fm);
        this.counttab = counttab;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0 :
                SavedPostsFragment savedPostsFragment = new SavedPostsFragment();
                return savedPostsFragment;
            case 1 :
                PopularFragment popularFragment = new PopularFragment();
                return popularFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return counttab;
    }
}
