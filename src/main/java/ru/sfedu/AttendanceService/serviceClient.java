package ru.sfedu.AttendanceService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.StackLocatorUtil;
import ru.sfedu.AttendanceService.api.DataProviderCSV;
import ru.sfedu.AttendanceService.model.beans.*;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


public class serviceClient {
    private static Logger log = LogManager.getLogger(serviceClient.class);

    public serviceClient() {
        log.debug("kursachClient[0]: starting application.........");
    }

    public void logBasicSystemInfo() throws IOException {
        Parent parentBean = new Parent();
        Student studentBean = new Student();
        Group groupBean = new Group();
        List<Long> students = new ArrayList<>();
        DataProviderCSV dataProviderCSV = new DataProviderCSV();


        parentBean.setId();
        parentBean.setName("someone");
        dataProviderCSV.addParent(parentBean);
        dataProviderCSV.editDebt(parentBean.getId(), 50, true);
        dataProviderCSV.editDebt(parentBean.getId(), 20, false);
//
//        studentBean.setId();
//        studentBean.setName("Student1");
//        studentBean.setClassNumber(6);
//        studentBean.setSchool("school 103");
//        studentBean.setParentById(parentBean.getId());
//
//        students.add(studentBean.getId());
//        dataProviderCSV.addStudent(studentBean);
//
//        studentBean.setId();
//        studentBean.setName("Student2");
//        studentBean.setClassNumber(7);
//        studentBean.setSchool("school 103");
//        studentBean.setParentById(parentBean.getId());
//
//        students.add(studentBean.getId());
//        dataProviderCSV.addStudent(studentBean);
//
//        groupBean.setId();
//        groupBean.setName("Programming");
//        groupBean.setStudentsById(students);
//        dataProviderCSV.addGroup(groupBean);
//
//        dataProviderCSV.getAllParents();
//        dataProviderCSV.getAllStudents();
//        dataProviderCSV.getAllGroups();


//        School schoolBean = new School();
//
//        schoolBean.setAddress("new address");
//        schoolBean.setId();
//        schoolBean.setNumber(123);
//        DataProviderCSV dataProviderCSV = new DataProviderCSV();
//        DataProviderXML dataProviderXML = new DataProviderXML();
//
//        dataProviderCSV.addSchoolRecord(schoolBean);
//        dataProviderXML.addSchoolRecord(schoolBean);
//
//        dataProviderCSV.viewAllSchool();
//        dataProviderXML.viewAllSchool();
//
//        dataProviderCSV.deleteSchoolRecord(schoolBean.getId());
//
//        dataProviderCSV.viewAllSchool();
//
//        dataProviderCSV.deleteSchoolRecord(schoolBean.getId());
        ;

        //log.info(dataProviderCSV.setAttendance("09-12-2021", "Programming", "Student", true));


    }

    public Optional getDetailFromString(String enumString){
        AbsenceDetails enumDetails = null;
        try{
            enumDetails = AbsenceDetails.valueOf(enumString);
        } catch(Exception e){
            log.error("Enum parsing from String error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.ofNullable(enumDetails);
    }
    public Enum<AbsenceDetails> a(String s){
        AbsenceDetails ap = null;
        s = s.toUpperCase(Locale.ROOT);
//        log.info(s);
        switch (s){
            case "ILLNESS":
                ap = AbsenceDetails.valueOf(s);
            case "ABSENCE":
                ap = AbsenceDetails.valueOf(s);
            case "CIRCUMSTANCES":
                ap = AbsenceDetails.valueOf(s);
        }
        return ap;
    }
}
