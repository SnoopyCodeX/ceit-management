package com.ceit.management.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ceit.management.AppInstance;
import com.ceit.management.R;
import com.ceit.management.adapter.StudentListAdapter;
import com.ceit.management.api.StudentAPI;
import com.ceit.management.model.ServerResponse;
import com.ceit.management.net.BlurImage;
import com.ceit.management.net.InternetReceiver;
import com.ceit.management.pojo.StudentItem;
import com.ceit.management.util.Constants;
import com.ceit.management.util.DialogUtil;
import com.ceit.management.view.CurvedBottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("ALL")
public class StudentsFragment extends Fragment implements WaveSwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, InternetReceiver.OnInternetConnectionChangedListener {
    private WaveSwipeRefreshLayout refreshStudentListLayout;
    private CurvedBottomNavigationView curvedBottomNavigationView;
    private FloatingActionButton fabAdd;
    private LinearLayout overlayNoWifi;
    private LinearLayout overlayNodataFound;
    private LinearLayout overlayServerError;
    private RecyclerView listStudents;
    private StudentListAdapter studentListAdapter;
    private View root;
    private boolean editMode = false;

    @SuppressLint("NonConstantResourceId")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_students, container, false);
        refreshStudentListLayout = root.findViewById(R.id.refresh_students_list);
        curvedBottomNavigationView = root.findViewById(R.id.bottom_nav_students);
        fabAdd = root.findViewById(R.id.fab);
        overlayNoWifi = root.findViewById(R.id.no_wifi_layout);
        overlayNodataFound = root.findViewById(R.id.no_data_layout);
        overlayServerError = root.findViewById(R.id.server_error_layout);
        listStudents = root.findViewById(R.id.list_students);

        refreshStudentListLayout.setColorSchemeColors(getContext().getResources().getColor(R.color.white));
        refreshStudentListLayout.setWaveColor(getContext().getResources().getColor(R.color.themeColor));
        refreshStudentListLayout.setOnRefreshListener(this);
        listStudents.setHasFixedSize(true);

        curvedBottomNavigationView.setOnNavigationItemSelectedListener((MenuItem item) -> {
            switch (item.getItemId()) {
                case R.id.bottom_nav_students:
                    Constants.DELETE_STUDENT_TAB_ACTIVE = false;

                    studentListAdapter.clear();
                    refreshStudentListLayout.setRefreshing(true);
                    fetchAllStudents();
                    break;

                case R.id.bottom_nav_removed:
                    Constants.DELETE_STUDENT_TAB_ACTIVE = true;

                    studentListAdapter.clear();
                    refreshStudentListLayout.setRefreshing(true);
                    fetchAllRemovedStudents();
                    break;
            }

            return false;
        });

        fabAdd.setOnClickListener((View view) -> {

        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        listStudents.setLayoutManager(layoutManager);

        studentListAdapter = new StudentListAdapter(new ArrayList<>());
        listStudents.setAdapter(studentListAdapter);
        fetchAllStudents();

        AppInstance.hookUpConnectivityListener(this);
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search student...");
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter modelPersonViewFilter = new IntentFilter(Constants.TRIGGER_MODAL_OPEN);
        modelPersonViewFilter.addAction(Constants.TRIGGER_REFRESH_LIST);
        getContext().registerReceiver(modalViewPersonTriggerReceiver, modelPersonViewFilter);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        studentListAdapter.getFilter().filter(newText);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        studentListAdapter.getFilter().filter(query);
        return true;
    }

    @Override
    public void onRefresh() {
        studentListAdapter.clear();

        if(Constants.DELETE_STUDENT_TAB_ACTIVE)
            fetchAllRemovedStudents();
        else
            fetchAllStudents();
    }

    @Override
    public void onInternetConnectionChanged(boolean isConnected) {
        if (!isConnected) {
            DialogUtil.warningDialog(getContext(), "Network Error", "You are not connected to the internet!", false);
            noWifiConnectivity();
        } else {
            DialogUtil.dismissDialog();
            connectedToInternet();
        }
    }

    private void noWifiConnectivity() {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.VISIBLE);
        listStudents.setVisibility(View.GONE);
    }

    private void noDataFound() {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.VISIBLE);
        overlayNoWifi.setVisibility(View.GONE);
        listStudents.setVisibility(View.GONE);
    }

    private void serverError() {
        overlayServerError.setVisibility(View.VISIBLE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        listStudents.setVisibility(View.GONE);
    }

    private void connectedToInternet() {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        listStudents.setVisibility(View.VISIBLE);
    }

    private void dataFound() {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        listStudents.setVisibility(View.VISIBLE);
    }

    private void fetchAllStudents() {
        if (!AppInstance.isConnected(getContext())) {
            DialogUtil.warningDialog(getContext(), "Network Error", "You are not connected to internet!", false);
            noWifiConnectivity();
            return;
        }

        StudentAPI api = AppInstance.retrofit().create(StudentAPI.class);
        Call<ServerResponse<StudentItem>> call = api.getAllStudents();
        call.enqueue(new Callback<ServerResponse<StudentItem>>() {
            @Override
            public void onResponse(Call<ServerResponse<StudentItem>> call, Response<ServerResponse<StudentItem>> response) {
                ServerResponse<StudentItem> server = response.body();
                refreshStudentListLayout.setRefreshing(false);

                if (server != null && !server.hasError) {
                    List<StudentItem> studentItems = server.data;
                    studentListAdapter.addStudentItem(studentItems);

                    connectedToInternet();
                    dataFound();
                } else
                    noDataFound();
            }

            @Override
            public void onFailure(Call<ServerResponse<StudentItem>> call, Throwable t) {
                DialogUtil.errorDialog(getContext(), t.getMessage(), "Okay");
                refreshStudentListLayout.setRefreshing(false);
                serverError();
            }
        });
    }

    private void fetchAllRemovedStudents() {
        if (!AppInstance.isConnected(getContext())) {
            DialogUtil.warningDialog(getContext(), "Network Error", "You are not connected to internet!", false);
            return;
        }

        StudentAPI api = AppInstance.retrofit().create(StudentAPI.class);
        Call<ServerResponse<StudentItem>> call = api.getAllRemovedStudents();
        call.enqueue(new Callback<ServerResponse<StudentItem>>() {
            @Override
            public void onResponse(Call<ServerResponse<StudentItem>> call, Response<ServerResponse<StudentItem>> response) {
                ServerResponse<StudentItem> server = response.body();
                refreshStudentListLayout.setRefreshing(false);

                if (server != null) {
                    List<StudentItem> studentItems = server.data;

                    if (studentItems != null && !server.hasError) {
                        studentListAdapter.addStudentItem(studentItems);

                        connectedToInternet();
                        dataFound();
                    } else
                        noDataFound();
                } else
                    noDataFound();
            }

            @Override
            public void onFailure(Call<ServerResponse<StudentItem>> call, Throwable t) {
                DialogUtil.errorDialog(getContext(), t.getMessage(), "Okay");
                refreshStudentListLayout.setRefreshing(false);
                serverError();
            }
        });
    }

    private void updateStudent(StudentItem student)
    {

    }

    private void viewStudent(StudentItem student)
    {
        ShapeableImageView profile = root.findViewById(R.id.v_profile);
        ImageView cover = root.findViewById(R.id.background);
        Button close = root.findViewById(R.id.v_close);
        Button edit = root.findViewById(R.id.btn_edit_profile);
        TextInputEditText name = root.findViewById(R.id.v_name);
        TextView birthday = root.findViewById(R.id.v_birthdate);
        TextView gender = root.findViewById(R.id.v_gender);
        TextView email = root.findViewById(R.id.v_email);
        TextView number = root.findViewById(R.id.v_number);
        TextView address = root.findViewById(R.id.v_address);
        TextView section = root.findViewById(R.id.v_section);
        TextView religion = root.findViewById(R.id.v_religion);

        profile.setShapeAppearanceModel(ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, 160).build());

        //hide fab and bottom nav
        curvedBottomNavigationView.setVisibility(View.GONE);
        fabAdd.setVisibility(View.GONE);

        BlurImage.with(getContext())
                .setBlurRadius(25f)
                .setBitmapScale(0.6f)
                .blurFromUri(student.photo)
                .into(cover);

        Glide.with(getContext())
                .load(student.photo)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.student)
                .error(R.drawable.student)
                .into(profile);

        name.setText(student.name);
        birthday.setText(student.birthday);
        gender.setText(student.gender);
        email.setText(student.email);
        number.setText(student.contactNumber);
        address.setText(student.address);
        section.setText(student.section);
        religion.setText(student.religion);

        ConstraintLayout bottomSheet = root.findViewById(R.id.sheet_info);
        BottomSheetBehavior<ConstraintLayout> sheet = BottomSheetBehavior.from(bottomSheet);
        sheet.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO, true);
        sheet.setState(BottomSheetBehavior.STATE_EXPANDED);

        close.setOnClickListener(v -> {
            sheet.setState(BottomSheetBehavior.STATE_HIDDEN);

            curvedBottomNavigationView.setVisibility(View.VISIBLE);
            fabAdd.setVisibility(View.VISIBLE);
        });

        edit.setOnClickListener(v -> {
            sheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        });
    }

    private final BroadcastReceiver modalViewPersonTriggerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent data)
        {
            if(data.getAction().equals(Constants.TRIGGER_REFRESH_LIST))
            {
                onRefresh();
                return;
            }

            int studentId = data.getExtras().getInt(Constants.KEY_TRIGGER_MODAL_VIEW);
            int modalType = data.getExtras().getInt(Constants.KEY_TRIGGER_MODAL_TYPE);


            if(modalType != 2)
            {
                refreshStudentListLayout.setRefreshing(false);
                return;
            }

            DialogUtil.progressDialog(getContext(), "Loading student...", getContext().getResources().getColor(R.color.themeColor), false);
            StudentAPI api = AppInstance.retrofit().create(StudentAPI.class);
            Call<ServerResponse<StudentItem>> call = api.getStudent(studentId);
            call.enqueue(new Callback<ServerResponse<StudentItem>>() {
                @Override
                public void onResponse(@NotNull Call<ServerResponse<StudentItem>> call, @NotNull Response<ServerResponse<StudentItem>> response)
                {
                    DialogUtil.dismissDialog();
                    ServerResponse<StudentItem> server = response.body();
                    refreshStudentListLayout.setRefreshing(false);

                    if(server != null && !server.hasError)
                    {
                        List<StudentItem> studentItems = server.data;

                        if(studentItems != null)
                            viewStudent(studentItems.get(0));
                        else
                            DialogUtil.errorDialog(context, server.message, "Okay");
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ServerResponse<StudentItem>> call, @NotNull Throwable t)
                {
                    DialogUtil.errorDialog(context, "Server Error", t.getMessage(), false);
                    refreshStudentListLayout.setRefreshing(false);
                }
            });
        }
    };
}
