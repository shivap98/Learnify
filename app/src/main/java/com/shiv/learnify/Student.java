package com.shiv.learnify;

/*
 * Created by Shiv Paul on 10/13/2018.
 */

import android.media.Image;
import java.util.ArrayList;

public class Student
{
    String name;
    String email;
    String phone;
    ArrayList<String> courses;
    String displayPicturelink;

    public Student(String name, String email, String phone, ArrayList<String> courses, String displayPictureLink)
    {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.courses = courses;
        this.displayPicturelink = displayPictureLink;
    }

    public Student() {
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone=" + phone +
                ", courses=" + courses +
                ", displayPicture=" + displayPicturelink +
                '}';
    }
}
