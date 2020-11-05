package com.ceit.management.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ceit.management.R;
import com.ceit.management.adapter.holder.BaseViewHolder;
import com.ceit.management.adapter.holder.ClassListHolder;
import com.ceit.management.pojo.ClassItem;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import java.util.ArrayList;
import java.util.List;

public class ClassListAdapter extends RecyclerView.Adapter<BaseViewHolder> implements Filterable
{
    private static final int VIEW_TYPE_NORMAL = 1;

    private ViewBinderHelper binderHelper = new ViewBinderHelper();
    private List<ClassItem> classItems;
    private List<ClassItem> copy;

    public ClassListAdapter(List<ClassItem> classItems)
    {
        this.copy = new ArrayList<>();
        this.classItems = classItems;
        this.copy.addAll(classItems);

        binderHelper.setOpenOnlyOne(true);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new ClassListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_class_item, parent, false), classItems);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position)
    {
        if(classItems != null && 0 <= position && position < classItems.size())
        {
            binderHelper.bind(((ClassListHolder) holder).swipeRevealLayout, classItems.get(position).name + "_" + classItems.get(position).id);
            holder.onBind(position);
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        return VIEW_TYPE_NORMAL;
    }

    @Override
    public int getItemCount()
    {
        return (classItems == null) ? 0 : classItems.size();
    }

    @Override
    public Filter getFilter()
    {
        return filter;
    }

    public void addClassItem(List<ClassItem> classItems)
    {
        this.classItems.addAll(classItems);
        this.copy.addAll(classItems);
        notifyDataSetChanged();
    }

    public void clear()
    {
        classItems.clear();
        notifyDataSetChanged();
    }

    public ClassItem getClass(int position)
    {
        return classItems.get(position);
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ClassItem> filteredClasses = new ArrayList<>();

            if (constraint != null && constraint.length() > 0 && !constraint.toString().isEmpty())
                for (ClassItem item : classItems)
                {
                    if (item.name.toLowerCase().contains(constraint.toString().toLowerCase().trim()))
                        filteredClasses.add(item);
                }
            else
                filteredClasses.addAll(copy);

            FilterResults results = new FilterResults();
            results.values = filteredClasses;
            return  results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            classItems.clear();
            classItems.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
