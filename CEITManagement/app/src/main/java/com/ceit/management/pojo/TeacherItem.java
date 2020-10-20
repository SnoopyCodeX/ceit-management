package com.ceit.management.pojo;

public class TeacherItem
{
    public String teacherPhoto;
    public String teacherName;
    public String teacherPosition;
    public String teacherRank;

    private TeacherItem()
    {}

    private TeacherItem(String photo, String name, String position, String rank)
    {
        this.teacherPhoto = photo;
        this.teacherName = name;
        this.teacherPosition = position;
        this.teacherRank = rank;
    }

    public static synchronized TeacherItem newTeacher(String photo, String name, String position, String rank)
    {
        return new TeacherItem(photo, name, position, rank);
    }
}