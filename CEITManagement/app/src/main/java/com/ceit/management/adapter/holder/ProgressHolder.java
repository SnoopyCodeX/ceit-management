package com.ceit.management.adapter.holder;

import android.view.View;
import butterknife.ButterKnife;

public class ProgressHolder extends BaseViewHolder
{
    public ProgressHolder(View itemView)
    {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    protected void clear()
    {}
}
