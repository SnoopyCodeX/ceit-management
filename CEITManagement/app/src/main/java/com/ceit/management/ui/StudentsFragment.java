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
import com.ceit.management.adapter.StudentListAdapter;
import com.ceit.management.api.ClassAPI;
import com.ceit.management.api.StudentAPI;
import com.ceit.management.model.ServerResponse;
import com.ceit.management.net.BlurImage;
import com.ceit.management.net.InternetReceiver;
import com.ceit.management.pojo.ClassItem;
import com.ceit.management.pojo.StudentItem;
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

@SuppressWarnings("ALL")
public class StudentsFragment extends Fragment implements WaveSwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, InternetReceiver.OnInternetConnectionChangedListener {
    private WaveSwipeRefreshLayout refreshStudentListLayout;
    private CurvedBottomNavigationView curvedBottomNavigationView;
    private NestedScrollView scroller;
    private FloatingActionButton fabAdd;
    private LinearLayout overlayNoWifi;
    private LinearLayout overlayNodataFound;
    private LinearLayout overlayServerError;
    private RecyclerView listStudents;
    private StudentListAdapter studentListAdapter;

    private String base64Image;
    private View root;

    @SuppressLint("NonConstantResourceId")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_students, container, false);
        refreshStudentListLayout = root.findViewById(R.id.refresh_students_list);
        curvedBottomNavigationView = root.findViewById(R.id.bottom_nav_students);
        scroller = root.findViewById(R.id.scroller);
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

        fabAdd.setOnClickListener(v -> {
            showAddForm();
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if(requestCode == Constants.CODE_PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK)
        {
            ShapeableImageView profile = root.findViewById(R.id.e_profile);
            ImageView cover = root.findViewById(R.id.e_background);

            Bitmap photo = ImageUtil.imageUriToBitmap(getContext(), data.getData());
            base64Image = ImageUtil.imageToBase64(photo);

            profile.setShapeAppearanceModel(ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, 160).build());
            profile.setImageBitmap(photo);
        }

        super.onActivityResult(requestCode, resultCode, data);
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
        scroller.setVisibility(View.GONE);
    }

    private void noDataFound() {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.VISIBLE);
        overlayNoWifi.setVisibility(View.GONE);
        scroller.setVisibility(View.GONE);
    }

    private void serverError() {
        overlayServerError.setVisibility(View.VISIBLE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        scroller.setVisibility(View.GONE);
    }

    private void connectedToInternet() {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        scroller.setVisibility(View.VISIBLE);
    }

    private void dataFound() {
        overlayServerError.setVisibility(View.GONE);
        overlayNodataFound.setVisibility(View.GONE);
        overlayNoWifi.setVisibility(View.GONE);
        scroller.setVisibility(View.VISIBLE);
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

    private void fetchAllClasses(String selectedClass)
    {
        DialogUtil.progressDialog(getContext(), "Fetching registered classes..", getContext().getResources().getColor(R.color.themeColor), false);
        final Spinner section = root.findViewById(R.id.e_section);

        ClassAPI api = AppInstance.retrofit().create(ClassAPI.class);
        Call<ServerResponse<ClassItem>> call = api.getAllClasses();
        call.enqueue(new Callback<ServerResponse<ClassItem>>() {
            @Override
            public void onResponse(Call<ServerResponse<ClassItem>> call, Response<ServerResponse<ClassItem>> response)
            {
                ServerResponse<ClassItem> server = response.body();
                DialogUtil.dismissDialog();

                if(server != null && !server.hasError && server.data.size() > 0)
                {
                    List<ClassItem> items = server.data;
                    String[] names = new String[items.size()];
                    int defIndex = 0;

                    for(int i = 0; i < items.size(); i++)
                    {
                        if(items.get(i).name.toLowerCase().equals(selectedClass.toLowerCase()))
                            defIndex = i;
                        names[i] = items.get(i).name;
                    }

                    ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names);
                    section.setAdapter(classAdapter);
                    section.setSelection(defIndex);

                }
                else
                {
                    List<ClassItem> fallbackItem = new ArrayList<>();
                    ClassItem classItem = ClassItem.newClass();
                    classItem.name = "No classes available";
                    classItem.id = -1;
                    fallbackItem.add(classItem);

                    String[] names = new String[fallbackItem.size()];
                    int defIndex = 0;

                    for(int i = 0; i < fallbackItem.size(); i++)
                    {
                        if(fallbackItem.get(i).name.toLowerCase().equals(selectedClass.toLowerCase()))
                            defIndex = i;
                        names[i] = fallbackItem.get(i).name;
                    }

                    ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names);
                    section.setAdapter(classAdapter);
                    section.setSelection(defIndex);
                }
            }

            @Override
            public void onFailure(Call<ServerResponse<ClassItem>> call, Throwable t)
            {
                List<ClassItem> fallbackItem = new ArrayList<>();
                ClassItem classItem = ClassItem.newClass();
                classItem.name = "No classes available";
                classItem.id = -1;
                fallbackItem.add(classItem);

                String[] names = new String[fallbackItem.size()];
                int defIndex = 0;

                for(int i = 0; i < fallbackItem.size(); i++)
                {
                    if(fallbackItem.get(i).name.toLowerCase().equals(selectedClass.toLowerCase()))
                        defIndex = i;
                    names[i] = fallbackItem.get(i).name;
                }

                ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names);
                section.setAdapter(classAdapter);
                section.setSelection(defIndex);
            }
        });
    }

    private void editStudent(StudentItem student)
    {
        ShapeableImageView profile = root.findViewById(R.id.e_profile);
        ImageView cover = root.findViewById(R.id.e_background);

        ImageView changePhoto = root.findViewById(R.id.change_photo);
        Button close = root.findViewById(R.id.e_close);
        Button save = root.findViewById(R.id.btn_save_cancel_profile);

        TextInputEditText name = root.findViewById(R.id.e_name);
        TextView birthday = root.findViewById(R.id.e_birthdate);
        Spinner gender = root.findViewById(R.id.e_gender);
        TextInputEditText email = root.findViewById(R.id.e_email);
        TextInputEditText number = root.findViewById(R.id.e_number);
        TextInputEditText address = root.findViewById(R.id.e_address);
        Spinner section = root.findViewById(R.id.e_section);
        TextInputEditText religion = root.findViewById(R.id.e_religion);

        profile.setShapeAppearanceModel(ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, 160).build());

        //hide fab and bottom nav
        curvedBottomNavigationView.setVisibility(View.GONE);
        fabAdd.setVisibility(View.GONE);

        Glide.with(getContext())
                .load(student.photo)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.student)
                .error(R.drawable.student)
                .into(profile);

        name.setText(student.name);
        birthday.setText(student.birthday);
        email.setText(student.email);
        number.setText(student.contactNumber);
        address.setText(student.address);
        religion.setText(student.religion);

        ConstraintLayout bottomEditAddSheet = root.findViewById(R.id.sheet_edit);
        BottomSheetBehavior<ConstraintLayout> editAddSheet = BottomSheetBehavior.from(bottomEditAddSheet);
        editAddSheet.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO, true);
        editAddSheet.setState(BottomSheetBehavior.STATE_EXPANDED);

        birthday.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date("Jan 1, 1990"));
            DatePickerDialog picker = new DatePickerDialog(getContext(), (view, year, month, date) -> {
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(year, month, date);

                String strBday = DateFormat.getDateInstance().format(newCalendar.getTime());
                birthday.setText(strBday);
                student.birthday = strBday;
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH));

            Calendar bday = Calendar.getInstance();
            bday.setTime(new Date(birthday.getText().toString()));
            picker.getDatePicker().updateDate(bday.get(Calendar.YEAR), bday.get(Calendar.MONTH), bday.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });

        String[] genders = {"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, genders);
        gender.setAdapter(genderAdapter);
        gender.setSelection(student.gender.toLowerCase().equals("male") ? 0 : 1);

        changePhoto.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(pickIntent, "Choose image..."), Constants.CODE_PICK_IMAGE);
        });

        close.setOnClickListener(v -> {
            name.clearFocus();
            email.clearFocus();
            address.clearFocus();
            religion.clearFocus();
            number.clearFocus();

            name.setText("");
            email.setText("");
            address.setText("");
            religion.setText("");
            number.setText("");

            editAddSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

            curvedBottomNavigationView.setVisibility(View.VISIBLE);
            fabAdd.setVisibility(View.VISIBLE);
        });

        save.setOnClickListener(v -> {
            try {
                DialogUtil.progressDialog(getContext(), "Updating student...", getContext().getResources().getColor(R.color.themeColor), false);
                PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.createInstance(getContext());

                String str_name = name.getText().toString();
                String str_email = email.getText().toString();
                String str_address = address.getText().toString();
                String str_religion = religion.getText().toString();
                String str_number = number.getText().toString();
                String str_bday = birthday.getText().toString();
                String str_gender = gender.getSelectedItem().toString();
                String str_section = section.getSelectedItem().toString();

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
                else
                {
                    StudentAPI api = AppInstance.retrofit().create(StudentAPI.class);
                    Call<ServerResponse<StudentItem>> call = api.updateStudent(student.id, StudentItem.newStudent(
                            str_name,
                            str_gender,
                            str_email,
                            "+63" + phoneNumberUtil.parse(str_number, "PH").getNationalNumber(),
                            str_address,
                            str_religion,
                            str_bday,
                            str_section,
                            base64Image
                    ));
                    call.enqueue(new Callback<ServerResponse<StudentItem>>() {
                        @Override
                        public void onResponse(Call<ServerResponse<StudentItem>> call, Response<ServerResponse<StudentItem>> response)
                        {
                            DialogUtil.dismissDialog();
                            ServerResponse<StudentItem> server = response.body();

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
                        public void onFailure(Call<ServerResponse<StudentItem>> call, Throwable t)
                        {
                            DialogUtil.dismissDialog();
                            DialogUtil.errorDialog(getContext(), "Error", t.getMessage(), "Okay", false);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        fetchAllClasses(student.section);
    }

    private void viewStudent(StudentItem student)
    {
        ShapeableImageView profile = root.findViewById(R.id.v_profile);
        ImageView cover = root.findViewById(R.id.v_background);
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
        email.setText(student.email);
        number.setText(student.contactNumber);
        address.setText(student.address);
        religion.setText(student.religion);
        section.setText(student.section);

        ConstraintLayout bottomViewSheet = root.findViewById(R.id.sheet_info);
        BottomSheetBehavior<ConstraintLayout> viewSheet = BottomSheetBehavior.from(bottomViewSheet);
        viewSheet.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO, true);
        viewSheet.setState(BottomSheetBehavior.STATE_EXPANDED);

        close.setOnClickListener(v -> {
            cover.setBackgroundColor(getContext().getResources().getColor(R.color.themeColor));
            viewSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

            name.clearFocus();
            email.clearFocus();
            address.clearFocus();
            religion.clearFocus();
            number.clearFocus();

            name.setText("");
            email.setText("");
            address.setText("");
            religion.setText("");
            number.setText("");

            curvedBottomNavigationView.setVisibility(View.VISIBLE);
            fabAdd.setVisibility(View.VISIBLE);
        });

        edit.setOnClickListener(v -> {
            viewSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

            editStudent(student);
        });
    }

    private void showAddForm()
    {
        ShapeableImageView profile = root.findViewById(R.id.e_profile);
        ImageView cover = root.findViewById(R.id.e_background);

        ImageView changePhoto = root.findViewById(R.id.change_photo);
        Button close = root.findViewById(R.id.e_close);
        Button add = root.findViewById(R.id.btn_save_cancel_profile);

        TextInputEditText name = root.findViewById(R.id.e_name);
        TextView birthday = root.findViewById(R.id.e_birthdate);
        Spinner gender = root.findViewById(R.id.e_gender);
        TextInputEditText email = root.findViewById(R.id.e_email);
        TextInputEditText number = root.findViewById(R.id.e_number);
        TextInputEditText address = root.findViewById(R.id.e_address);
        Spinner section = root.findViewById(R.id.e_section);
        TextInputEditText religion = root.findViewById(R.id.e_religion);

        profile.setShapeAppearanceModel(ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, 160).build());

        //hide fab and bottom nav
        curvedBottomNavigationView.setVisibility(View.GONE);
        fabAdd.setVisibility(View.GONE);

        ConstraintLayout bottomEditAddSheet = root.findViewById(R.id.sheet_edit);
        BottomSheetBehavior<ConstraintLayout> addSheet = BottomSheetBehavior.from(bottomEditAddSheet);
        addSheet.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO, true);
        addSheet.setState(BottomSheetBehavior.STATE_EXPANDED);

        birthday.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date("Jan 1, 1990"));
            DatePickerDialog picker = new DatePickerDialog(getContext(), (view, year, month, date) -> {
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(year, month, date);

                String strBday = DateFormat.getDateInstance().format(newCalendar.getTime());
                birthday.setText(strBday);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            picker.show();
        });

        String[] genders = {"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, genders);
        gender.setAdapter(genderAdapter);
        gender.setSelection(0);

        changePhoto.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(pickIntent, "Choose image..."), Constants.CODE_PICK_IMAGE);
        });

        close.setOnClickListener(v -> {
            name.clearFocus();
            email.clearFocus();
            address.clearFocus();
            religion.clearFocus();
            number.clearFocus();

            name.setText("");
            email.setText("");
            address.setText("");
            religion.setText("");
            number.setText("");

            addSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

            curvedBottomNavigationView.setVisibility(View.VISIBLE);
            fabAdd.setVisibility(View.VISIBLE);
        });

        add.setOnClickListener(v -> {
            try {
                DialogUtil.progressDialog(getContext(), "Adding student...", getContext().getResources().getColor(R.color.themeColor), false);
                PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.createInstance(getContext());

                String str_name = name.getText().toString();
                String str_email = email.getText().toString();
                String str_address = address.getText().toString();
                String str_religion = religion.getText().toString();
                String str_number = number.getText().toString();
                String str_bday = birthday.getText().toString();
                String str_gender = gender.getSelectedItem().toString();
                String str_section = section.getSelectedItem().toString();

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
                else
                {
                    StudentAPI api = AppInstance.retrofit().create(StudentAPI.class);
                    Call<ServerResponse<StudentItem>> call = api.addNewStudent(StudentItem.newStudent(
                            str_name,
                            str_gender,
                            str_email,
                            "+63" + phoneNumberUtil.parse(str_number, "PH").getNationalNumber(),
                            str_address,
                            str_religion,
                            str_bday,
                            str_section,
                            base64Image
                    ));
                    call.enqueue(new Callback<ServerResponse<StudentItem>>() {
                        @Override
                        public void onResponse(Call<ServerResponse<StudentItem>> call, Response<ServerResponse<StudentItem>> response)
                        {
                            DialogUtil.dismissDialog();
                            ServerResponse<StudentItem> server = response.body();

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
                        public void onFailure(Call<ServerResponse<StudentItem>> call, Throwable t)
                        {
                            DialogUtil.dismissDialog();
                            DialogUtil.errorDialog(getContext(), "Error", t.getMessage(), "Okay", false);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        fetchAllClasses("");
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
            int modalAction = data.getExtras().getInt(Constants.KEY_TRIGGER_ACTION_TYPE);


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

                        if(studentItems != null && modalAction == 0)
                            viewStudent(studentItems.get(0));
                        else
                            DialogUtil.errorDialog(context, "Error", server.message, "Okay", false);
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
