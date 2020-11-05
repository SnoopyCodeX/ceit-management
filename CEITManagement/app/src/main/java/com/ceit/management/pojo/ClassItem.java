package com.ceit.management.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ClassItem
{
    @SerializedName("id")
    @Expose(serialize = false, deserialize = true)
    public int id;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("teacher")
    @Expose
    public String teacher;

    @SerializedName("department")
    @Expose
    public String department;

    private ClassItem()
    {}

    private ClassItem(String name, String teacher, String department)
    {
        this.name = name;
        this.teacher = teacher;
        this.department = department;
    }

    private ClassItem(int id, String name, String teacher, String department)
    {
        this.id = id;
        this.name = name;
        this.teacher = teacher;
        this.department = department;
    }

    public static synchronized ClassItem newClass(String name, String teacher, String department)
    {
        return new ClassItem(name, teacher, department);
    }

    public static synchronized ClassItem newClass()
    {
        return new ClassItem();
    }
}
