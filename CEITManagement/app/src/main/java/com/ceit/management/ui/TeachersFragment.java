package com.ceit.management.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ceit.management.AppInstance;
import com.ceit.management.R;
import com.ceit.management.adapter.TeacherListAdapter;
import com.ceit.management.adapter.pagination.PaginationListener;
import com.ceit.management.pojo.TeacherItem;
import com.ceit.management.util.DialogUtil;

import java.util.ArrayList;
import java.util.List;

public class TeachersFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    private SwipeRefreshLayout refreshTeacherListLayout;
    private RecyclerView listTeachers;
    private TeacherListAdapter teacherListAdapter;
    private boolean isLastPage = false;
    private boolean isLoading = false;
    private int currentPage = 1;
    private int totalPage = 10;
    private int itemCount = 0;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_teachers, container, false);
        refreshTeacherListLayout = root.findViewById(R.id.refresh_teachers_list);
        listTeachers = root.findViewById(R.id.list_teachers);

        refreshTeacherListLayout.setColorSchemeColors(getContext().getResources().getColor(R.color.themeColor));
        refreshTeacherListLayout.setOnRefreshListener(this);
        listTeachers.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        listTeachers.setLayoutManager(layoutManager);

        teacherListAdapter = new TeacherListAdapter(new ArrayList<>());
        listTeachers.setAdapter(teacherListAdapter);
        fetchAllTeachers();
        listTeachers.addOnScrollListener(new PaginationListener(layoutManager) {
            @Override
            protected void loadMoreItems()
            {
                isLoading = true;
                currentPage++;
                fetchAllTeachers();
            }

            @Override
            public boolean isLoading()
            {
                return isLoading;
            }

            @Override
            public boolean isLastPage()
            {
                return isLastPage;
            }
        });

        return root;
    }

    @Override
    public void onRefresh()
    {
        itemCount = 0;
        currentPage = 1;
        isLastPage = false;
        teacherListAdapter.clear();
        fetchAllTeachers();
    }

    private void fetchAllTeachers()
    {
        if(!AppInstance.isConnected(getContext()))
        {
            DialogUtil.warningDialog(getContext(), "Network Error", "You are not connected to internet!", "Okay", false);
            return;
        }

        List<TeacherItem> teacherItems = new ArrayList<>();
        (new Handler()).postDelayed(() -> {
            for(int i = 0; i < 10; i++)
            {
                itemCount++;
                teacherItems.add(TeacherItem.newTeacher(
                        "",
                        "Teacher #" + itemCount,
                        "High School Teacher",
                        "Teacher 2"
                ));
            }

            if(currentPage != 1)
                teacherListAdapter.removeLoading();
            teacherListAdapter.addTeacherItem(teacherItems);
            refreshTeacherListLayout.setRefreshing(false);

            if(currentPage < totalPage)
                teacherListAdapter.addLoading();
            else
                isLastPage = true;
            isLoading = false;
        }, 1500);
    }
}
