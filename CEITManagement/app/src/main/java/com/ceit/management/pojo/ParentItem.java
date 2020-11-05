package com.ceit.management.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ParentItem
{
    @SerializedName("id")
    @Expose(serialize = false, deserialize = true)
    public int id;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("gender")
    @Expose
    public String gender;

    @SerializedName("child")
    @Expose
    public int[] child;

    @SerializedName("religion")
    @Expose
    public String religion;

    @SerializedName("birthday")
    @Expose
    public String birthday;

    @SerializedName("address")
    @Expose
    public String address;

    @SerializedName("contactNumber")
    @Expose
    public String contactNumber;

    @SerializedName("email")
    @Expose
    public String email;

    @SerializedName("occupation")
    @Expose
    public String occupation;

    @SerializedName("photo")
    @Expose
    public String photo;

    private ParentItem()
    {}

    private ParentItem(String name, String gender, int[] child, String religion, String birthday, String address, String contactNumber, String email, String occupation, String photo)
    {
        this.name = name;
        this.gender = gender;
        this.child = child;
        this.religion = religion;
        this.birthday = birthday;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.occupation = occupation;
        this.photo = photo;
    }

    private ParentItem(int id, String name, String gender, int[] child, String religion, String birthday, String address, String contactNumber, String email, String occupation, String photo)
    {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.child = child;
        this.religion = religion;
        this.birthday = birthday;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.occupation = occupation;
        this.photo = photo;
    }

    public static synchronized ParentItem newParent(String name, String gender, int[] child, String religion, String birthday, String address, String contactNumber, String email, String occupation, String photo)
    {
        return new ParentItem(name, gender, child, religion, birthday, address, contactNumber, email, occupation, photo);
    }

    public static synchronized ParentItem newParent()
    {
        return new ParentItem();
    }
}
