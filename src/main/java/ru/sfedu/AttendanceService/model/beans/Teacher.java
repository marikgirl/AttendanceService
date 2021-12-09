package ru.sfedu.AttendanceService.model.beans;

import com.opencsv.bean.CsvBindByName;
import org.simpleframework.xml.ElementList;

import java.util.List;
import java.util.Objects;

public class Teacher extends User{
    @ElementList(inline = true, required = false)
    @CsvBindByName
    private List<Long> groupsById;

    public List<Long> getGroupsById() {
        return groupsById;
    }

    public void setGroupsById(List<Long> groupsById) {
        this.groupsById = groupsById;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Teacher teacher = (Teacher) o;
        return Objects.equals(groupsById, teacher.groupsById);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), groupsById);
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "groupsById=" + groupsById +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
