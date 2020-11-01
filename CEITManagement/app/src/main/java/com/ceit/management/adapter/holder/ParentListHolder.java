package com.ceit.management.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ceit.management.R;
import com.ceit.management.pojo.ParentItem;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ParentListHolder extends BaseViewHolder
{
    @BindView(R.id.parent_photo)
    public ShapeableImageView photo;

    @BindView(R.id.parent_name)
    public TextView name;

    @BindView(R.id.parent_occupation)
    public TextView occupation;

    @BindView(R.id.parent_gender)
    public TextView gender;

    private List<ParentItem> parentItemList;
    private View itemView;

    public ParentListHolder(View itemView, List<ParentItem> parentItemList)
    {
        super(itemView);
        this.parentItemList = parentItemList;
        this.itemView = itemView;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(int position)
    {
        super.onBind(position);

        ParentItem parent = parentItemList.get(position);
        photo.setShapeAppearanceModel(ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, 80).build());
        name.setText(parent.name);
        occupation.setText(parent.occupation);
        gender.setText(parent.gender);

        Glide.with(itemView.getContext())
                .load(parent.photo)
                .placeholder(R.drawable.parents)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(photo);

        itemView.setOnClickListener((View v) -> {

        });
    }

    @Override
    protected void clear()
    {}
}
