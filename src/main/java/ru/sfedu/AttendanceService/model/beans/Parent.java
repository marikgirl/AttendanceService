package ru.sfedu.AttendanceService.model.beans;

import com.opencsv.bean.CsvBindByName;
import org.simpleframework.xml.Element;

import java.io.Serializable;
import java.util.Objects;

public class Parent implements Serializable {
    @Element
    @CsvBindByName
    private long id;
    @Element
    @CsvBindByName
    private String name;
    @Element
    @CsvBindByName
    private int debt;

    public Parent() {
        this.id = 0L;
        this.debt = 0;
    }

    public Parent(long id, String name, int debt) {
        this.id = id;
        this.name = name;
        this.debt = debt;
    }

    public long getId() {
        return id;
    }

    public void setId() {this.id = System.currentTimeMillis();}

    public void setId(long id){
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDebt() {
        return debt;
    }

    public void setDebt(int debt) {
        this.debt = debt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parent parent = (Parent) o;
        return id == parent.id && debt == parent.debt && Objects.equals(name, parent.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, debt);
    }

    @Override
    public String toString() {
        return "Parent{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", debt=" + debt +
                '}';
    }
}
