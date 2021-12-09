package ru.sfedu.AttendanceService.model.beans;

import com.opencsv.bean.CsvBindByName;
import org.simpleframework.xml.Element;

import java.io.Serializable;
import java.util.Objects;

public class Student implements Serializable {
    @Element
    @CsvBindByName
    private long id;
    @Element
    @CsvBindByName
    private String name;
    @Element
    @CsvBindByName
    private long parentById;
    @Element
    @CsvBindByName
    private int classNumber;
    @Element
    @CsvBindByName
    private String school;

    public Student() {
        this.id = 0L;
    }

    public long getId() {
        return id;
    }

    public void setId() {
        this.id = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getParentById() {
        return parentById;
    }

    public void setParentById(long parentById) {
        this.parentById = parentById;
    }

    public int getClassNumber() {
        return classNumber;
    }

    public void setClassNumber(int classNumber) {
        this.classNumber = classNumber;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return id == student.id && parentById == student.parentById && classNumber == student.classNumber && Objects.equals(name, student.name) && Objects.equals(school, student.school);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, parentById, classNumber, school);
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parentById=" + parentById +
                ", classNumber=" + classNumber +
                ", school='" + school + '\'' +
                '}';
    }
}
