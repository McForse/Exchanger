package com.shotball.project.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.shotball.project.R;

public class MessagesFragment extends Fragment {

    private static final String TAG = "MessagesFragment";

    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_messages, container, false);
        Log.d(TAG, "onCreateView");

        initToolbar();

        return rootView;
    }

    private void initToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.setTitle("Messages");
        //((AppCompatActivity) mActivity).setSupportActionBar(toolbar);
        //setHasOptionsMenu(true);
    }
}
