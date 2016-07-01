package com.nao.im.ui.activity.player;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nao.im.R;

/**
 * Created by chaopei on 2015/11/6.
 */
public class UserListFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_list, container, false);
        initViews(v);
        return v;
    }

    private void initViews(View v) {

    }
}
