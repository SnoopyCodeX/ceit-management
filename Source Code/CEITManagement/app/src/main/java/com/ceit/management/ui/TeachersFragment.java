package com.ceit.management.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ceit.management.AppInstance;
import com.ceit.management.R;
import com.ceit.management.adapter.TeacherListAdapter;
import com.ceit.management.api.TeacherAPI;
import com.ceit.management.model.ServerResponse;
import com.ceit.management.net.BlurImage;
import com.ceit.management.net.InternetReceiver;
import com.ceit.management.pojo.TeacherItem;
import com.ceit.management.util.Constants;
import com.ceit.management.util.DialogUtil;
import com.ceit.management.util.ImageUtil;
import com.ceit.management.view.CurvedBottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("All")
public class TeachersFragment extends Fragment implements WaveSwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, InternetReceiver.OnInternetConnectionChangedListener
{
    private WaveSwipeRefreshLayout refreshTeacherListLayout;
    private CurvedBottomNavigationView curvedBottomNavigationView;
    private NestedScrollView scroller;
    private FloatingActionButton fabAdd;
    private LinearLayout overlayNoWifi;
    private LinearLayout overlayNodataFound;
    private LinearLayout overlayServerError;
    private RecyclerView listTeachers;
    private TeacherListAdapter teacherListAdapter;

    private String base64Image;
    private View root;

    @SuppressLint("NonConstantResourceId")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        root = inflater.inflate(R.layout.fragment_teachers, container, false);
        refreshTeacherListLayout = root.findViewById(R.id.refresh_teachers_list);
        curvedBottomNavigationView = root.findViewById(R.id.bottom_nav_teachers);
        scroller = root.findViewById(R.id.scroller);
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
                    Constants.DELETE_TEACHER_TAB_ACTIVE = false;

                    teacherListAdapter.clear();
                    refreshTeacherListLayout.setRefreshing(true);
                    fetchAllTeachers();
                break;

                case R.id.bottom_nav_removed:
                    Constants.DELETE_TEACHER_TAB_ACTIVE = true;

                    teacherListAdapter.clear();
                    refreshTeacherListLayout.setRefreshing(true);
                    fetchAllRemovedTeachers();
                break;
            }

            return false;
        });

        fabAdd.setOnClickListener(v -> {
            showAddForm();
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

        if(Constants.DELETE_TEACHER_TAB_ACTIVE)
            fetchAllRemovedTeachers();
        else
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if(requestCode == Constants.CODE_PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK)
        {
            ShapeableImageView profile = root.findViewById(R.id.e_profile);

            Bitmap photo = ImageUtil.imageUriToBitmap(getContext(), data.getData());
            base64Image = ImageUtil.imageToBase64(photo);

            Glide.with(getContext())
                    .load(photo)
                    .circleCrop()
                    .into(profile);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.TRIGGER_REFRESH_LIST);
        filter.addAction(Constants.TRIGGER_MODAL_OPEN);
        getContext().registerReceiver(receiver, filter);
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
            public void onResponse(@NotNull Call<ServerResponse<TeacherItem>> call, @NotNull Response<ServerResponse<TeacherItem>> response)
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
            public void onFailure(@NotNull Call<ServerResponse<TeacherItem>> call, @NotNull Throwable t)
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
            public void onResponse(@NotNull Call<ServerResponse<TeacherItem>> call, @NotNull Response<ServerResponse<TeacherItem>> response)
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
            public void onFailure(@NotNull Call<ServerResponse<TeacherItem>> call, @NotNull Throwable t)
            {
                refreshTeacherListLayout.setRefreshing(false);
                serverError();
            }
        });
    }

    private void viewTeacher(TeacherItem teacher)
    {
        ShapeableImageView profile = root.findViewById(R.id.e_profile);
        ImageView cover = root.findViewById(R.id.e_background);
        ImageView change = root.findViewById(R.id.change_photo);
        Button edit = root.findViewById(R.id.btn_save_cancel_profile);
        Button close = root.findViewById(R.id.e_close);

        TextView birthday = root.findViewById(R.id.e_birthdate);
        Spinner gender = root.findViewById(R.id.e_gender);

        TextInputEditText name = root.findViewById(R.id.e_name);
        TextInputEditText rank = root.findViewById(R.id.e_rank);
        TextInputEditText religion = root.findViewById(R.id.e_religion);
        TextInputEditText email = root.findViewById(R.id.e_email);
        TextInputEditText number = root.findViewById(R.id.e_number);
        TextInputEditText address = root.findViewById(R.id.e_address);

        change.setVisibility(View.GONE);

        BlurImage.with(getContext())
                .setBlurRadius(25f)
                .setBitmapScale(0.6f)
                .blurFromUri(teacher.photo)
                .into(cover);

        Glide.with(getContext())
                .load(teacher.photo)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(teacher.gender.equals("Male") ? R.drawable.male : R.drawable.female)
                .error(teacher.gender.equals("Male") ? R.drawable.male : R.drawable.female)
                .into(profile);

        edit.setText("Edit Teacher");

        String[] genders = {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, genders);
        gender.setAdapter(adapter);
        gender.setSelection(teacher.gender.equals("Male") ? 0 : 1);

        name.setText(teacher.name);
        rank.setText(teacher.rank);
        religion.setText(teacher.religion);
        email.setText(teacher.email);
        number.setText(teacher.contactNumber);
        address.setText(teacher.address);
        birthday.setText(teacher.birthday);

        name.setEnabled(false);
        rank.setEnabled(false);
        religion.setEnabled(false);
        email.setEnabled(false);
        number.setEnabled(false);
        address.setEnabled(false);
        gender.setEnabled(false);
        birthday.setEnabled(false);

        // Hide fab and bottom nav
        fabAdd.setVisibility(View.GONE);
        curvedBottomNavigationView.setVisibility(View.GONE);

        ConstraintLayout sheet = root.findViewById(R.id.sheet_edit);
        BottomSheetBehavior<ConstraintLayout> editSheet = BottomSheetBehavior.from(sheet);
        editSheet.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO, true);
        editSheet.setState(BottomSheetBehavior.STATE_EXPANDED);

        edit.setOnClickListener(v -> {
            close.callOnClick();
            editTeacher(teacher);
        });

        close.setOnClickListener(v -> {
            editSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
            name.setEnabled(true);
            rank.setEnabled(true);
            religion.setEnabled(true);
            email.setEnabled(true);
            number.setEnabled(true);
            address.setEnabled(true);
            gender.setEnabled(true);
            birthday.setEnabled(true);

            name.setText("");
            rank.setText("");
            religion.setText("");
            email.setText("");
            number.setText("");
            address.setText("");
            birthday.setText("Birth Date");

            fabAdd.setVisibility(View.VISIBLE);
            curvedBottomNavigationView.setVisibility(View.VISIBLE);
        });
    }

    private void editTeacher(TeacherItem teacher)
    {
        ShapeableImageView profile = root.findViewById(R.id.e_profile);
        ImageView cover = root.findViewById(R.id.e_background);
        ImageView change = root.findViewById(R.id.change_photo);
        Button save = root.findViewById(R.id.btn_save_cancel_profile);
        Button close = root.findViewById(R.id.e_close);

        TextView birthday = root.findViewById(R.id.e_birthdate);
        Spinner gender = root.findViewById(R.id.e_gender);

        TextInputEditText name = root.findViewById(R.id.e_name);
        TextInputEditText rank = root.findViewById(R.id.e_rank);
        TextInputEditText religion = root.findViewById(R.id.e_religion);
        TextInputEditText email = root.findViewById(R.id.e_email);
        TextInputEditText number = root.findViewById(R.id.e_number);
        TextInputEditText address = root.findViewById(R.id.e_address);
        cover.setBackgroundColor(getContext().getResources().getColor(R.color.themeColor));

        Glide.with(getContext())
                .load(teacher.photo)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(teacher.gender.equals("Male") ? R.drawable.male : R.drawable.female)
                .error(teacher.gender.equals("Male") ? R.drawable.male : R.drawable.female)
                .into(profile);

        save.setText("Update Teacher");
        change.setVisibility(View.VISIBLE);

        String[] genders = {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, genders);
        gender.setAdapter(adapter);
        gender.setSelection(teacher.gender.equals("Male") ? 0 : 1);

        // Hide fab and bottom nav
        fabAdd.setVisibility(View.GONE);
        curvedBottomNavigationView.setVisibility(View.GONE);

        name.setText(teacher.name);
        rank.setText(teacher.rank);
        religion.setText(teacher.religion);
        email.setText(teacher.email);
        number.setText(teacher.contactNumber);
        address.setText(teacher.address);
        birthday.setText(teacher.birthday);

        ConstraintLayout sheet = root.findViewById(R.id.sheet_edit);
        BottomSheetBehavior<ConstraintLayout> editSheet = BottomSheetBehavior.from(sheet);
        editSheet.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO, true);
        editSheet.setState(BottomSheetBehavior.STATE_EXPANDED);

        birthday.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date("Jan 1, 1950"));
            DatePickerDialog picker = new DatePickerDialog(getContext(), (view, year, month, date) -> {
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(year, month, date);

                String strBday = DateFormat.getDateInstance().format(newCalendar.getTime());
                birthday.setText(strBday);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            Calendar def = Calendar.getInstance();
            def.setTime(new Date(birthday.getText().toString()));
            picker.getDatePicker().updateDate(def.get(Calendar.YEAR), def.get(Calendar.MONTH), def.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });

        change.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(pickIntent, "Choose image..."), Constants.CODE_PICK_IMAGE);
        });

        close.setOnClickListener(v -> {
            name.clearFocus();
            religion.clearFocus();
            email.clearFocus();
            number.clearFocus();
            address.clearFocus();
            rank.clearFocus();;

            name.setText("");
            religion.setText("");
            email.setText("");
            number.setText("");
            address.setText("");
            rank.setText("");

            gender.setSelection(0);
            profile.setImageResource(R.drawable.parents);
            birthday.setText(R.string.hint_birth_date);
            editSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

            fabAdd.setVisibility(View.VISIBLE);
            curvedBottomNavigationView.setVisibility(View.VISIBLE);
        });

        save.setOnClickListener(v -> {
            try {
                DialogUtil.progressDialog(getContext(), "Updating teacher...", getContext().getResources().getColor(R.color.themeColor), false);
                PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.createInstance(getContext());

                String str_name = name.getText().toString();
                String str_religion = religion.getText().toString();
                String str_email = email.getText().toString();
                String str_number = number.getText().toString();
                String str_address = address.getText().toString();
                String str_rank = rank.getText().toString();

                String str_bday = birthday.getText().toString();
                String str_gender = gender.getSelectedItem().toString();

                if(str_name.isEmpty() || !str_name.matches("([a-zA-Z\\.\\s]+)$") || !(str_name.length() >= 4))
                    DialogUtil.errorDialog(getContext(), "Error", "Please enter a valid name", "Okay", false);
                else if(str_bday.toLowerCase().equals("birth date"))
                    DialogUtil.errorDialog(getContext(), "Error", "Please set a valid birth date", "Okay", false);
                else if(str_religion.isEmpty() || !str_religion.matches("([a-zA-Z\\-\\s]+)$") || !(str_religion.length() >= 4))
                    DialogUtil.errorDialog(getContext(), "Error", "Please add a valid religion", "Okay", false);
                else if(str_email.isEmpty() || !str_email.matches("([a-zA-Z0-9\\-\\_\\.\\@]+)$"))
                    DialogUtil.errorDialog(getContext(), "Error", "Please add a valid email address", "Okay", false);
                else if(str_number.isEmpty() || !phoneNumberUtil.isValidNumber(phoneNumberUtil.parse(str_number, "PH")))
                    DialogUtil.errorDialog(getContext(), "Error", "Please enter a valid Philippine phone number", "Okay", false);
                else if(str_address.isEmpty() || !(str_address.length() >= 5))
                    DialogUtil.errorDialog(getContext(), "Error", "Please enter a valid address", "Okay", false);
                else if(str_rank.isEmpty() || !(str_rank.length() >= 4))
                    DialogUtil.errorDialog(getContext(), "Error", "Please enter a valid rank", "Okay", false);
                else
                {
                    TeacherAPI api = AppInstance.retrofit().create(TeacherAPI.class);
                    Call<ServerResponse<TeacherItem>> call = api.updateTeacher(teacher.id, TeacherItem.newTeacher(
                            str_name,
                            str_rank,
                            str_gender,
                            str_email,
                            "+63" + phoneNumberUtil.parse(str_number, "PH").getNationalNumber(),
                            str_religion,
                            str_address,
                            str_bday,
                            base64Image
                    ));
                    call.enqueue(new Callback<ServerResponse<TeacherItem>>() {
                        @Override
                        public void onResponse(Call<ServerResponse<TeacherItem>> call, Response<ServerResponse<TeacherItem>> response)
                        {
                            DialogUtil.dismissDialog();
                            ServerResponse<TeacherItem> server = response.body();

                            if(server != null && !server.hasError) {
                                close.callOnClick();
                                onRefresh();
                                DialogUtil.successDialog(getContext(), "Success", server.message, "Okay", false);
                            }
                            else if(server != null && server.hasError)
                                DialogUtil.errorDialog(getContext(), "Error", server.message, "Okay", false);
                            else
                                DialogUtil.errorDialog(getContext(), "Error", "Server returned an unexpected result", "Okay", false);
                        }

                        @Override
                        public void onFailure(Call<ServerResponse<TeacherItem>> call, Throwable t)
                        {
                            DialogUtil.dismissDialog();
                            DialogUtil.errorDialog(getContext(), "Error", t.getMessage(), "Okay", false);
                        }
                    });
                }

            } catch(Exception e) {
                DialogUtil.errorDialog(getContext(), "Error", e.getMessage(), "Okay", false);
                e.printStackTrace();
            }
        });
    }

    private void showAddForm()
    {
        ShapeableImageView profile = root.findViewById(R.id.e_profile);
        ImageView cover = root.findViewById(R.id.e_background);
        ImageView change = root.findViewById(R.id.change_photo);
        Button add = root.findViewById(R.id.btn_save_cancel_profile);
        Button close = root.findViewById(R.id.e_close);

        TextView birthday = root.findViewById(R.id.e_birthdate);
        Spinner gender = root.findViewById(R.id.e_gender);

        TextInputEditText name = root.findViewById(R.id.e_name);
        TextInputEditText rank = root.findViewById(R.id.e_rank);
        TextInputEditText religion = root.findViewById(R.id.e_religion);
        TextInputEditText email = root.findViewById(R.id.e_email);
        TextInputEditText number = root.findViewById(R.id.e_number);
        TextInputEditText address = root.findViewById(R.id.e_address);
        cover.setBackgroundColor(getContext().getResources().getColor(R.color.themeColor));

        add.setText("Add Teacher");
        Glide.with(getContext())
                .load(R.drawable.teacher)
                .circleCrop()
                .into(profile);

        String[] genders = {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, genders);
        gender.setAdapter(adapter);
        gender.setSelection(0);

        // Hide fab and bottom nav
        fabAdd.setVisibility(View.GONE);
        curvedBottomNavigationView.setVisibility(View.GONE);

        ConstraintLayout addLayout = root.findViewById(R.id.sheet_edit);
        BottomSheetBehavior<ConstraintLayout> addSheet = BottomSheetBehavior.from(addLayout);
        addSheet.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO, true);
        addSheet.setState(BottomSheetBehavior.STATE_EXPANDED);

        birthday.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date("Jan 1, 1950"));
            DatePickerDialog picker = new DatePickerDialog(getContext(), (view, year, month, date) -> {
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(year, month, date);

                String strBday = DateFormat.getDateInstance().format(newCalendar.getTime());
                birthday.setText(strBday);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });

        change.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(pickIntent, "Choose image..."), Constants.CODE_PICK_IMAGE);
        });

        close.setOnClickListener(v -> {
            name.clearFocus();
            religion.clearFocus();
            email.clearFocus();
            number.clearFocus();
            address.clearFocus();
            rank.clearFocus();;

            name.setText("");
            religion.setText("");
            email.setText("");
            number.setText("");
            address.setText("");
            rank.setText("");

            gender.setSelection(0);
            profile.setImageResource(R.drawable.parents);
            birthday.setText(R.string.hint_birth_date);
            addSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

            fabAdd.setVisibility(View.VISIBLE);
            curvedBottomNavigationView.setVisibility(View.VISIBLE);
        });

        add.setOnClickListener(v -> {
            try {
                DialogUtil.progressDialog(getContext(), "Adding teacher...", getContext().getResources().getColor(R.color.themeColor), false);
                PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.createInstance(getContext());

                String str_name = name.getText().toString();
                String str_religion = religion.getText().toString();
                String str_email = email.getText().toString();
                String str_number = number.getText().toString();
                String str_address = address.getText().toString();
                String str_rank = rank.getText().toString();

                String str_bday = birthday.getText().toString();
                String str_gender = gender.getSelectedItem().toString();

                if(base64Image == null)
                    DialogUtil.errorDialog(getContext(), "Error", "Please add an image", "Okay", false);
                else if(str_name.isEmpty() || !str_name.matches("([a-zA-Z\\.\\s]+)$") || !(str_name.length() >= 4))
                    DialogUtil.errorDialog(getContext(), "Error", "Please enter a valid name", "Okay", false);
                else if(str_bday.toLowerCase().equals("birth date"))
                    DialogUtil.errorDialog(getContext(), "Error", "Please set a valid birth date", "Okay", false);
                else if(str_religion.isEmpty() || !str_religion.matches("([a-zA-Z\\-\\s]+)$") || !(str_religion.length() >= 4))
                    DialogUtil.errorDialog(getContext(), "Error", "Please add a valid religion", "Okay", false);
                else if(str_email.isEmpty() || !str_email.matches("([a-zA-Z0-9\\-\\_\\.\\@]+)$"))
                    DialogUtil.errorDialog(getContext(), "Error", "Please add a valid email address", "Okay", false);
                else if(str_number.isEmpty() || !phoneNumberUtil.isValidNumber(phoneNumberUtil.parse(str_number, "PH")))
                    DialogUtil.errorDialog(getContext(), "Error", "Please enter a valid Philippine phone number", "Okay", false);
                else if(str_address.isEmpty() || !(str_address.length() >= 5))
                    DialogUtil.errorDialog(getContext(), "Error", "Please enter a valid address", "Okay", false);
                else if(str_rank.isEmpty() || !(str_rank.length() >= 4))
                    DialogUtil.errorDialog(getContext(), "Error", "Please enter a valid rank", "Okay", false);
                else
                {
                    TeacherAPI api = AppInstance.retrofit().create(TeacherAPI.class);
                    Call<ServerResponse<TeacherItem>> call = api.addNewTeacher(TeacherItem.newTeacher(
                            str_name,
                            str_rank,
                            str_gender,
                            str_email,
                            "+63" + phoneNumberUtil.parse(str_number, "PH").getNationalNumber(),
                            str_religion,
                            str_address,
                            str_bday,
                            base64Image
                    ));
                    call.enqueue(new Callback<ServerResponse<TeacherItem>>() {
                        @Override
                        public void onResponse(Call<ServerResponse<TeacherItem>> call, Response<ServerResponse<TeacherItem>> response)
                        {
                            DialogUtil.dismissDialog();
                            ServerResponse<TeacherItem> server = response.body();

                            if(server != null && !server.hasError) {
                                close.callOnClick();
                                onRefresh();
                                DialogUtil.successDialog(getContext(), "Success", server.message, "Okay", false);
                            }
                            else if(server != null && server.hasError)
                                DialogUtil.errorDialog(getContext(), "Error", server.message, "Okay", false);
                            else
                                DialogUtil.errorDialog(getContext(), "Error", "Server returned an unexpected result", "Okay", false);
                        }

                        @Override
                        public void onFailure(Call<ServerResponse<TeacherItem>> call, Throwable t)
                        {
                            DialogUtil.dismissDialog();
                            DialogUtil.errorDialog(getContext(), "Error", t.getMessage(), "Okay", false);
                        }
                    });
                }

            } catch(Exception e) {
                DialogUtil.errorDialog(getContext(), "Error", e.getMessage(), "Okay", false);
                e.printStackTrace();
            }
        });
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver()
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
                int type = intent.getExtras().getInt(Constants.KEY_TRIGGER_MODAL_TYPE);
                int id = intent.getExtras().getInt(Constants.KEY_TRIGGER_MODAL_VIEW);
                int action = intent.getExtras().getInt(Constants.KEY_TRIGGER_ACTION_TYPE);

                if(type != 1)
                    return;

                TeacherAPI api = AppInstance.retrofit().create(TeacherAPI.class);
                Call<ServerResponse<TeacherItem>> call = api.getTeacher(id);
                call.enqueue(new Callback<ServerResponse<TeacherItem>>() {
                    @Override
                    public void onResponse(Call<ServerResponse<TeacherItem>> call, Response<ServerResponse<TeacherItem>> response)
                    {
                        DialogUtil.dismissDialog();
                        ServerResponse<TeacherItem> server = response.body();
                        refreshTeacherListLayout.setRefreshing(false);

                        if(server != null && !server.hasError)
                        {
                            List<TeacherItem> studentItems = server.data;

                            if(studentItems != null && action == 0)
                                viewTeacher(studentItems.get(0));
                            else
                                DialogUtil.errorDialog(context, "Error", server.message, "Okay", false);
                        }
                    }

                    @Override
                    public void onFailure(Call<ServerResponse<TeacherItem>> call, Throwable t)
                    {
                        DialogUtil.errorDialog(context, "Server Error", t.getMessage(), false);
                        refreshTeacherListLayout.setRefreshing(false);
                    }
                });
            }
        }
    };
}
