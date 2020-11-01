package com.ceit.management.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.ceit.management.R;
import com.ceit.management.pojo.ClassItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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

        itemView.setOnClickListener((View v) -> {

        });
    }

    @Override
    protected void clear()
    {}
}
