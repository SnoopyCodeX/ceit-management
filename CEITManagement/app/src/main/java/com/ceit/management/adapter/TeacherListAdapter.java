package com.ceit.management.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ceit.management.R;
import com.ceit.management.adapter.holder.BaseViewHolder;
import com.ceit.management.adapter.holder.ProgressHolder;
import com.ceit.management.adapter.holder.TeacherListHolder;
import com.ceit.management.pojo.TeacherItem;

import java.util.List;

public class TeacherListAdapter extends RecyclerView.Adapter<BaseViewHolder>
{
    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private boolean isLoaderVisible = false;

    private List<TeacherItem> teacherItems;

    public TeacherListAdapter(List<TeacherItem> teacherItems)
    {
        this.teacherItems = teacherItems;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        switch(viewType)
        {
            case VIEW_TYPE_NORMAL:
                return new TeacherListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_teacher_item, parent, false), teacherItems);

            case VIEW_TYPE_LOADING:
                return new ProgressHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_progress_item, parent, false));
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position)
    {
        holder.onBind(position);
    }

    @Override
    public int getItemViewType(int position)
    {
        if (isLoaderVisible)
            return position == teacherItems.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
        else
            return VIEW_TYPE_NORMAL;
    }

    @Override
    public int getItemCount()
    {
        return (teacherItems == null) ? 0 : teacherItems.size();
    }

    public void addTeacherItem(List<TeacherItem> teacherItems)
    {
        this.teacherItems.addAll(teacherItems);
        notifyDataSetChanged();
    }

    public void addLoading()
    {
        this.isLoaderVisible = true;
        this.teacherItems.add(TeacherItem.newTeacher("", "", "", ""));
        notifyItemInserted(teacherItems.size() - 1);
    }

    public void removeLoading()
    {
        this.isLoaderVisible = false;
        int position = teacherItems.size() - 1;
        TeacherItem item = teacherItems.get(position);
        if(item != null)
        {
            teacherItems.remove(position);
            notifyItemRemoved(position);
        }
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
}
