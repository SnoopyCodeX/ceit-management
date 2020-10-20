package com.ceit.management.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.ceit.management.R;
import com.ceit.management.util.Constants;

public class DashboardFragment extends Fragment implements View.OnClickListener
{
    private CardView gridClass, gridTeachers, gridStudents, gridParents;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        gridClass = root.findViewById(R.id.grid_class);
        gridTeachers = root.findViewById(R.id.grid_teachers);
        gridStudents = root.findViewById(R.id.grid_students);
        gridParents = root.findViewById(R.id.grid_parents);

        gridClass.setOnClickListener(this);
        gridTeachers.setOnClickListener(this);
        gridStudents.setOnClickListener(this);
        gridParents.setOnClickListener(this);

        return root;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.grid_class:
                    navigateToFragment(R.id.nav_class);
                break;

            case R.id.grid_teachers:
                    navigateToFragment(R.id.nav_teachers);
                break;

            case R.id.grid_students:
                    navigateToFragment(R.id.nav_students);
                break;

            case R.id.grid_parents:
                    navigateToFragment(R.id.nav_parents);
                break;
        }
    }

    private void navigateToFragment(int fragmentId)
    {
        Intent data = new Intent(Constants.TRIGGER_FRAGMENT_NAVIGATION);
        data.putExtra(Constants.KEY_TRIGGER_FRAGMENT_NAVIGATE, fragmentId);
        getContext().sendBroadcast(data);
    }
}

