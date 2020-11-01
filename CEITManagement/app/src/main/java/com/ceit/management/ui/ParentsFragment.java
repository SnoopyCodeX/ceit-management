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
import com.ceit.management.adapter.ParentListAdapter;
import com.ceit.management.api.ParentAPI;
import com.ceit.management.model.ServerResponse;
import com.ceit.management.net.InternetReceiver;
import com.ceit.management.pojo.ParentItem;
import com.ceit.management.util.DialogUtil;
import com.ceit.management.view.CurvedBottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentsFragment extends Fragment implements WaveSwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, InternetReceiver.OnInternetConnectionChangedListener
{
    private WaveSwipeRefreshLayout refreshParentListLayout;
    private CurvedBottomNavigationView curvedBottomNavigationView;
    private FloatingActionButton fabAdd;
    private LinearLayout overlayNoWifi;
    private LinearLayout overlayNodataFound;
    private LinearLayout overlayServerError;
    private RecyclerView listParents;
    private View root;
    private ParentListAdapter parentListAdapter;

    @SuppressLint("NonConstantResourceId")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        root = inflater.inflate(R.layout.fragment_parents, container, false);
        refreshParentListLayout = root.findViewById(R.id.refresh_parents_list);
        curvedBottomNavigationView = root.findViewById(R.id.bottom_nav_parents);
        fabAdd = root.findViewById(R.id.fab);
        overlayNoWifi = root.findViewById(R.id.no_wifi_layout);
        overlayNodataFound = root.findViewById(R.id.no_data_layout);
        overlayServerError = root.findViewById(R.id.server_error_layout);
        listParents = root.findViewById(R.id.list_parents);

        refreshParentListLayout.setColorSchemeColors(getContext().getResources().getColor(R.color.white));
        refreshParentListLayout.setWaveColor(getContext().getResources().getColor(R.color.themeColor));
        refreshParentListLayout.setOnRefreshListener(this);
        listParents.setHasFixedSize(true);

        curvedBottomNavigationView.setOnNavigationItemSelectedListener((MenuItem item) -> {
            switch(item.getItemId())
            {
                case R.id.bottom_nav_parents:
                    parentListAdapter.clear();
                    refreshParentListLayout.setRefreshing(true);
                    fetchAllParents();
                    break;

                case R.id.bottom_nav_removed:
                    parentListAdapter.clear();
                    refreshParentListLayout.setRefreshing(true);
                    fetchAllRemovedParents();
                    break;
            }

            return false;
        });

        fabAdd.setOnClickListener((View view) -> {

        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        listParents.setLayoutManager(layoutManager);

        parentListAdapter = new ParentListAdapter(new ArrayList<>());
        listParents.setAdapter(parentListAdapter);
        fetchAllParents();

        setHasOptionsMenu(true);
        AppInstance.hookUpConnectivityListener(this);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search parent...");
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        parentListAdapter.getFilter().filter(newText);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        parentListAdapter.getFilter().filter(query);
        return true;
    }

    @Override
    public void onRefresh()
    {
        parentListAdapter.clear();
        fetchAllParents();
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
        listParents.setVisibility(View.GONE);
    }

    private void noDataFound()
    {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.VISIBLE);
        overlayNoWifi.setVisibility(View.GONE);
        listParents.setVisibility(View.GONE);
    }

    private void serverError()
    {
        overlayServerError.setVisibility(View.VISIBLE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        listParents.setVisibility(View.GONE);
    }

    private void connectedToInternet()
    {
        overlayServerError.setVisibility(View.VISIBLE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        listParents.setVisibility(View.VISIBLE);
    }

    private void dataFound()
    {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        listParents.setVisibility(View.VISIBLE);
    }

    private void fetchAllParents()
    {
        if(!AppInstance.isConnected(getContext()))
        {
            DialogUtil.warningDialog(getContext(), "Network Error", "You are not connected to the internet!", false);
            noWifiConnectivity();
            refreshParentListLayout.setRefreshing(false);
            return;
        }
        else
            connectedToInternet();

        ParentAPI api = AppInstance.retrofit().create(ParentAPI.class);
        Call<ServerResponse<ParentItem>> call = api.getAllParents();
        call.enqueue(new Callback<ServerResponse<ParentItem>>() {
            @Override
            public void onResponse(Call<ServerResponse<ParentItem>> call, Response<ServerResponse<ParentItem>> response)
            {
                ServerResponse<ParentItem> server = response.body();

                if(server != null)
                {
                    List<ParentItem> parentItems = server.data;

                    if(parentItems != null && !server.hasError)
                    {
                        parentListAdapter.addParentItem(parentItems);

                        dataFound();
                        connectedToInternet();
                    }
                    else
                        noDataFound();
                } else
                    noDataFound();

                refreshParentListLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ServerResponse<ParentItem>> call, Throwable t)
            {
                refreshParentListLayout.setRefreshing(false);
                serverError();
            }
        });
    }

    private void fetchAllRemovedParents()
    {
        if(!AppInstance.isConnected(getContext()))
        {
            DialogUtil.warningDialog(getContext(), "Network Error", "You are not connected to the internet!", false);
            noWifiConnectivity();
            refreshParentListLayout.setRefreshing(false);
            return;
        }
        else
            connectedToInternet();

        ParentAPI api = AppInstance.retrofit().create(ParentAPI.class);
        Call<ServerResponse<ParentItem>> call = api.getAllRemovedParents();
        call.enqueue(new Callback<ServerResponse<ParentItem>>() {
            @Override
            public void onResponse(Call<ServerResponse<ParentItem>> call, Response<ServerResponse<ParentItem>> response)
            {
                ServerResponse<ParentItem> server = response.body();

                if(server != null)
                {
                    List<ParentItem> parentItems = server.data;

                    if(parentItems != null && !server.hasError)
                    {
                        parentListAdapter.addParentItem(parentItems);

                        dataFound();
                        connectedToInternet();
                    }
                    else
                        noDataFound();
                } else
                    noDataFound();

                refreshParentListLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ServerResponse<ParentItem>> call, Throwable t)
            {
                refreshParentListLayout.setRefreshing(false);
                serverError();
            }
        });
    }
}
