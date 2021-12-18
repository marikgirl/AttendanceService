package ru.sfedu.AttendanceService.model.beans;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import org.simpleframework.xml.Element;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class Attendance implements Serializable {
    @Element
    @CsvBindByName
    private long id;
    @Element
    @CsvDate(value = "dd-MM-yyyy", writeFormat = "dd-MM-yyyy")
    @CsvBindByName
    private LocalDate date;
    @Element
    @CsvBindByName
    private long groupById;
    @Element
    @CsvBindByName
    private long studentById;
    @Element
    @CsvBindByName
    private boolean status;
    @Element
    @CsvBindByName
    private AbsenceDetails details;

    public Attendance() {
        this.id = 0L;
    }

    public long getId() {return id;}

    public void setId() {this.id = System.currentTimeMillis();}

    public void setId(long id){
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getGroupById() {
        return groupById;
    }

    public void setGroupById(long groupById) {
        this.groupById = groupById;
    }

    public long getStudentById() {
        return studentById;
    }

    public void setStudentById(long studentById) {
        this.studentById = studentById;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public AbsenceDetails getDetails() {
        return details;
    }

    public void setDetails(AbsenceDetails details) {
        this.details = details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attendance that = (Attendance) o;
        return groupById == that.groupById && studentById == that.studentById && status == that.status && Objects.equals(date, that.date) && details == that.details;
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, groupById, studentById, status, details);
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "date=" + date +
                ", groupById=" + groupById +
                ", studentById=" + studentById +
                ", status=" + status +
                ", details=" + details +
                '}';
    }
}
