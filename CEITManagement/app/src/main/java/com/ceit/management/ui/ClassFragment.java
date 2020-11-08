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
import androidx.core.widget.NestedScrollView;
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

@SuppressWarnings("ALL")
public class ClassFragment extends Fragment implements WaveSwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, InternetReceiver.OnInternetConnectionChangedListener
{
    private WaveSwipeRefreshLayout refreshClassListLayout;
    private CurvedBottomNavigationView curvedBottomNavigationView;
    private NestedScrollView scroller;
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
        scroller = root.findViewById(R.id.scroller);
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
        filter.addAction(Constants.TRIGGER_MODAL_OPEN);
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
        scroller.setVisibility(View.GONE);
    }

    private void noDataFound()
    {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.VISIBLE);
        overlayNoWifi.setVisibility(View.GONE);
        scroller.setVisibility(View.GONE);
    }

    private void serverError()
    {
        overlayServerError.setVisibility(View.VISIBLE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        scroller.setVisibility(View.GONE);
    }

    private void connectedToInternet()
    {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        scroller.setVisibility(View.VISIBLE);
    }

    private void dataFound()
    {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        scroller.setVisibility(View.VISIBLE);
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
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, getContext().getResources().getStringArray(R.array.dept_college_list));
        TextInputLayout name = root.findViewById(R.id.input_classname);
        AppCompatSpinner dept = root.findViewById(R.id.input_class_dept);
        AppCompatSpinner tea = root.findViewById(R.id.input_class_teacher);
        AppCompatButton add = root.findViewById(R.id.btn_add_class);
        AppCompatButton close = root.findViewById(R.id.btn_cancel);

        ConstraintLayout sheet = root.findViewById(R.id.sheet_edit);
        BottomSheetBehavior<ConstraintLayout> addSheet = BottomSheetBehavior.from(sheet);
        addSheet.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO, true);
        addSheet.setState(BottomSheetBehavior.STATE_EXPANDED);

        dept.setAdapter(deptAdapter);
        dept.setSelection(0);

        // Hide bottom nav and fab
        curvedBottomNavigationView.setVisibility(View.GONE);
        fabAdd.setVisibility(View.GONE);

        // Fetch all available teachers and populate the teacher spinner
        fetchTeachers("");

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
            name.getEditText().setText("");
            name.getEditText().clearFocus();
            name.setError("");
            addSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

            tea.setSelection(0);
            dept.setSelection(0);

            name.setEnabled(true);
            dept.setEnabled(true);
            tea.setEnabled(true);

            curvedBottomNavigationView.setVisibility(View.VISIBLE);
            fabAdd.setVisibility(View.VISIBLE);
        });
    }

    private void fetchTeachers(String selected)
    {
        DialogUtil.progressDialog(getContext(), "Preparing form...", getContext().getResources().getColor(R.color.themeColor), false);
        TeacherAPI api = AppInstance.retrofit().create(TeacherAPI.class);
        Call<ServerResponse<TeacherItem>> call = api.getAllTeachers();
        call.enqueue(new Callback<ServerResponse<TeacherItem>>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse<TeacherItem>> call, @NotNull Response<ServerResponse<TeacherItem>> response)
            {
                DialogUtil.dismissDialog();
                AppCompatSpinner teacherSpinner = root.findViewById(R.id.input_class_teacher);
                ServerResponse<TeacherItem> server = response.body();

                if(server != null && !server.hasError)
                {
                    List<TeacherItem> teachers = server.data;
                    String[] names = new String[teachers.size()];
                    int selection = 0;
                    for(int i = 0; i < teachers.size(); i++)
                    {
                        if(teachers.get(i).name.toLowerCase().equals(selected.toLowerCase()))
                            selection = i;
                        names[i] = teachers.get(i).name;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names);
                    teacherSpinner.setAdapter(adapter);
                    teacherSpinner.setSelection(selection);
                }
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse<TeacherItem>> call, @NotNull Throwable t)
            {
                DialogUtil.dismissDialog();
                DialogUtil.errorDialog(getContext(), "Preparation Failed", t.getMessage(), "Okay", false);
            }
        });
    }

    private void editClass(ClassItem item)
    {
        String[] departments = getContext().getResources().getStringArray(R.array.dept_college_list);
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, departments);
        TextInputLayout name = root.findViewById(R.id.input_classname);
        AppCompatSpinner dept = root.findViewById(R.id.input_class_dept);
        AppCompatSpinner tea = root.findViewById(R.id.input_class_teacher);
        AppCompatButton save = root.findViewById(R.id.btn_add_class);
        AppCompatButton close = root.findViewById(R.id.btn_cancel);

        save.setText("Save Class");
        fetchTeachers(item.teacher);
        name.getEditText().setText(item.name);

        int selection = 0;
        dept.setAdapter(deptAdapter);
        for(String department : departments)
            if(department.replaceAll("\\s+", "").toLowerCase().equals(item.department.replaceAll("\\s+", "").toLowerCase()))
            {
                dept.setSelection(selection);
                break;
            }
            else
                selection += 1;

        ConstraintLayout sheet = root.findViewById(R.id.sheet_edit);
        BottomSheetBehavior<ConstraintLayout> editSheet = BottomSheetBehavior.from(sheet);
        editSheet.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO, true);
        editSheet.setState(BottomSheetBehavior.STATE_EXPANDED);

        // Hide bottom nav and fab
        curvedBottomNavigationView.setVisibility(View.GONE);
        fabAdd.setVisibility(View.GONE);

        save.setOnClickListener(v -> {
            if(name.getEditText().toString().isEmpty() || name.getEditText().toString().length() <= 4)
                DialogUtil.errorDialog(getContext(), "Invalide Name", "Invalid class name, name must be more than 4 characters long", "Okay", false);
            else
            {
                DialogUtil.progressDialog(getContext(), "Updating class...", getContext().getResources().getColor(R.color.themeColor), false);
                ClassAPI api = AppInstance.retrofit().create(ClassAPI.class);
                Call<ServerResponse<ClassItem>> call = api.updateClass(item.id, ClassItem.newClass(
                        name.getEditText().getText().toString(),
                        tea.getSelectedItem().toString(),
                        dept.getSelectedItem().toString()
                ));
                call.enqueue(new Callback<ServerResponse<ClassItem>>() {
                    @Override
                    public void onResponse(Call<ServerResponse<ClassItem>> call, Response<ServerResponse<ClassItem>> response)
                    {
                        ServerResponse<ClassItem> server = response.body();

                        if(server != null && !server.hasError) {
                            DialogUtil.successDialog(getContext(), "Update Successful", server.message, "Okay", false);
                            close.callOnClick();
                            onRefresh();
                        }
                        else if(server != null && server.hasError)
                            DialogUtil.errorDialog(getContext(), "Update Failed", server.message, "Okay", false);
                        else
                            DialogUtil.errorDialog(getContext(), "Update Failed", "Server returned an unexpected result", "Okay", false);
                    }

                    @Override
                    public void onFailure(Call<ServerResponse<ClassItem>> call, Throwable t) {
                        DialogUtil.errorDialog(getContext(), "Update Failed", t.getMessage(), "Okay", false);
                    }
                });
            }
        });

        close.setOnClickListener(v -> {
            editSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

            name.getEditText().setText("");
            name.getEditText().clearFocus();
            name.setError("");

            tea.setSelection(0);
            dept.setSelection(0);

            name.setEnabled(true);
            dept.setEnabled(true);
            tea.setEnabled(true);

            curvedBottomNavigationView.setVisibility(View.VISIBLE);
            fabAdd.setVisibility(View.VISIBLE);
        });
    }

    private void viewClass(ClassItem item)
    {
        ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{item.teacher});
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{item.department});
        TextInputLayout name = root.findViewById(R.id.input_classname);
        AppCompatSpinner dept = root.findViewById(R.id.input_class_dept);
        AppCompatSpinner tea = root.findViewById(R.id.input_class_teacher);
        AppCompatButton edit = root.findViewById(R.id.btn_add_class);
        AppCompatButton close = root.findViewById(R.id.btn_cancel);

        name.setEnabled(false);
        dept.setEnabled(false);
        tea.setEnabled(false);

        name.getEditText().setText(item.name);
        dept.setAdapter(deptAdapter);
        tea.setAdapter(teacherAdapter);
        tea.setSelection(0);
        dept.setSelection(0);

        edit.setText("Edit Class");
        close.setText("Close");

        ConstraintLayout sheet = root.findViewById(R.id.sheet_edit);
        BottomSheetBehavior<ConstraintLayout> addSheet = BottomSheetBehavior.from(sheet);
        addSheet.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO, true);
        addSheet.setState(BottomSheetBehavior.STATE_EXPANDED);

        // Hide bottom nav and fab
        curvedBottomNavigationView.setVisibility(View.GONE);
        fabAdd.setVisibility(View.GONE);

        edit.setOnClickListener(v -> {
            close.callOnClick();
            editClass(item);
        });

        close.setOnClickListener(v -> {
            addSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

            name.getEditText().setText("");
            name.getEditText().clearFocus();
            name.setError("");

            tea.setSelection(0);
            dept.setSelection(0);

            name.setEnabled(true);
            dept.setEnabled(true);
            tea.setEnabled(true);

            curvedBottomNavigationView.setVisibility(View.VISIBLE);
            fabAdd.setVisibility(View.VISIBLE);
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

            if(intent.getAction().equals(Constants.TRIGGER_MODAL_OPEN))
            {
                int action = intent.getExtras().getInt(Constants.KEY_TRIGGER_ACTION_TYPE);
                int type = intent.getExtras().getInt(Constants.KEY_TRIGGER_MODAL_TYPE);
                int id = intent.getExtras().getInt(Constants.KEY_TRIGGER_MODAL_VIEW);

                if(type != 4)
                    return;

                DialogUtil.progressDialog(getContext(), "Loading class...", getContext().getResources().getColor(R.color.themeColor), false);
                ClassAPI api = AppInstance.retrofit().create(ClassAPI.class);
                Call<ServerResponse<ClassItem>> call = api.getClass(id);
                call.enqueue(new Callback<ServerResponse<ClassItem>>() {
                    @Override
                    public void onResponse(Call<ServerResponse<ClassItem>> call, Response<ServerResponse<ClassItem>> response)
                    {
                        DialogUtil.dismissDialog();
                        ServerResponse<ClassItem> server = response.body();

                        if(server != null && !server.hasError)
                        {
                            List<ClassItem> items = server.data;

                            if(items != null && action == 0)
                                viewClass(items.get(0));
                            else
                                DialogUtil.errorDialog(context, "Error", server.message, "Okay", false);
                        }
                    }

                    @Override
                    public void onFailure(Call<ServerResponse<ClassItem>> call, Throwable t)
                    {
                        DialogUtil.errorDialog(context, "Server Error", t.getMessage(), false);
                    }
                });
            }
        }
    };
}
