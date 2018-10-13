package com.shiv.learnify;

/*
 * Created by Shiv Paul on 10/13/2018.
 */

public class Beacon
{
    Student student;
    String course;
    CustomLatLng location;
    String title;
    String description;

    public Beacon(Student student, String course, CustomLatLng location, String title, String description)
    {
        this.student = student;
        this.course = course;
        this.location = location;
        this.title = title;
        this.description = description;
    }

    public Beacon() {

    }

    @Override
    public String toString() {
        return "Beacon{" +
                "student=" + student +
                ", course='" + course + '\'' +
                ", location=" + location +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
