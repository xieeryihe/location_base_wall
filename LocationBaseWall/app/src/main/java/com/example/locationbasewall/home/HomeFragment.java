package com.example.locationbasewall.home;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locationbasewall.R;
import com.example.locationbasewall.utils.DataGetter;

import java.util.Objects;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.homeFragmentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // getLocationAndData();
        Activity activity = getActivity();
        DataGetter.getLocationAndPostOverviewData(Objects.requireNonNull(activity), recyclerView,
                1,10,-1);

        return view;
    }

}
