package com.ceit.management.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TeacherItem
{
    @SerializedName("id")
    @Expose(serialize = false, deserialize = true)
    public int id;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("rank")
    @Expose
    public String rank;

    @SerializedName("gender")
    @Expose
    public String gender;

    @SerializedName("email")
    @Expose
    public String email;

    @SerializedName("contact_number")
    @Expose
    public String contactNumber;

    @SerializedName("religion")
    @Expose
    public String religion;

    @SerializedName("address")
    @Expose
    public String address;

    @SerializedName("birthday")
    @Expose
    public String birthday;

    @SerializedName("photo")
    @Expose
    public String photo;

    private TeacherItem()
    {}

    private TeacherItem(String name, String rank, String gender, String email, String contactNumber, String religion, String address, String birthday, String photo)
    {
        this.name = name;
        this.rank = rank;
        this.gender = gender;
        this.email = email;
        this.contactNumber = contactNumber;
        this.religion = religion;
        this.address = address;
        this.birthday = birthday;
        this.photo = photo;
    }

    public static synchronized TeacherItem newTeacher(String name, String rank, String gender, String email, String contactNumber, String religion, String address, String birthday, String photo)
    {
        return new TeacherItem(name, rank, gender, email, contactNumber, religion, address, birthday, photo);
    }

    public static synchronized TeacherItem newTeacher()
    {
        return new TeacherItem();
    }
}