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
    int phone;
    ArrayList<String> courses;
    Image displayPicture;

    public Student(String name, String email, int phone, ArrayList<String> courses, Image displayPicture)
    {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.courses = courses;
        this.displayPicture = displayPicture;
    }

    public Student() {
    }
}
