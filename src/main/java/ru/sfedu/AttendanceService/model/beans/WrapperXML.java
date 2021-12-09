package ru.sfedu.AttendanceService.model.beans;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;


@Root(name = "main")
public class WrapperXML<T>{
    @ElementList(inline = true, required = false)
    public List<T> list;

    public WrapperXML() {
    }

    public WrapperXML(List<T> list) {
        this.list = list;
    }


    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}