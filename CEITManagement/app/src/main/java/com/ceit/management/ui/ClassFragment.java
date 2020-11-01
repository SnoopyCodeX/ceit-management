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
import com.ceit.management.adapter.ClassListAdapter;
import com.ceit.management.api.ClassAPI;
import com.ceit.management.model.ServerResponse;
import com.ceit.management.net.InternetReceiver;
import com.ceit.management.pojo.ClassItem;
import com.ceit.management.util.DialogUtil;
import com.ceit.management.view.CurvedBottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassFragment extends Fragment implements WaveSwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, InternetReceiver.OnInternetConnectionChangedListener
{
    private WaveSwipeRefreshLayout refreshClassListLayout;
    private CurvedBottomNavigationView curvedBottomNavigationView;
    private FloatingActionButton fabAdd;
    private LinearLayout overlayNoWifi;
    private LinearLayout overlayNodataFound;
    private LinearLayout overlayServerError;
    private RecyclerView listClasses;
    private ClassListAdapter classListAdapter;

    @SuppressLint("NonConstantResourceId")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_class, container, false);
        refreshClassListLayout = root.findViewById(R.id.refresh_classes_list);
        curvedBottomNavigationView = root.findViewById(R.id.bottom_nav_classes);
        fabAdd = root.findViewById(R.id.fab);
        overlayNoWifi = root.findViewById(R.id.no_wifi_layout);
        overlayNodataFound = root.findViewById(R.id.no_data_layout);
        overlayServerError = root.findViewById(R.id.server_error_layout);
        listClasses = root.findViewById(R.id.list_classes);

        refreshClassListLayout.setColorSchemeColors(getContext().getResources().getColor(R.color.white));
        refreshClassListLayout.setWaveColor(getContext().getResources().getColor(R.color.themeColor));
        refreshClassListLayout.setOnRefreshListener(this);
        listClasses.setHasFixedSize(true);

        curvedBottomNavigationView.setOnNavigationItemSelectedListener((MenuItem item) -> {
            switch(item.getItemId())
            {
                case R.id.bottom_nav_classes:
                    classListAdapter.clear();
                    refreshClassListLayout.setRefreshing(true);
                    fetchAllClasses();
                break;

                case R.id.bottom_nav_removed:
                    classListAdapter.clear();
                    refreshClassListLayout.setRefreshing(true);
                    fetchAllRemovedClasses();
                break;
            }

            return false;
        });

        fabAdd.setOnClickListener((View v) -> {

        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        listClasses.setLayoutManager(layoutManager);

        classListAdapter = new ClassListAdapter(new ArrayList<>());
        listClasses.setAdapter(classListAdapter);

        refreshClassListLayout.setRefreshing(true);
        fetchAllClasses();

        AppInstance.hookUpConnectivityListener(this);
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search class...");
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        classListAdapter.getFilter().filter(newText);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        classListAdapter.getFilter().filter(query);
        return true;
    }

    @Override
    public void onRefresh()
    {
        classListAdapter.clear();
        fetchAllClasses();
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
        listClasses.setVisibility(View.GONE);
    }

    private void noDataFound()
    {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.VISIBLE);
        overlayNoWifi.setVisibility(View.GONE);
        listClasses.setVisibility(View.GONE);
    }

    private void serverError()
    {
        overlayServerError.setVisibility(View.VISIBLE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        listClasses.setVisibility(View.GONE);
    }

    private void connectedToInternet()
    {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        listClasses.setVisibility(View.VISIBLE);
    }

    private void dataFound()
    {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        listClasses.setVisibility(View.VISIBLE);
    }

    private void fetchAllClasses()
    {
        if(!AppInstance.isConnected(getContext()))
        {
            DialogUtil.warningDialog(getContext(), "Network Error", "You are not connected to internet!", false);
            refreshClassListLayout.setRefreshing(false);
            noWifiConnectivity();
            return;
        }

        ClassAPI api = AppInstance.retrofit().create(ClassAPI.class);
        Call<ServerResponse<ClassItem>> call = api.getAllClasses();
        call.enqueue(new Callback<ServerResponse<ClassItem>>() {
            @Override
            public void onResponse(Call<ServerResponse<ClassItem>> call, Response<ServerResponse<ClassItem>> response)
            {
                ServerResponse<ClassItem> server = response.body();
                refreshClassListLayout.setRefreshing(false);

                if(server != null)
                {
                    List<ClassItem> classItems = server.data;

                    if(classItems != null && !server.hasError)
                    {
                        classListAdapter.addClassItem(classItems);

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
            public void onFailure(Call<ServerResponse<ClassItem>> call, Throwable t)
            {
                refreshClassListLayout.setRefreshing(false);
                serverError();
            }
        });
    }

    private void fetchAllRemovedClasses()
    {
        if(!AppInstance.isConnected(getContext()))
        {
            DialogUtil.warningDialog(getContext(), "Network Error", "You are not connected to internet!", false);
            refreshClassListLayout.setRefreshing(false);
            noWifiConnectivity();
            return;
        }

        ClassAPI api = AppInstance.retrofit().create(ClassAPI.class);
        Call<ServerResponse<ClassItem>> call = api.getAllRemovedClasses();
        call.enqueue(new Callback<ServerResponse<ClassItem>>() {
            @Override
            public void onResponse(Call<ServerResponse<ClassItem>> call, Response<ServerResponse<ClassItem>> response)
            {
                ServerResponse<ClassItem> server = response.body();
                refreshClassListLayout.setRefreshing(false);

                if(server != null)
                {
                    List<ClassItem> classItems = server.data;

                    if(classItems != null && !server.hasError)
                    {
                        classListAdapter.addClassItem(classItems);

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
            public void onFailure(Call<ServerResponse<ClassItem>> call, Throwable t)
            {
                refreshClassListLayout.setRefreshing(false);
                serverError();
            }
        });
    }
}
