package com.ceit.management.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DefaultDatabaseErrorHandler;
import android.media.DrmInitData;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ceit.management.AppInstance;
import com.ceit.management.R;
import com.ceit.management.adapter.ClassListAdapter;
import com.ceit.management.api.ClassAPI;
import com.ceit.management.api.TeacherAPI;
import com.ceit.management.model.ServerResponse;
import com.ceit.management.net.InternetReceiver;
import com.ceit.management.pojo.ClassItem;
import com.ceit.management.pojo.TeacherItem;
import com.ceit.management.util.Constants;
import com.ceit.management.util.DialogUtil;
import com.ceit.management.view.CurvedBottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

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

    private View root;

    @SuppressLint("NonConstantResourceId")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        root = inflater.inflate(R.layout.fragment_class, container, false);
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
                    Constants.DELETE_CLASS_TAB_ACTIVE = false;

                    classListAdapter.clear();
                    refreshClassListLayout.setRefreshing(true);
                    fetchAllClasses();
                break;

                case R.id.bottom_nav_removed:
                    Constants.DELETE_CLASS_TAB_ACTIVE = true;

                    classListAdapter.clear();
                    refreshClassListLayout.setRefreshing(true);
                    fetchAllRemovedClasses();
                break;
            }

            return false;
        });

        fabAdd.setOnClickListener(v -> {
            showAddClass();
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
    public void onResume()
    {
        super.onResume();

        IntentFilter filter = new IntentFilter(Constants.TRIGGER_REFRESH_LIST);
        getContext().registerReceiver(receiver, filter);
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

        if(Constants.DELETE_CLASS_TAB_ACTIVE)
            fetchAllRemovedClasses();
        else
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
            public void onResponse(@NotNull Call<ServerResponse<ClassItem>> call, @NotNull Response<ServerResponse<ClassItem>> response)
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
            public void onFailure(@NotNull Call<ServerResponse<ClassItem>> call, @NotNull Throwable t)
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
            public void onResponse(@NotNull Call<ServerResponse<ClassItem>> call, @NotNull Response<ServerResponse<ClassItem>> response)
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
            public void onFailure(@NotNull Call<ServerResponse<ClassItem>> call, @NotNull Throwable t)
            {
                refreshClassListLayout.setRefreshing(false);
                serverError();
            }
        });
    }

    private void showAddClass()
    {
        TextInputLayout name = root.findViewById(R.id.input_classname);
        AppCompatSpinner dept = root.findViewById(R.id.input_class_dept);
        AppCompatSpinner tea = root.findViewById(R.id.input_class_teacher);
        AppCompatButton add = root.findViewById(R.id.btn_add_class);
        AppCompatButton close = root.findViewById(R.id.btn_cancel);

        ConstraintLayout sheet = root.findViewById(R.id.sheet_edit);
        BottomSheetBehavior<ConstraintLayout> addSheet = BottomSheetBehavior.from(sheet);
        addSheet.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO, true);
        addSheet.setState(BottomSheetBehavior.STATE_EXPANDED);

        // Hide bottom nav and fab
        curvedBottomNavigationView.setVisibility(View.GONE);
        fabAdd.setVisibility(View.GONE);

        // Fetch all available teachers and populate the teacher spinner
        fetchTeachers();

        add.setOnClickListener(v -> {
            String str_name = name.getEditText().getText().toString();
            String str_dept = dept.getSelectedItem().toString().replaceAll("\\s+", "");
            String str_tea = tea.getSelectedItem().toString();

            if(str_name.isEmpty())
                name.setError("Class name should not be empty!");
            else
            {
                DialogUtil.progressDialog(getContext(), "Creating class...", getContext().getResources().getColor(R.color.themeColor), false);
                ClassAPI api = AppInstance.retrofit().create(ClassAPI.class);
                Call<ServerResponse<ClassItem>> call = api.addNewClass(ClassItem.newClass(str_name, str_tea, str_dept));
                call.enqueue(new Callback<ServerResponse<ClassItem>>() {
                    @Override
                    public void onResponse(@NotNull Call<ServerResponse<ClassItem>> call, @NotNull Response<ServerResponse<ClassItem>> response)
                    {
                        ServerResponse<ClassItem> server = response.body();
                        DialogUtil.dismissDialog();

                        if(server != null && !server.hasError)
                        {
                            addSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                            DialogUtil.successDialog(getContext(), "Success", "Class has been created successfully!", "Okay", false);
                            onRefresh();

                            curvedBottomNavigationView.setVisibility(View.VISIBLE);
                            fabAdd.setVisibility(View.VISIBLE);
                        }
                        else if(server != null && server.hasError)
                            DialogUtil.errorDialog(getContext(), "Failed", server.message, "Okay", false);
                        else
                            DialogUtil.errorDialog(getContext(), "Failed", "Server returned an unexpected result", "Okay", false);
                    }

                    @Override
                    public void onFailure(@NotNull Call<ServerResponse<ClassItem>> call, @NotNull Throwable t)
                    {
                        DialogUtil.dismissDialog();
                        DialogUtil.errorDialog(getContext(), "Failed", t.getMessage(), "Okay", false);
                    }
                });
            }
        });

        close.setOnClickListener(v -> {
            name.getEditText().clearComposingText();
            name.getEditText().clearFocus();
            name.setError("");
            addSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

            curvedBottomNavigationView.setVisibility(View.VISIBLE);
            fabAdd.setVisibility(View.VISIBLE);
        });
    }

    private void fetchTeachers()
    {
        TeacherAPI api = AppInstance.retrofit().create(TeacherAPI.class);
        Call<ServerResponse<TeacherItem>> call = api.getAllTeachers();
        call.enqueue(new Callback<ServerResponse<TeacherItem>>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse<TeacherItem>> call, @NotNull Response<ServerResponse<TeacherItem>> response)
            {
                AppCompatSpinner teacherSpinner = root.findViewById(R.id.input_class_teacher);
                ServerResponse<TeacherItem> server = response.body();

                if(server != null && !server.hasError)
                {
                    List<TeacherItem> teachers = server.data;
                    String[] names = new String[teachers.size()];

                    for(int i = 0; i < teachers.size(); i++)
                        names[i] = teachers.get(i).name;

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names);
                    teacherSpinner.setAdapter(adapter);
                    teacherSpinner.setSelection(0);
                }
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse<TeacherItem>> call, @NotNull Throwable t)
            {}
        });
    }

    BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getAction().equals(Constants.TRIGGER_REFRESH_LIST))
            {
                onRefresh();
                return;
            }
        }
    };
}
