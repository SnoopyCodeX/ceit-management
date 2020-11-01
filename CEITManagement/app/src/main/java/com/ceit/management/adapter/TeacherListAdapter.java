package com.ceit.management.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ceit.management.R;
import com.ceit.management.adapter.holder.BaseViewHolder;
import com.ceit.management.adapter.holder.TeacherListHolder;
import com.ceit.management.pojo.TeacherItem;

import java.util.ArrayList;
import java.util.List;

public class TeacherListAdapter extends RecyclerView.Adapter<BaseViewHolder> implements Filterable
{
    private static final int VIEW_TYPE_NORMAL = 1;

    private List<TeacherItem> teacherItems;
    private List<TeacherItem> copy;

    public TeacherListAdapter(List<TeacherItem> teacherItems)
    {
        this.copy = new ArrayList<>();
        this.teacherItems = teacherItems;
        this.copy.addAll(teacherItems);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new TeacherListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_teacher_item, parent, false), teacherItems);
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
        return (teacherItems == null) ? 0 : teacherItems.size();
    }

    @Override
    public Filter getFilter()
    {
        return filter;
    }

    public void addTeacherItem(List<TeacherItem> teacherItems)
    {
        this.teacherItems.addAll(teacherItems);
        this.copy.addAll(teacherItems);
        notifyDataSetChanged();
    }

    public void clear()
    {
        teacherItems.clear();
        notifyDataSetChanged();
    }

    public TeacherItem getTeacher(int position)
    {
        return teacherItems.get(position);
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            List<TeacherItem> filteredTeachers = new ArrayList<>();

            if(constraint != null && constraint.length() > 0 && !constraint.toString().isEmpty())
                for(TeacherItem teacher : teacherItems)
                {
                    if (teacher.name.toLowerCase().contains(constraint.toString().toLowerCase().trim()))
                        filteredTeachers.add(teacher);
                }
            else
                filteredTeachers.addAll(copy);

            FilterResults results = new FilterResults();
            results.values = filteredTeachers;
            return  results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            teacherItems.clear();
            teacherItems.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
