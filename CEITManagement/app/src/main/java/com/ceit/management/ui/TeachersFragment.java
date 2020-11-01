package com.ceit.management.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ceit.management.AppInstance;
import com.ceit.management.R;
import com.ceit.management.adapter.TeacherListAdapter;
import com.ceit.management.api.TeacherAPI;
import com.ceit.management.model.ServerResponse;
import com.ceit.management.net.InternetReceiver;
import com.ceit.management.pojo.TeacherItem;
import com.ceit.management.util.DialogUtil;
import com.ceit.management.view.CurvedBottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeachersFragment extends Fragment implements WaveSwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, InternetReceiver.OnInternetConnectionChangedListener
{
    private WaveSwipeRefreshLayout refreshTeacherListLayout;
    private CurvedBottomNavigationView curvedBottomNavigationView;
    private FloatingActionButton fabAdd;
    private LinearLayout overlayNoWifi;
    private LinearLayout overlayNodataFound;
    private LinearLayout overlayServerError;
    private RecyclerView listTeachers;
    private TeacherListAdapter teacherListAdapter;

    @SuppressLint("NonConstantResourceId")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_teachers, container, false);
        refreshTeacherListLayout = root.findViewById(R.id.refresh_teachers_list);
        curvedBottomNavigationView = root.findViewById(R.id.bottom_nav_teachers);
        fabAdd = root.findViewById(R.id.fab);
        overlayNoWifi = root.findViewById(R.id.no_wifi_layout);
        overlayNodataFound = root.findViewById(R.id.no_data_layout);
        overlayServerError = root.findViewById(R.id.server_error_layout);
        listTeachers = root.findViewById(R.id.list_teachers);

        refreshTeacherListLayout.setColorSchemeColors(getContext().getResources().getColor(R.color.white));
        refreshTeacherListLayout.setWaveColor(getContext().getResources().getColor(R.color.themeColor));
        refreshTeacherListLayout.setOnRefreshListener(this);
        listTeachers.setHasFixedSize(true);

        curvedBottomNavigationView.setOnNavigationItemSelectedListener((MenuItem item) -> {
            switch(item.getItemId())
            {
                case R.id.bottom_nav_teachers:
                    teacherListAdapter.clear();
                    refreshTeacherListLayout.setRefreshing(true);
                    fetchAllTeachers();
                break;

                case R.id.bottom_nav_removed:
                    teacherListAdapter.clear();
                    refreshTeacherListLayout.setRefreshing(true);
                    fetchAllRemovedTeachers();
                break;
            }

            return false;
        });

        fabAdd.setOnClickListener((View view) -> {

        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        listTeachers.setLayoutManager(layoutManager);

        teacherListAdapter = new TeacherListAdapter(new ArrayList<>());
        listTeachers.setAdapter(teacherListAdapter);

        refreshTeacherListLayout.setRefreshing(true);
        fetchAllTeachers();

        AppInstance.hookUpConnectivityListener(this);
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onRefresh()
    {
        teacherListAdapter.clear();
        fetchAllTeachers();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search teacher...");
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        teacherListAdapter.getFilter().filter(newText);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        teacherListAdapter.getFilter().filter(query);
        return true;
    }

    @Override
    public void onInternetConnectionChanged(boolean isConnected)
    {
        if(!isConnected)
        {
            DialogUtil.warningDialog(getContext(), "Network Error", "You are not connected to the internet!", false);
            noWifiConnectivity();
        }
        else
        {
            DialogUtil.dismissDialog();
            connectedToInternet();
        }
    }


    private void noWifiConnectivity()
    {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.VISIBLE);
        listTeachers.setVisibility(View.GONE);
    }

    private void noDataFound()
    {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.VISIBLE);
        overlayNoWifi.setVisibility(View.GONE);
        listTeachers.setVisibility(View.GONE);
    }

    private void serverError()
    {
        overlayServerError.setVisibility(View.VISIBLE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        listTeachers.setVisibility(View.GONE);
    }

    private void connectedToInternet()
    {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        listTeachers.setVisibility(View.VISIBLE);
    }

    private void dataFound()
    {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        listTeachers.setVisibility(View.VISIBLE);
    }

    private void fetchAllTeachers()
    {
        if(!AppInstance.isConnected(getContext()))
        {
            DialogUtil.warningDialog(getContext(), "Network Error", "You are not connected to internet!", "Okay", false);
            refreshTeacherListLayout.setRefreshing(false);
            noWifiConnectivity();
            return;
        }

        TeacherAPI api = AppInstance.retrofit().create(TeacherAPI.class);
        Call<ServerResponse<TeacherItem>> call = api.getAllTeachers();
        call.enqueue(new Callback<ServerResponse<TeacherItem>>() {
            @Override
            public void onResponse(Call<ServerResponse<TeacherItem>> call, Response<ServerResponse<TeacherItem>> response)
            {
                ServerResponse<TeacherItem> server = response.body();
                refreshTeacherListLayout.setRefreshing(false);

                if(server != null)
                {
                    List<TeacherItem> teacherItems = server.data;

                    if(teacherItems != null && !server.hasError)
                    {
                        teacherListAdapter.addTeacherItem(teacherItems);

                        connectedToInternet();
                        dataFound();
                    }
                    else
                        noDataFound();
                }
                else
                    noDataFound();
            }

            @Override
            public void onFailure(Call<ServerResponse<TeacherItem>> call, Throwable t)
            {
                refreshTeacherListLayout.setRefreshing(false);
                serverError();
            }
        });
    }

    private void fetchAllRemovedTeachers()
    {
        if(!AppInstance.isConnected(getContext()))
        {
            DialogUtil.warningDialog(getContext(), "Network Error", "You are not connected to internet!", "Okay", false);
            refreshTeacherListLayout.setRefreshing(false);
            noWifiConnectivity();
            return;
        }

        TeacherAPI api = AppInstance.retrofit().create(TeacherAPI.class);
        Call<ServerResponse<TeacherItem>> call = api.getAllRemovedTeachers();
        call.enqueue(new Callback<ServerResponse<TeacherItem>>() {
            @Override
            public void onResponse(Call<ServerResponse<TeacherItem>> call, Response<ServerResponse<TeacherItem>> response)
            {
                ServerResponse<TeacherItem> server = response.body();
                refreshTeacherListLayout.setRefreshing(false);

                if(server != null)
                {
                    List<TeacherItem> teacherItems = server.data;

                    if(teacherItems != null && !server.hasError)
                    {
                        teacherListAdapter.addTeacherItem(teacherItems);

                        connectedToInternet();
                        dataFound();
                    }
                    else
                        noDataFound();
                }
                else
                    noDataFound();
            }

            @Override
            public void onFailure(Call<ServerResponse<TeacherItem>> call, Throwable t)
            {
                refreshTeacherListLayout.setRefreshing(false);
                serverError();
            }
        });
    }
}
