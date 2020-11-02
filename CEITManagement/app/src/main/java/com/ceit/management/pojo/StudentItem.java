package com.ceit.management.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StudentItem
{
    @SerializedName("id")
    @Expose
    public int id;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("gender")
    @Expose
    public String gender;

    @SerializedName("email")
    @Expose
    public String email;

    @SerializedName("contact_number")
    @Expose
    public String contactNumber;

    @SerializedName("address")
    @Expose
    public String address;

    @SerializedName("religion")
    @Expose
    public String religion;

    @SerializedName("birthday")
    @Expose
    public String birthday;

    @SerializedName("section")
    @Expose
    public String section;

    @SerializedName("photo")
    @Expose
    public String photo;

    @SerializedName("hasError")
    @Expose
    public boolean hasError;

    @SerializedName("message")
    @Expose
    public String message;

    private StudentItem()
    {}

    private StudentItem(String name, String gender, String email, String contactNumber, String address, String religion, String birthday, String section, String photo)
    {
        this.name = name;
        this.gender = gender;
        this.email = email;
        this.contactNumber = contactNumber;
        this.address = address;
        this.religion = religion;
        this.birthday = birthday;
        this.section = section;
        this.photo = photo;
    }

    private StudentItem(int id, String name, String gender, String email, String contactNumber, String address, String religion, String birthday, String section, String photo)
    {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.email = email;
        this.contactNumber = contactNumber;
        this.address = address;
        this.religion = religion;
        this.birthday = birthday;
        this.section = section;
        this.photo = photo;
    }

    public static synchronized StudentItem newStudent(String name, String gender, String email, String contactNumber, String address, String religion, String birthday, String section, String photo)
    {
        return new StudentItem(name, gender, email, contactNumber, address, religion, birthday, section, photo);
    }

    public static synchronized StudentItem newStudent()
    {
        return new StudentItem();
    }
}
