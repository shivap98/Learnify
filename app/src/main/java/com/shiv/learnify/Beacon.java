package com.shiv.learnify;

/*
 * Created by Shiv Paul on 10/13/2018.
 */

import com.google.android.gms.maps.model.LatLng;

public class Beacon
{
    Student student;
    String course;
    LatLng location;
    String title;
    String description;

    public Beacon(Student student, String course, LatLng location, String title, String description)
    {
        this.student = student;
        this.course = course;
        this.location = location;
        this.title = title;
        this.description = description;
    }
}
