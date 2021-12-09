package ru.sfedu.AttendanceService.model.beans;

import com.opencsv.bean.CsvBindByName;
import org.simpleframework.xml.Element;

import java.util.Objects;

public class Admin extends User{
    @Element
    @CsvBindByName
    private String position;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Admin admin = (Admin) o;
        return Objects.equals(position, admin.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), position);
    }

    @Override
    public String toString() {
        return "Admin{" +
                "position='" + position + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
