package com.ceit.management.adapter.holder;

import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ceit.management.AppInstance;
import com.ceit.management.R;
import com.ceit.management.api.TeacherAPI;
import com.ceit.management.model.ServerResponse;
import com.ceit.management.pojo.TeacherItem;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherListHolder extends BaseViewHolder
{
    @BindView(R.id.teacher_photo)
    public ShapeableImageView photo;

    @BindView(R.id.teacher_name)
    public TextView name;

    @BindView(R.id.teacher_rank)
    public TextView rank;

    @BindView(R.id.teacher_gender)
    public TextView gender;

    @BindView(R.id.swipe)
    public SwipeRevealLayout swipeRevealLayout;

    @BindView(R.id.delete_layout)
    ImageView deleteLayout;

    @BindView(R.id.restore_layout)
    ImageView restoreLayout;

    @BindView(R.id.front_layout)
    FrameLayout frontLayout;

    private List<TeacherItem> teacherItemList;
    private View itemView;

    public TeacherListHolder(View itemView, List<TeacherItem> teacherItemList)
    {
        super(itemView);
        this.teacherItemList = teacherItemList;
        this.itemView = itemView;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(int position)
    {
        super.onBind(position);

        TeacherItem teacher = teacherItemList.get(position);
        photo.setShapeAppearanceModel(ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, 80).build());
        name.setText(teacher.name);
        rank.setText(teacher.rank);
        gender.setText(teacher.gender);

        Glide.with(itemView.getContext())
                .load(teacher.photo)
                .placeholder(R.drawable.teacher)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(photo);

        frontLayout.setOnClickListener(v -> {
            if(swipeRevealLayout.isOpened())
            {
                swipeRevealLayout.close(true);
                return;
            }

            Intent trigger = new Intent(Constants.TRIGGER_MODAL_OPEN);
            trigger.putExtra(Constants.KEY_TRIGGER_MODAL_VIEW, teacher.id);
            trigger.putExtra(Constants.KEY_TRIGGER_MODAL_TYPE, 1);
            trigger.putExtra(Constants.KEY_TRIGGER_ACTION_TYPE, 0);
            itemView.getContext().sendBroadcast(trigger);
        });

        restoreLayout.setVisibility(Constants.DELETE_TEACHER_TAB_ACTIVE ? View.VISIBLE : View.GONE);

        restoreLayout.setOnClickListener(v -> {
            DialogUtil.warningDialog(itemView.getContext(), "Confirm Restore", "Are you sure you want to restore this?", "Yes", "No",
                    (dlg) -> {
                        dlg.dismissWithAnimation();

                        DialogUtil.progressDialog(itemView.getContext(), "Restoring data...", itemView.getContext().getResources().getColor(R.color.themeColor), false);
                        TeacherAPI api = AppInstance.retrofit().create(TeacherAPI.class);
                        Call<ServerResponse<TeacherItem>> call = api.restoreTeacher(teacher.id);
                        call.enqueue(new Callback<ServerResponse<TeacherItem>>() {
                            @Override
                            public void onResponse(@NotNull Call<ServerResponse<TeacherItem>> call, @NotNull Response<ServerResponse<TeacherItem>> response)
                            {
                                ServerResponse<TeacherItem> server = response.body();
                                DialogUtil.dismissDialog();

                                if(server != null && !server.hasError)
                                {
                                    itemView.getContext().sendBroadcast(new Intent(Constants.TRIGGER_REFRESH_LIST));
                                    DialogUtil.successDialog(itemView.getContext(), "Restore Success", "Teacher has been restored successfully!");
                                }
                                else if(server != null && server.hasError)
                                    DialogUtil.errorDialog(itemView.getContext(), "Restore Failed", server.message);
                                else
                                    DialogUtil.errorDialog(itemView.getContext(), "Restore Failed", "Server returned an unexpected result");

                                swipeRevealLayout.close(true);
                            }

                            @Override
                            public void onFailure(@NotNull Call<ServerResponse<TeacherItem>> call, @NotNull Throwable t) {
                                DialogUtil.errorDialog(itemView.getContext(), "Restore Failed", t.getMessage());

                                swipeRevealLayout.close(true);
                            }
                        });
                    },
                    (dlg) -> {
                        dlg.dismissWithAnimation();
                        swipeRevealLayout.close(true);
                    }, false);
        });

        deleteLayout.setOnClickListener(v -> {
            DialogUtil.warningDialog(itemView.getContext(), Constants.DELETE_TEACHER_TAB_ACTIVE ? "Permanent Delete" : "Confirm Delete", Constants.DELETE_TEACHER_TAB_ACTIVE ? "Delete? This action cannot be reverted" : "Are you sure you want to delete this?", "Yes", "No",
                    (dlg) -> {
                        dlg.dismissWithAnimation();
                        DialogUtil.progressDialog(itemView.getContext(), "Deleting teacher...", itemView.getContext().getResources().getColor(R.color.themeColor), false);
                        TeacherAPI api = AppInstance.retrofit().create(TeacherAPI.class);
                        Call<ServerResponse<TeacherItem>> call = Constants.DELETE_TEACHER_TAB_ACTIVE ? api.permanentDeleteTeacher(teacher.id) : api.deleteTeacher(teacher.id);
                        call.enqueue(new Callback<ServerResponse<TeacherItem>>() {
                            @Override
                            public void onResponse(@NotNull Call<ServerResponse<TeacherItem>> call, @NotNull Response<ServerResponse<TeacherItem>> response)
                            {
                                ServerResponse<TeacherItem> server = response.body();
                                DialogUtil.dismissDialog();

                                if(server != null && !server.hasError)
                                {
                                    itemView.getContext().sendBroadcast(new Intent(Constants.TRIGGER_REFRESH_LIST));
                                    DialogUtil.successDialog(itemView.getContext(), "Delete Success", "Teacher has been deleted successfully!");
                                }
                                else if(server != null && server.hasError)
                                    DialogUtil.errorDialog(itemView.getContext(), "Delete Failed", server.message);
                                else
                                    DialogUtil.errorDialog(itemView.getContext(), "Delete Failed", "Server returned an unexpected result");

                                swipeRevealLayout.close(true);
                            }

                            @Override
                            public void onFailure(@NotNull Call<ServerResponse<TeacherItem>> call, @NotNull Throwable t) {
                                DialogUtil.errorDialog(itemView.getContext(), "Delete Failed", t.getMessage());

                                swipeRevealLayout.close(true);
                            }
                        });
                    },
                    (dlg) -> {
                        dlg.dismissWithAnimation();
                        swipeRevealLayout.close(true);
                    }, false);
        });
    }

    @Override
    protected void clear()
    {}
}
