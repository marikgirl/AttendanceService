package ru.sfedu.AttendanceService.model.beans;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Group implements Serializable {
    @Element
    @CsvBindByName
    private long id;
    @Element
    @CsvBindByName
    private String name;
    @ElementList(inline = true)
    @CsvBindAndSplitByName(elementType = Long.class, splitOn = " ")
    private List<Long> studentsId;

    public Group() {
        this.id = 0L;
    }

    public long getId() {
        return id;
    }

    public void setId() {this.id = System.currentTimeMillis();}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getStudentsId() {
        return studentsId;
    }

    public void setStudentsId(List<Long> studentsId) {
        this.studentsId = studentsId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return id == group.id && Objects.equals(name, group.name) && Objects.equals(studentsId, group.studentsId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, studentsId);
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", studentsById=" + studentsId +
                '}';
    }
}
