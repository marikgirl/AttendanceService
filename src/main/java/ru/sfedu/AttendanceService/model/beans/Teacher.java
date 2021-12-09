package ru.sfedu.AttendanceService.model.beans;

import com.opencsv.bean.CsvBindByName;
import org.simpleframework.xml.ElementList;

import java.util.List;
import java.util.Objects;

public class Teacher extends User{
    @ElementList(inline = true, required = false)
    @CsvBindByName
    private List<Long> groupsId;

    public List<Long> getGroupsId() {
        return groupsId;
    }

    public void setGroupsId(List<Long> groupsId) {
        this.groupsId = groupsId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Teacher teacher = (Teacher) o;
        return Objects.equals(groupsId, teacher.groupsId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), groupsId);
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "groupsById=" + groupsId +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
