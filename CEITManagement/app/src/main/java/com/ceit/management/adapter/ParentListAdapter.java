package com.ceit.management.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ceit.management.R;
import com.ceit.management.adapter.holder.BaseViewHolder;
import com.ceit.management.adapter.holder.ParentListHolder;
import com.ceit.management.pojo.ParentItem;

import java.util.ArrayList;
import java.util.List;

public class ParentListAdapter extends RecyclerView.Adapter<BaseViewHolder> implements Filterable
{
    private static final int VIEW_TYPE_NORMAL = 1;

    private List<ParentItem> parentItems;
    private List<ParentItem> copy;

    public ParentListAdapter(List<ParentItem> parentItems)
    {
        this.copy = new ArrayList<>();
        this.parentItems = parentItems;
        this.copy.addAll(parentItems);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new ParentListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_parent_item, parent, false), parentItems);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position)
    {
        holder.onBind(position);
    }

    @Override
    public int getItemViewType(int position)
    {
        return VIEW_TYPE_NORMAL;
    }

    @Override
    public int getItemCount()
    {
        return (parentItems == null) ? 0 : parentItems.size();
    }

    @Override
    public Filter getFilter()
    {
        return filter;
    }

    public void addParentItem(List<ParentItem> parentItems)
    {
        this.parentItems.addAll(parentItems);
        this.copy.addAll(parentItems);
        notifyDataSetChanged();
    }

    public void clear()
    {
        parentItems.clear();
        notifyDataSetChanged();
    }

    public ParentItem getParent(int position)
    {
        return parentItems.get(position);
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            List<ParentItem> filteredItems = new ArrayList<>();

            if(constraint != null && constraint.length() > 0 && !constraint.toString().isEmpty())
                for(ParentItem parent : parentItems)
                {
                    if (parent.name.toLowerCase().contains(constraint.toString().toLowerCase().trim()))
                        filteredItems.add(parent);
                }
            else
                filteredItems.addAll(copy);

            FilterResults results = new FilterResults();
            results.values = filteredItems;
            return  results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            parentItems.clear();
            parentItems.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}