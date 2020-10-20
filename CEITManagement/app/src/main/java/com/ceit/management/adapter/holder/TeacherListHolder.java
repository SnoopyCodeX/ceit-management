package com.ceit.management.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ceit.management.R;
import com.ceit.management.pojo.TeacherItem;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TeacherListHolder extends BaseViewHolder
{
    @BindView(R.id.teacher_photo)
    public ShapeableImageView teacherPhoto;

    @BindView(R.id.teacher_name)
    public TextView teacherName;

    @BindView(R.id.teacher_position)
    public TextView teacherPosition;

    @BindView(R.id.teacher_rank)
    public TextView teacherRank;

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
        teacherName.setText(teacher.teacherName);
        teacherPosition.setText(teacher.teacherPosition);
        teacherRank.setText(teacher.teacherRank);

        Glide.with(itemView.getContext())
                .load(teacher.teacherPhoto)
                .placeholder(R.drawable.teacher)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(teacherPhoto);

        itemView.setOnClickListener((View v) -> {
            
        });
    }

    @Override
    protected void clear()
    {}
}
