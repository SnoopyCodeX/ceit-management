package com.ceit.management.adapter.holder;

import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ceit.management.AppInstance;
import com.ceit.management.R;
import com.ceit.management.api.StudentAPI;
import com.ceit.management.model.ServerResponse;
import com.ceit.management.pojo.StudentItem;
import com.ceit.management.util.Constants;
import com.ceit.management.util.DialogUtil;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentListHolder extends BaseViewHolder
{
    @BindView(R.id.student_photo)
    public ShapeableImageView photo;

    @BindView(R.id.student_name)
    public TextView name;

    @BindView(R.id.student_class)
    public TextView section;

    @BindView(R.id.student_gender)
    public TextView gender;

    @BindView(R.id.swipe)
    public SwipeRevealLayout swipeRevealLayout;

    @BindView(R.id.delete_layout)
    FrameLayout deleteLayout;

    @BindView(R.id.front_layout)
    FrameLayout frontLayout;

    private List<StudentItem> studentItemList;
    private View itemView;

    public StudentListHolder(View itemView, List<StudentItem> studentItemList)
    {
        super(itemView);
        this.studentItemList = studentItemList;
        this.itemView = itemView;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(int position)
    {
        super.onBind(position);

        StudentItem student = studentItemList.get(position);
        photo.setShapeAppearanceModel(ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, 80).build());
        name.setText(student.name);
        section.setText(student.section);
        gender.setText(student.gender);

        Glide.with(itemView.getContext())
                .load(student.photo)
                .placeholder(R.drawable.student)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(photo);

        frontLayout.setOnClickListener(v -> {
            Intent trigger = new Intent(Constants.TRIGGER_MODAL_OPEN);
            trigger.putExtra(Constants.KEY_TRIGGER_MODAL_VIEW, student.id);
            trigger.putExtra(Constants.KEY_TRIGGER_MODAL_TYPE, 2);
            itemView.getContext().sendBroadcast(trigger);
        });

        deleteLayout.setOnClickListener(v -> {
            DialogUtil.warningDialog(itemView.getContext(), "Confirm Delete", "Are you sure you want to delete this?", "Yes", "No",
                    (dlg) -> {
                        dlg.dismissWithAnimation();
                        DialogUtil.progressDialog(itemView.getContext(), "Deleting student", itemView.getContext().getResources().getColor(R.color.themeColor), false);
                        StudentAPI api = AppInstance.retrofit().create(StudentAPI.class);
                        Call<ServerResponse<StudentItem>> call = api.deleteStudent(student.id);
                        call.enqueue(new Callback<ServerResponse<StudentItem>>() {
                            @Override
                            public void onResponse(@NotNull Call<ServerResponse<StudentItem>> call, @NotNull Response<ServerResponse<StudentItem>> response)
                            {
                                ServerResponse<StudentItem> server = response.body();
                                DialogUtil.dismissDialog();

                                if(server != null && !server.hasError)
                                    DialogUtil.successDialog(itemView.getContext(), "Delete Success", "Student has been deleted successfully!");
                                else if(server != null && server.hasError)
                                    DialogUtil.errorDialog(itemView.getContext(), "Delete Failed", server.message);
                                else
                                    DialogUtil.errorDialog(itemView.getContext(), "Delete Failed", "Server returned an unexpected result");
                            }

                            @Override
                            public void onFailure(@NotNull Call<ServerResponse<StudentItem>> call, @NotNull Throwable t) {
                                DialogUtil.errorDialog(itemView.getContext(), "Delete Failed", t.getMessage());
                            }
                        });
                    },
                    SweetAlertDialog::dismissWithAnimation, false);
        });
    }

    @Override
    protected void clear()
    {}
}
