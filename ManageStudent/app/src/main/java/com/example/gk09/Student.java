package com.example.gk09;

import java.io.Serializable;

public class Student implements Serializable {
    private String id;
    private String name;
    private String studentClass;
    private int age;
    private String phone;
    private String email;
    private String address;
    private String imageUrl;

    public Student() {}

    public Student(String id, String name, String studentClass, int age, String phone, String email, String address, String imageUrl) {
        this.id = id;
        this.name = name;
        this.studentClass = studentClass;
        this.age = age;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStudentClass() { return studentClass; }
    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
