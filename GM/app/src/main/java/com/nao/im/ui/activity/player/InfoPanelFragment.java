package com.nao.im.ui.activity.player;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nao.im.R;
import com.nao.im.ui.activity.main.MainActivity;

/**
 * Created by chaopei on 2015/11/6.
 */
public class InfoPanelFragment extends Fragment {

    ViewPager mInfoPager;

    MainActivity mActivity;
    IMFragment mImFragment;
    MusicListFragment mMusicListFragment;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info_panel, container, false);
        initViews(v);
        return v;
    }

    private void initViews(View parent) {
        mInfoPager = (ViewPager) parent.findViewById(R.id.info_pager);
        mInfoPager.setAdapter(new InfoPanelAdapter(mActivity.getSupportFragmentManager()));
    }

    private class InfoPanelAdapter extends FragmentPagerAdapter {

        public InfoPanelAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    mImFragment = new IMFragment();
                    return mImFragment;
                case 1:
                    mMusicListFragment = new MusicListFragment();
                    return mMusicListFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public void setSoftInputVisible(boolean visible) {
        mImFragment.setSoftInputVisible(visible);
    }
}
