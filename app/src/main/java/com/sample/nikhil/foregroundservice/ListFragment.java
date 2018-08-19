package com.sample.nikhil.foregroundservice;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ListFragment extends Fragment {

    public static final String ARG = "ARG";
    TextView fragmenttext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView= inflater.inflate(R.layout.fragment_list, container, false);
        fragmenttext=rootView.findViewById(R.id.fragmenttext);
        return rootView;
    }

}
