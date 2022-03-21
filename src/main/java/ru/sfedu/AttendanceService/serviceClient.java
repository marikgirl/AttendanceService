package ru.sfedu.AttendanceService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.StackLocatorUtil;
import ru.sfedu.AttendanceService.api.DataProviderCSV;
import ru.sfedu.AttendanceService.api.DataProviderH2;
import ru.sfedu.AttendanceService.api.DataProviderXML;
import ru.sfedu.AttendanceService.model.beans.*;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static ru.sfedu.AttendanceService.utils.ConfigurationUtil.getConfigurationEntry;


public class serviceClient {
    private static Logger log = LogManager.getLogger(serviceClient.class);

    public serviceClient() {
        log.debug("AttendanceService[0]: starting application.........");
    }

    public void logBasicSystemInfo() {



//        Parent parentBean = new Parent();
//        Student studentBean = new Student();
//        Group groupBean = new Group();
//        List<Long> students = new ArrayList<>();
//        DataProviderH2 dataProviderH2 = new DataProviderH2();
//
//        parentBean.setId(2);
//        parentBean.setName("Parent1");
//        parentBean.setDebt(0);
//        dataProviderH2.addParent(parentBean);
//        parentBean.setName("justParent2");
//        dataProviderH2.updateParent(parentBean.getId(), parentBean);
//        dataProviderH2.deleteParent(1);
//        dataProviderH2.getAllParents().get().stream().forEach(p->{
//            log.info(p.getId() + p.getName() + p.getDebt());
//        });
//
//        studentBean.setId();
//        studentBean.setName("Student1");
//        studentBean.setClassNumber(6);
//        studentBean.setSchool("school 103");
//        studentBean.setParentId(parentBean.getId());
//        students.add(studentBean.getId());
//        dataProviderXML.addStudent(studentBean);
//
//        studentBean.setId();
//        studentBean.setName("Student2");
//        studentBean.setClassNumber(7);
//        studentBean.setSchool("school 103");
//        studentBean.setParentId(parentBean.getId());
//        students.add(studentBean.getId());
//        dataProviderXML.addStudent(studentBean);
//
//        groupBean.setId();
//        groupBean.setName("Programming");
//        groupBean.setStudentsId(students);
//        dataProviderXML.addGroup(groupBean);
//
//        dataProviderXML.getAllParents();
//        dataProviderXML.getAllStudents();
//        dataProviderXML.getAllGroups();

    }

}
