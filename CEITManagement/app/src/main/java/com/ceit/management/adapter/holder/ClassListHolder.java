package com.ceit.management.adapter.holder;

import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ceit.management.AppInstance;
import com.ceit.management.R;
import com.ceit.management.api.ClassAPI;
import com.ceit.management.model.ServerResponse;
import com.ceit.management.pojo.ClassItem;
import com.ceit.management.util.Constants;
import com.ceit.management.util.DialogUtil;
import com.chauthai.swipereveallayout.SwipeRevealLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassListHolder extends BaseViewHolder
{
    @BindView(R.id.class_name)
    public TextView name;

    @BindView(R.id.class_department)
    public TextView department;

    @BindView(R.id.class_adviser)
    public TextView adviser;

    private List<ClassItem> classItemList;
    private View itemView;

    @BindView(R.id.swipe)
    public SwipeRevealLayout swipeRevealLayout;

    @BindView(R.id.delete_layout)
    ImageView deleteLayout;

    @BindView(R.id.restore_layout)
    ImageView restoreLayout;

    @BindView(R.id.front_layout)
    FrameLayout frontLayout;

    public ClassListHolder(View itemView, List<ClassItem> classItems)
    {
        super(itemView);
        this.classItemList = classItems;
        this.itemView = itemView;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(int position)
    {
        super.onBind(position);

        ClassItem item = classItemList.get(position);
        name.setText(item.name);
        department.setText(item.department);
        adviser.setText(item.teacher);

        frontLayout.setOnClickListener(v -> {
            if(swipeRevealLayout.isOpened())
            {
                swipeRevealLayout.close(true);
                return;
            }

            Intent trigger = new Intent(Constants.TRIGGER_MODAL_OPEN);
            trigger.putExtra(Constants.KEY_TRIGGER_MODAL_VIEW, item.id);
            trigger.putExtra(Constants.KEY_TRIGGER_MODAL_TYPE, 4);
            itemView.getContext().sendBroadcast(trigger);
        });

        restoreLayout.setVisibility(Constants.DELETE_CLASS_TAB_ACTIVE ? View.VISIBLE : View.GONE);

        restoreLayout.setOnClickListener(v -> {
            DialogUtil.warningDialog(itemView.getContext(), "Confirm Restore", "Are you sure you want to restore this?", "Yes", "No",
                    (dlg) -> {
                        dlg.dismissWithAnimation();

                        DialogUtil.progressDialog(itemView.getContext(), "Restoring data...", itemView.getContext().getResources().getColor(R.color.themeColor), false);
                        ClassAPI api = AppInstance.retrofit().create(ClassAPI.class);
                        Call<ServerResponse<ClassItem>> call = api.restoreClass(item.id);
                        call.enqueue(new Callback<ServerResponse<ClassItem>>() {
                            @Override
                            public void onResponse(@NotNull Call<ServerResponse<ClassItem>> call, @NotNull Response<ServerResponse<ClassItem>> response)
                            {
                                ServerResponse<ClassItem> server = response.body();
                                DialogUtil.dismissDialog();

                                if(server != null && !server.hasError)
                                {
                                    itemView.getContext().sendBroadcast(new Intent(Constants.TRIGGER_REFRESH_LIST));
                                    DialogUtil.successDialog(itemView.getContext(), "Restore Success", "Class has been restored successfully!");
                                }
                                else if(server != null && server.hasError)
                                    DialogUtil.errorDialog(itemView.getContext(), "Restore Failed", server.message);
                                else
                                    DialogUtil.errorDialog(itemView.getContext(), "Restore Failed", "Server returned an unexpected result");

                                swipeRevealLayout.close(true);
                            }

                            @Override
                            public void onFailure(@NotNull Call<ServerResponse<ClassItem>> call, @NotNull Throwable t) {
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
            DialogUtil.warningDialog(itemView.getContext(), Constants.DELETE_CLASS_TAB_ACTIVE ? "Permanent Delete" : "Confirm Delete", Constants.DELETE_CLASS_TAB_ACTIVE ? "Permanently delete? This action cannot be reverted" : "Are you sure you want to delete this?", "Yes", "No",
                    (dlg) -> {
                        dlg.dismissWithAnimation();
                        DialogUtil.progressDialog(itemView.getContext(), "Deleting class...", itemView.getContext().getResources().getColor(R.color.themeColor), false);
                        ClassAPI api = AppInstance.retrofit().create(ClassAPI.class);
                        Call<ServerResponse<ClassItem>> call = Constants.DELETE_CLASS_TAB_ACTIVE ? api.permanentDeleteClass(item.id) : api.deleteClass(item.id);
                        call.enqueue(new Callback<ServerResponse<ClassItem>>() {
                            @Override
                            public void onResponse(@NotNull Call<ServerResponse<ClassItem>> call, @NotNull Response<ServerResponse<ClassItem>> response)
                            {
                                ServerResponse<ClassItem> server = response.body();
                                DialogUtil.dismissDialog();

                                if(server != null && !server.hasError)
                                {
                                    itemView.getContext().sendBroadcast(new Intent(Constants.TRIGGER_REFRESH_LIST));
                                    DialogUtil.successDialog(itemView.getContext(), "Delete Success", "Class has been deleted successfully!");
                                }
                                else if(server != null && server.hasError)
                                    DialogUtil.errorDialog(itemView.getContext(), "Delete Failed", server.message);
                                else
                                    DialogUtil.errorDialog(itemView.getContext(), "Delete Failed", "Server returned an unexpected result");

                                swipeRevealLayout.close(true);
                            }

                            @Override
                            public void onFailure(@NotNull Call<ServerResponse<ClassItem>> call, @NotNull Throwable t) {
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
