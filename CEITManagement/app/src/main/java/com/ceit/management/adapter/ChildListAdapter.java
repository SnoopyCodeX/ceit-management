package com.ceit.management.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.ceit.management.R;
import com.ceit.management.pojo.StudentItem;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.ArrayList;
import java.util.List;

public class ChildListAdapter extends ArrayAdapter<StudentItem>
{
    private List<StudentItem> suggestions;
    private List<StudentItem> students;
    private List<StudentItem> all;
    private Context context;

    public ChildListAdapter(Context context, ArrayList<StudentItem> items)
    {
        super(context, R.layout.list_child_item, items);

        this.context = context;
        this.suggestions = new ArrayList<>();
        this.students = items;
        this.all = items;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_child_item, parent, false);
        }

        StudentItem student = students.get(position);
        ShapeableImageView profile = convertView.findViewById(R.id.student_photo);
        TextView name = convertView.findViewById(R.id.student_name);
        TextView section = convertView.findViewById(R.id.student_class);
        TextView gender = convertView.findViewById(R.id.student_gender);

        profile.setShapeAppearanceModel(ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, 80).build());

        Glide.with(context)
                .load(student.photo)
                .placeholder(R.drawable.student)
                .error(R.drawable.student)
                .into(profile);

        name.setText(student.name);
        section.setText(student.section);
        gender.setText(student.gender);

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter()
    {
        return nameFilter;
    }

    private final Filter nameFilter = new Filter()
    {
        private String convertToString(StudentItem object)
        {
            return (object.name);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            if(constraint != null && !constraint.toString().isEmpty())
            {
                suggestions.clear();

                for(StudentItem item : all)
                {
                    if(convertToString(item).toLowerCase().contains(constraint.toString().toLowerCase()))
                        suggestions.add(item);
                }

                FilterResults results = new FilterResults();
                results.values = suggestions;
                results.count = suggestions.size();
                return results;
            }

            return new FilterResults();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults filterResults)
        {
            List<StudentItem> results = (List<StudentItem>) filterResults.values;
            if(results != null && results.size() > 0)
            {
                clear();
                for(StudentItem item : results)
                    add(item);
                notifyDataSetChanged();
            }
        }
    };
}
