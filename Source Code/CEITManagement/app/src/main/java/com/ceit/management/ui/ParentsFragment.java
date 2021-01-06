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
import android.widget.ListView;
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
import com.ceit.management.adapter.ChildListAdapter;
import com.ceit.management.adapter.ParentListAdapter;
import com.ceit.management.adapter.SelectedChildAdapter;
import com.ceit.management.api.ParentAPI;
import com.ceit.management.api.StudentAPI;
import com.ceit.management.model.ServerResponse;
import com.ceit.management.net.BlurImage;
import com.ceit.management.net.InternetReceiver;
import com.ceit.management.pojo.ParentItem;
import com.ceit.management.pojo.StudentItem;
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
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
public class ParentsFragment extends Fragment implements WaveSwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, InternetReceiver.OnInternetConnectionChangedListener
{
    private WaveSwipeRefreshLayout refreshParentListLayout;
    private CurvedBottomNavigationView curvedBottomNavigationView;
    private NestedScrollView scroller;
    private FloatingActionButton fabAdd;
    private LinearLayout overlayNoWifi;
    private LinearLayout overlayNodataFound;
    private LinearLayout overlayServerError;
    private RecyclerView listParents;
    private View root;
    private ParentListAdapter parentListAdapter;

    private SelectedChildAdapter selectedChildAdapter;
    private ChildListAdapter childListAdapter;
    private String base64Image;

    @SuppressLint("NonConstantResourceId")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        root = inflater.inflate(R.layout.fragment_parents, container, false);
        refreshParentListLayout = root.findViewById(R.id.refresh_parents_list);
        curvedBottomNavigationView = root.findViewById(R.id.bottom_nav_parents);
        scroller = root.findViewById(R.id.scroller);
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
                    Constants.DELETE_PARENT_TAB_ACTIVE = false;

                    parentListAdapter.clear();
                    refreshParentListLayout.setRefreshing(true);
                    fetchAllParents();
                    break;

                case R.id.bottom_nav_removed:
                    Constants.DELETE_PARENT_TAB_ACTIVE = true;

                    parentListAdapter.clear();
                    refreshParentListLayout.setRefreshing(true);
                    fetchAllRemovedParents();
                    break;
            }

            return false;
        });

        fabAdd.setOnClickListener((View view) -> {
            showAddParent();
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        listParents.setLayoutManager(layoutManager);

        parentListAdapter = new ParentListAdapter(new ArrayList<>());
        listParents.setAdapter(parentListAdapter);

        refreshParentListLayout.setRefreshing(true);
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if(requestCode == Constants.CODE_PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK)
        {
            ShapeableImageView profile = root.findViewById(R.id.e_profile);

            Bitmap photo = ImageUtil.imageUriToBitmap(getContext(), data.getData());
            base64Image = ImageUtil.imageToBase64(photo);

            //profile.setShapeAppearanceModel(ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, 160).build());

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

        if(Constants.DELETE_PARENT_TAB_ACTIVE)
            fetchAllRemovedParents();
        else
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
        overlayServerError.setVisibility(View.VISIBLE);
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
            public void onResponse(@NotNull Call<ServerResponse<ParentItem>> call, @NotNull Response<ServerResponse<ParentItem>> response)
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
                call.cancel();
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse<ParentItem>> call, @NotNull Throwable t)
            {
                refreshParentListLayout.setRefreshing(false);
                serverError();
                call.cancel();
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
            public void onResponse(@NotNull Call<ServerResponse<ParentItem>> call, @NotNull Response<ServerResponse<ParentItem>> response)
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
                call.cancel();
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse<ParentItem>> call, @NotNull Throwable t)
            {
                refreshParentListLayout.setRefreshing(false);
                serverError();
                call.cancel();
            }
        });
    }

    private void fetchAllUnselectedStudents()
    {
        DialogUtil.progressDialog(getContext(), "Preparing form...", getContext().getResources().getColor(R.color.themeColor), false);
        StudentAPI api = AppInstance.retrofit().create(StudentAPI.class);
        Call<ServerResponse<StudentItem>> call = api.getAllStudents();
        call.enqueue(new Callback<ServerResponse<StudentItem>>() {
            @Override
            public void onResponse(Call<ServerResponse<StudentItem>> call, Response<ServerResponse<StudentItem>> response)
            {
                ServerResponse<StudentItem> server = response.body();
                DialogUtil.dismissDialog();
                call.cancel();

                if(server != null && !server.hasError)
                {
                    MaterialAutoCompleteTextView child = root.findViewById(R.id.input_child);
                    List<StudentItem> selectedItems = (selectedChildAdapter != null) ? selectedChildAdapter.getStudents() : new ArrayList<>();
                    ArrayList<StudentItem> unselectedItems = (ArrayList<StudentItem>) server.data;

                    if(!selectedItems.isEmpty() && !unselectedItems.isEmpty())
                    {
                        for(StudentItem selected : selectedItems)
                            for(int i = 0; i < unselectedItems.size(); i++)
                            {
                                StudentItem unselected = unselectedItems.get(i);
                                if (selected.name.toLowerCase().equals(unselected.name.toLowerCase()))
                                    unselectedItems.remove(unselected);
                            }

                        if(childListAdapter != null)
                        {
                            childListAdapter.clear();

                            for(StudentItem item : unselectedItems)
                                childListAdapter.add(item);
                        }
                        else
                        {
                            childListAdapter = new ChildListAdapter(getContext(), unselectedItems);
                            child.setAdapter(childListAdapter);
                        }
                    }
                    else if(selectedItems.isEmpty() && !unselectedItems.isEmpty())
                    {
                        childListAdapter = new ChildListAdapter(getContext(), unselectedItems);
                        child.setAdapter(childListAdapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<ServerResponse<StudentItem>> call, Throwable t)
            {
                DialogUtil.errorDialog(getContext(), "Preparing Failed", t.getMessage(), "Okay", false);
                call.cancel();
            }
        });
    }

    private void showAddParent()
    {
        TextInputEditText name = root.findViewById(R.id.e_name);
        TextInputEditText religion = root.findViewById(R.id.e_religion);
        TextInputEditText email = root.findViewById(R.id.e_email);
        TextInputEditText number = root.findViewById(R.id.e_number);
        TextInputEditText address = root.findViewById(R.id.e_address);
        TextInputEditText occupation = root.findViewById(R.id.e_occupation);
        MaterialAutoCompleteTextView child = root.findViewById(R.id.input_child);
        ListView selectedChild = root.findViewById(R.id.selected_child);
        Button add = root.findViewById(R.id.btn_save_cancel_profile);
        Button close = root.findViewById(R.id.e_close);
        ShapeableImageView profile = root.findViewById(R.id.e_profile);
        ImageView cover = root.findViewById(R.id.e_background);
        ImageView change = root.findViewById(R.id.change_photo);
        Spinner gender = root.findViewById(R.id.e_gender);
        TextView birthday = root.findViewById(R.id.e_birthdate);
        TextView noChildSelected = root.findViewById(R.id.no_child);
        fetchAllUnselectedStudents();

        //profile.setShapeAppearanceModel(ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, 160).build());

        Glide.with(getContext())
                .load(R.drawable.parents)
                .circleCrop()
                .into(profile);

        cover.setBackgroundColor(getContext().getResources().getColor(R.color.themeColor));
        add.setText("Add Parent");

        String[] genders = {"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, genders);
        gender.setAdapter(genderAdapter);
        gender.setSelection(0);

        // Hide fab and bottom nav
        fabAdd.setVisibility(View.GONE);
        curvedBottomNavigationView.setVisibility(View.GONE);

        ConstraintLayout addLayout = root.findViewById(R.id.sheet_add_edit);
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

        selectedChildAdapter = new SelectedChildAdapter(getContext(), new ArrayList<StudentItem>());
        selectedChildAdapter.setOnListEmptyListener(() -> {
            noChildSelected.setVisibility(View.VISIBLE);
            selectedChild.setVisibility(View.GONE);
            fetchAllUnselectedStudents();
        });
        selectedChildAdapter.setOnItemsChangedListener((id, type) -> {
            fetchAllUnselectedStudents();
        });
        selectedChild.setAdapter(selectedChildAdapter);

        child.setOnItemClickListener((adapterView, view, position, id) -> {
            StudentItem student = (StudentItem) adapterView.getItemAtPosition(position);
            selectedChildAdapter.add(student);
            child.setText("");

            noChildSelected.setVisibility(View.GONE);
            selectedChild.setVisibility(View.VISIBLE);
            fetchAllUnselectedStudents();
        });

        close.setOnClickListener(v -> {
            selectedChildAdapter = null;
            name.clearFocus();
            religion.clearFocus();
            email.clearFocus();
            number.clearFocus();
            address.clearFocus();
            occupation.clearFocus();
            child.clearFocus();

            name.setText("");
            religion.setText("");
            email.setText("");
            number.setText("");
            address.setText("");
            occupation.setText("");
            child.setText("");

            gender.setSelection(0);
            profile.setImageResource(R.drawable.parents);
            birthday.setText(R.string.hint_birth_date);

            addSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

            noChildSelected.setVisibility(View.VISIBLE);
            selectedChild.setVisibility(View.GONE);

            fabAdd.setVisibility(View.VISIBLE);
            curvedBottomNavigationView.setVisibility(View.VISIBLE);
        });

        add.setOnClickListener(v -> {
            try {
                DialogUtil.progressDialog(getContext(), "Adding parent...", getContext().getResources().getColor(R.color.themeColor), false);
                PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.createInstance(getContext());

                String str_name = name.getText().toString();
                String str_religion = religion.getText().toString();
                String str_email = email.getText().toString();
                String str_number = number.getText().toString();
                String str_address = address.getText().toString();
                String str_occupation = occupation.getText().toString();

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
                else if(str_occupation.isEmpty() || !(str_occupation.length() >= 4))
                    DialogUtil.errorDialog(getContext(), "Error", "Please enter a valid occupation", "Okay", false);
                else
                {
                    ParentAPI api = AppInstance.retrofit().create(ParentAPI.class);
                    Call<ServerResponse<ParentItem>> call = api.addNewParent(ParentItem.newParent(
                            str_name,
                            str_gender,
                            selectedChildAdapter.getStudentIds(),
                            new int[0],
                            str_religion,
                            str_bday,
                            str_address,
                            "+63" + String.valueOf(phoneNumberUtil.parse(str_number, "PH").getNationalNumber()),
                            str_email,
                            str_occupation,
                            base64Image
                    ));
                    call.enqueue(new Callback<ServerResponse<ParentItem>>() {
                        @Override
                        public void onResponse(Call<ServerResponse<ParentItem>> call, Response<ServerResponse<ParentItem>> response)
                        {
                            DialogUtil.dismissDialog();
                            ServerResponse<ParentItem> server = response.body();

                            if(server != null && !server.hasError) {
                                close.callOnClick();
                                onRefresh();
                                DialogUtil.successDialog(getContext(), "Success", server.message, "Okay", false);
                            }
                            else if(server != null && server.hasError)
                                DialogUtil.errorDialog(getContext(), "Error", server.message, "Okay", false);
                            else
                                DialogUtil.errorDialog(getContext(), "Error", "Server returned an unexpected result", "Okay", false);

                            call.cancel();
                        }

                        @Override
                        public void onFailure(Call<ServerResponse<ParentItem>> call, Throwable t)
                        {
                            DialogUtil.dismissDialog();
                            DialogUtil.errorDialog(getContext(), "Error", t.getMessage(), "Okay", false);
                            call.cancel();
                        }
                    });
                }

            } catch(Exception e) {
                DialogUtil.errorDialog(getContext(), "Error", e.getMessage(), "Okay", false);
                e.printStackTrace();
            }
        });
    }

    private void viewParent(ParentItem item)
    {
        TextInputEditText name = root.findViewById(R.id.e_name);
        TextInputEditText religion = root.findViewById(R.id.e_religion);
        TextInputEditText email = root.findViewById(R.id.e_email);
        TextInputEditText number = root.findViewById(R.id.e_number);
        TextInputEditText address = root.findViewById(R.id.e_address);
        TextInputEditText occupation = root.findViewById(R.id.e_occupation);
        MaterialAutoCompleteTextView child = root.findViewById(R.id.input_child);
        ListView selectedChild = root.findViewById(R.id.selected_child);
        Button edit = root.findViewById(R.id.btn_save_cancel_profile);
        Button close = root.findViewById(R.id.e_close);
        ShapeableImageView profile = root.findViewById(R.id.e_profile);
        ImageView cover =  root.findViewById(R.id.e_background);
        ImageView change = root.findViewById(R.id.change_photo);
        ImageView iconChild = root.findViewById(R.id.icon_child);
        Spinner gender = root.findViewById(R.id.e_gender);
        TextView birthday = root.findViewById(R.id.e_birthdate);
        TextView noChildSelected = root.findViewById(R.id.no_child);

        //profile.setShapeAppearanceModel(ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, 160).build());
        profile.setImageResource(R.drawable.parents);

        BlurImage.with(getContext())
                .setBlurRadius(25f)
                .setBitmapScale(0.6f)
                .blurFromUri(item.photo)
                .into(cover);

        Glide.with(getContext())
                .load(item.photo)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(item.gender.equals("Male") ? R.drawable.male : R.drawable.female)
                .error(item.gender.equals("Male") ? R.drawable.male : R.drawable.female)
                .into(profile);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Male", "Female"});
        gender.setAdapter(genderAdapter);
        gender.setSelection(item.gender.equals("Male") ? 0 : 1);

        edit.setText("Edit Parent");

        ArrayList<StudentItem> students = new ArrayList<>();
        ArrayList<StudentItem> selected = (item.selectedChild == null) ? new ArrayList<>() : item.selectedChild;
        for(int i = 0; i < selected.size(); i++)
            students.add(selected.get(i));

        if(selectedChildAdapter == null)
            selectedChildAdapter = new SelectedChildAdapter(getContext(), students);
        selectedChildAdapter.setEditMode(false);

        if(selected.size() > 0)
        {
            noChildSelected.setVisibility(View.GONE);
            selectedChild.setVisibility(View.VISIBLE);
            selectedChild.setAdapter(selectedChildAdapter);
        }
        else
        {
            noChildSelected.setVisibility(View.VISIBLE);
            selectedChild.setVisibility(View.GONE);
        }

        name.setText(item.name);
        religion.setText(item.religion);
        email.setText(item.email);
        number.setText(item.contactNumber);
        address.setText(item.address);
        occupation.setText(item.occupation);
        birthday.setText(item.birthday);

        change.setVisibility(View.GONE);
        name.setEnabled(false);
        religion.setEnabled(false);
        email.setEnabled(false);
        number.setEnabled(false);
        address.setEnabled(false);
        occupation.setEnabled(false);
        birthday.setEnabled(false);
        gender.setEnabled(false);

        // Hide fab and bottom nav
        fabAdd.setVisibility(View.GONE);
        curvedBottomNavigationView.setVisibility(View.GONE);

        // Show child input and icon
        ((ViewGroup) child.getParent()).setVisibility(View.GONE);

        ConstraintLayout sheet = root.findViewById(R.id.sheet_add_edit);
        BottomSheetBehavior<ConstraintLayout> editSheet = BottomSheetBehavior.from(sheet);
        editSheet.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO, true);
        editSheet.setState(BottomSheetBehavior.STATE_EXPANDED);

        edit.setOnClickListener(v -> {
            close.callOnClick();
            if(selectedChildAdapter != null)
                selectedChildAdapter.clear();
            selectedChildAdapter = null;
            editProfile(item);
        });

        close.setOnClickListener(v -> {
            editSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
            name.setText("");
            religion.setText("");
            email.setText("");
            number.setText("");
            address.setText("");
            occupation.setText("");
            birthday.setText("Birth Date");

            change.setVisibility(View.VISIBLE);
            selectedChildAdapter.setEditMode(false);
            selectedChildAdapter = null;
            name.setEnabled(true);
            religion.setEnabled(true);
            email.setEnabled(true);
            number.setEnabled(true);
            address.setEnabled(true);
            occupation.setEnabled(true);
            birthday.setEnabled(true);
            gender.setEnabled(true);

            // Hide fab and bottom nav
            fabAdd.setVisibility(View.VISIBLE);
            curvedBottomNavigationView.setVisibility(View.VISIBLE);

            // Hide child input and icon
            child.setVisibility(View.VISIBLE);
            iconChild.setVisibility(View.VISIBLE);
        });
    }

    private void editProfile(ParentItem item)
    {
        TextInputEditText name = root.findViewById(R.id.e_name);
        TextInputEditText religion = root.findViewById(R.id.e_religion);
        TextInputEditText email = root.findViewById(R.id.e_email);
        TextInputEditText number = root.findViewById(R.id.e_number);
        TextInputEditText address = root.findViewById(R.id.e_address);
        TextInputEditText occupation = root.findViewById(R.id.e_occupation);
        MaterialAutoCompleteTextView child = root.findViewById(R.id.input_child);
        ListView selectedChild = root.findViewById(R.id.selected_child);
        Button save = root.findViewById(R.id.btn_save_cancel_profile);
        Button close = root.findViewById(R.id.e_close);
        ShapeableImageView profile = root.findViewById(R.id.e_profile);
        ImageView cover =  root.findViewById(R.id.e_background);
        ImageView change = root.findViewById(R.id.change_photo);
        ImageView iconChild = root.findViewById(R.id.icon_child);
        Spinner gender = root.findViewById(R.id.e_gender);
        TextView birthday = root.findViewById(R.id.e_birthdate);
        TextView noChildSelected = root.findViewById(R.id.no_child);
        List<Integer> removedChild = new ArrayList<>();
        fetchAllUnselectedStudents();

        //profile.setShapeAppearanceModel(ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, 160).build());
        profile.setImageResource(R.drawable.parents);

        BlurImage.with(getContext())
                .setBlurRadius(25f)
                .setBitmapScale(0.6f)
                .blurFromUri(item.photo)
                .into(cover);

        Glide.with(getContext())
                .load(item.photo)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(item.gender.equals("Male") ? R.drawable.male : R.drawable.female)
                .error(item.gender.equals("Male") ? R.drawable.male : R.drawable.female)
                .into(profile);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Male", "Female"});
        gender.setAdapter(genderAdapter);
        gender.setSelection(item.gender.equals("Male") ? 0 : 1);

        save.setText("Update Parent");

        name.setText(item.name);
        religion.setText(item.religion);
        email.setText(item.email);
        number.setText(item.contactNumber);
        address.setText(item.address);
        occupation.setText(item.occupation);
        birthday.setText(item.birthday);

        change.setVisibility(View.VISIBLE);
        name.setEnabled(true);
        religion.setEnabled(true);
        email.setEnabled(true);
        number.setEnabled(true);
        address.setEnabled(true);
        occupation.setEnabled(true);
        birthday.setEnabled(true);
        gender.setEnabled(true);

        // Hide fab and bottom nav
        fabAdd.setVisibility(View.GONE);
        curvedBottomNavigationView.setVisibility(View.GONE);

        // Show child input and icon
        ((ViewGroup) child.getParent()).setVisibility(View.VISIBLE);

        ConstraintLayout sheet = root.findViewById(R.id.sheet_add_edit);
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
            picker.show();
        });

        change.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(pickIntent, "Choose image..."), Constants.CODE_PICK_IMAGE);
        });


        ArrayList<StudentItem> students = new ArrayList<>();
        ArrayList<StudentItem> selected = (item.selectedChild == null) ? new ArrayList<>() : item.selectedChild;
        for(int i = 0; i < selected.size(); i++)
            students.add(selected.get(i));

        if(selectedChildAdapter == null)
            selectedChildAdapter = new SelectedChildAdapter(getContext(), students);
        selectedChildAdapter.setEditMode(true);

        if(selected.size() > 0)
        {
            noChildSelected.setVisibility(View.GONE);
            selectedChild.setVisibility(View.VISIBLE);
        }
        else
        {
            noChildSelected.setVisibility(View.VISIBLE);
            selectedChild.setVisibility(View.GONE);
        }

        selectedChildAdapter.setOnListEmptyListener(() -> {
            noChildSelected.setVisibility(View.VISIBLE);
            selectedChild.setVisibility(View.GONE);
            fetchAllUnselectedStudents();
        });
        selectedChildAdapter.setOnItemsChangedListener((id, type) -> {
            fetchAllUnselectedStudents();

            if(type == -1)
                removedChild.add(Integer.valueOf(id));
            else
                removedChild.remove(Integer.valueOf(id));
        });
        selectedChild.setAdapter(selectedChildAdapter);

        child.setOnItemClickListener((adapterView, view, position, id) -> {
            StudentItem student = (StudentItem) adapterView.getItemAtPosition(position);
            selectedChildAdapter.add(student);
            child.setText("");

            noChildSelected.setVisibility(View.GONE);
            selectedChild.setVisibility(View.VISIBLE);
            fetchAllUnselectedStudents();
        });

        save.setOnClickListener(v -> {
            try {
                DialogUtil.progressDialog(getContext(), "Updating parent...", getContext().getResources().getColor(R.color.themeColor), false);
                PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.createInstance(getContext());

                String str_name = name.getText().toString();
                String str_religion = religion.getText().toString();
                String str_email = email.getText().toString();
                String str_number = number.getText().toString();
                String str_address = address.getText().toString();
                String str_occupation = occupation.getText().toString();

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
                else if(str_occupation.isEmpty() || !(str_occupation.length() >= 4))
                    DialogUtil.errorDialog(getContext(), "Error", "Please enter a valid occupation", "Okay", false);
                else
                {
                    int[] removed = new int[removedChild.size()];

                    for(int i = 0; i < removedChild.size(); i++)
                        removed[i] = removedChild.get(i).intValue();

                    ParentAPI api = AppInstance.retrofit().create(ParentAPI.class);
                    Call<ServerResponse<ParentItem>> call = api.updateParent(item.id, ParentItem.newParent(
                            str_name,
                            str_gender,
                            selectedChildAdapter.getStudentIds(),
                            removed,
                            str_religion,
                            str_bday,
                            str_address,
                            "+63" + String.valueOf(phoneNumberUtil.parse(str_number, "PH").getNationalNumber()),
                            str_email,
                            str_occupation,
                            base64Image
                    ));
                    call.enqueue(new Callback<ServerResponse<ParentItem>>() {
                        @Override
                        public void onResponse(Call<ServerResponse<ParentItem>> call, Response<ServerResponse<ParentItem>> response)
                        {
                            DialogUtil.dismissDialog();
                            ServerResponse<ParentItem> server = response.body();

                            if(server != null && !server.hasError) {
                                close.callOnClick();
                                onRefresh();
                                DialogUtil.successDialog(getContext(), "Success", server.message, "Okay", false);
                            }
                            else if(server != null && server.hasError)
                                DialogUtil.errorDialog(getContext(), "Error", server.message, "Okay", false);
                            else
                                DialogUtil.errorDialog(getContext(), "Error", "Server returned an unexpected result", "Okay", false);

                            call.cancel();
                        }

                        @Override
                        public void onFailure(Call<ServerResponse<ParentItem>> call, Throwable t)
                        {
                            DialogUtil.dismissDialog();
                            DialogUtil.errorDialog(getContext(), "Error", t.getLocalizedMessage(), "Okay", false);
                            call.cancel();
                        }
                    });
                }

            } catch(Exception e) {
                DialogUtil.errorDialog(getContext(), "Error", e.getMessage(), "Okay", false);
                e.printStackTrace();
            }
        });

        close.setOnClickListener(v -> {
            name.setText("");
            religion.setText("");
            email.setText("");
            number.setText("");
            address.setText("");
            occupation.setText("");
            birthday.setText("Birth Date");

            change.setVisibility(View.GONE);
            selectedChildAdapter.setEditMode(false);
            selectedChildAdapter = null;
            name.setEnabled(true);
            religion.setEnabled(true);
            email.setEnabled(true);
            number.setEnabled(true);
            address.setEnabled(true);
            occupation.setEnabled(true);
            birthday.setEnabled(true);
            gender.setEnabled(true);

            // Hide fab and bottom nav
            fabAdd.setVisibility(View.VISIBLE);
            curvedBottomNavigationView.setVisibility(View.VISIBLE);

            // Hide child input and icon
            child.setVisibility(View.VISIBLE);
            iconChild.setVisibility(View.VISIBLE);

            editSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
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

                if(type != 3)
                    return;

                DialogUtil.progressDialog(getContext(), "Viewing parent...", getContext().getResources().getColor(R.color.themeColor), false);
                ParentAPI api = AppInstance.retrofit().create(ParentAPI.class);
                Call<ServerResponse<ParentItem>> call = api.getParent(id);
                call.enqueue(new Callback<ServerResponse<ParentItem>>() {
                    @Override
                    public void onResponse(Call<ServerResponse<ParentItem>> call, Response<ServerResponse<ParentItem>> response)
                    {
                        DialogUtil.dismissDialog();
                        ServerResponse<ParentItem> server = response.body();

                        if(server != null && !server.hasError)
                        {
                            List<ParentItem> items = server.data;

                            if(action == 0)
                                viewParent(items.get(0));
                        }
                        else if(server != null && server.hasError)
                            DialogUtil.errorDialog(getContext(), "Error", server.message, "Okay", false);
                        else
                            DialogUtil.errorDialog(getContext(), "Error", "Server returned an unexpected result", "Okay", false);

                        call.cancel();
                    }

                    @Override
                    public void onFailure(Call<ServerResponse<ParentItem>> call, Throwable t)
                    {
                        DialogUtil.dismissDialog();
                        DialogUtil.errorDialog(getContext(), "Error", t.getMessage(), "Okay", false);
                        call.cancel();
                    }
                });
            }
        }
    };
}
