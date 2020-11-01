package com.ceit.management.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ceit.management.R;
import com.ceit.management.pojo.TeacherItem;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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

        itemView.setOnClickListener((View v) -> {
            
        });
    }

    @Override
    protected void clear()
    {}
}
