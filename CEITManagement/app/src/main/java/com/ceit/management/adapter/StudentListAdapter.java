package com.ceit.management.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ceit.management.R;
import com.ceit.management.adapter.holder.BaseViewHolder;
import com.ceit.management.adapter.holder.StudentListHolder;
import com.ceit.management.pojo.StudentItem;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import java.util.ArrayList;
import java.util.List;

public class StudentListAdapter extends RecyclerView.Adapter<BaseViewHolder> implements Filterable
{
    private static final int VIEW_TYPE_NORMAL = 1;

    private ViewBinderHelper binderHelper = new ViewBinderHelper();
    private List<StudentItem> studentItems;
    private List<StudentItem> copy;

    public StudentListAdapter(List<StudentItem> studentItems)
    {
        this.copy = new ArrayList<>();
        this.studentItems = studentItems;
        this.copy.addAll(studentItems);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new StudentListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_student_item, parent, false), studentItems);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position)
    {
        if(studentItems != null && 0 <= position && position < studentItems.size())
            binderHelper.bind(((StudentListHolder) holder).swipeRevealLayout, studentItems.get(position).name + "_" + studentItems.get(position).id);

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
        return (studentItems == null) ? 0 : studentItems.size();
    }

    @Override
    public Filter getFilter()
    {
        return filter;
    }

    public void addStudentItem(List<StudentItem> teacherItems)
    {
        this.studentItems.addAll(teacherItems);
        this.copy.addAll(teacherItems);
        notifyDataSetChanged();
    }

    public void clear()
    {
        studentItems.clear();
        notifyDataSetChanged();
    }

    public StudentItem getStudent(int position)
    {
        return studentItems.get(position);
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            List<StudentItem> filteredStudents = new ArrayList<>();

            if(constraint != null && constraint.length() > 0 && !constraint.toString().isEmpty())
                for(StudentItem student : studentItems)
                {
                    if (student.name.toLowerCase().contains(constraint.toString().toLowerCase().trim()))
                        filteredStudents.add(student);
                }
            else
                filteredStudents.addAll(copy);

            FilterResults results = new FilterResults();
            results.values = filteredStudents;
            return  results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            studentItems.clear();
            studentItems.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}