package com.ceit.management.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import java.util.Arrays;
import java.util.List;

public class SelectedChildAdapter extends ArrayAdapter<StudentItem>
{
    private OnItemsChangedListener listener2;
    private OnListEmptyListener listener1;
    private List<StudentItem> students;
    private boolean editMode = false;
    private Context context;

    public SelectedChildAdapter(Context context, ArrayList<StudentItem> list)
    {
        super(context, R.layout.list_selected_child_item, list);

        this.context = context;
        this.students = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        @SuppressLint("ViewHolder") View root = LayoutInflater.from(context).inflate(R.layout.list_selected_child_item, parent, false);
        StudentItem student = students.get(position);
        ShapeableImageView profile = root.findViewById(R.id.student_photo);
        ImageView remove = root.findViewById(R.id.remove);
        TextView name = root.findViewById(R.id.student_name);
        TextView section = root.findViewById(R.id.student_class);
        TextView gender = root.findViewById(R.id.student_gender);

        //profile.setShapeAppearanceModel(ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, 80).build());

        Glide.with(context)
                .load(student.photo)
                .circleCrop()
                .placeholder(student.gender.equals("Male") ? R.drawable.male : R.drawable.female)
                .error(student.gender.equals("Male") ? R.drawable.male : R.drawable.female)
                .into(profile);

        remove.setVisibility(editMode ? View.VISIBLE : View.GONE);
        remove.setOnClickListener(v -> {
            if(listener2 != null)
                listener2.onItemChanged(students.get(position).id, -1);

            students.remove(position);
            this.notifyDataSetChanged();

            if(students.isEmpty() && listener1 != null)
                listener1.onListEmpty();
        });

        name.setText(student.name);
        section.setText(student.section);
        gender.setText(student.gender);

        return root;
    }

    public void setOnListEmptyListener(OnListEmptyListener listener)
    {
        this.listener1 = listener;
    }

    public void setOnItemsChangedListener(OnItemsChangedListener listener)
    {
        this.listener2 = listener;
    }

    @Override
    public void add(@Nullable StudentItem object)
    {
        students.add(object);
        this.notifyDataSetChanged();

        if(listener2 != null)
            listener2.onItemChanged(object.id, 1);
    }

    @Override
    public void addAll(StudentItem... items)
    {
        students.addAll(Arrays.asList(items));
        this.notifyDataSetChanged();
    }

    public List<StudentItem> getStudents() {
        return students;
    }

    public int[] getStudentIds()
    {
        int[] ids = new int[students.size()];
        int index = 0;

        if(students.size() > 0)
            for(StudentItem item : students)
                ids[index++] = item.id;

        return ids;
    }

    public void setEditMode(boolean editMode)
    {
        this.editMode = editMode;
    }

    public static interface OnListEmptyListener {
        public void onListEmpty();
    }

    public static interface OnItemsChangedListener {
        public void onItemChanged(int id, int type);
    }
}
