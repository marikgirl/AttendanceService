package ru.sfedu.AttendanceService.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.sfedu.AttendanceService.api.IDataProvider;
import ru.sfedu.AttendanceService.model.beans.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public abstract class DataProviderTest {
    public IDataProvider dataProvider;
    private static Logger log = LogManager.getLogger(DataProviderTest.class);
    public abstract void deleteRecords();
    public abstract IDataProvider getDataProvider();

    @Before
    public void beforeTesting(){
        deleteRecords();
        dataProvider = getDataProvider();
        insertRecords(dataProvider);
    }

    @After
    public void afterTesting(){
        deleteRecords();
    }

    public static void insertRecords(IDataProvider dataProvider) {
        Parent parent1 = new Parent(1L, "Alexandra Strokes", 0);
        Parent parent2 = new Parent(2L, "Graham Blur", 0);
        Parent parent3 = new Parent(3L, "Brittany Thunderstorms", 0);
        Student student1 = new Student(1L, "Alexa Chung", 1L, 7, "school 117");
        Student student2 = new Student(2L, "Damon Albarn", 2L, 8, "school 117");
        Student student3 = new Student(3L, "Alex Turner", 3L, 9, "lyceum 103");
        Student student4 = new Student(4L, "Julian Casablancas", 3, 11, "lyceum 103");
        Group group1 = new Group(1L, "Programming Middle", List.of(2L, 3L, 4L));
        Group group2 = new Group(2L, "Programming Junior", List.of(4L));
        Group group3 = new Group(3L, "English", List.of(1L, 3L));
        Admin admin1 = new Admin(new User(1L, "Eugene Troitsky"), "Owner", "RSPU");
        Admin admin2 = new Admin(new User(2L, "Igor Lorentz"), "Owner", "RSU");
        Teacher teacher1 = new Teacher(new User(3L, "Maria Leonova"), List.of(1L, 2L), "Programming");
        Teacher teacher2 = new Teacher(new User(4L, "Victoria Lorentz"), List.of(3L), "English");
        Attendance att1 = new Attendance(1L, LocalDate.parse("23-08-2021", DateTimeFormatter.ofPattern("dd-MM-yyyy")), 2L, 4L, true, AbsenceDetails.NONE);
        Attendance att2 = new Attendance(2L, LocalDate.parse("23-08-2021", DateTimeFormatter.ofPattern("dd-MM-yyyy")), 3L, 1L, false, AbsenceDetails.ILLNESS);
        Attendance att3 = new Attendance(3L, LocalDate.parse("23-08-2021", DateTimeFormatter.ofPattern("dd-MM-yyyy")), 3L, 3L, true, AbsenceDetails.ABSENCE);

        dataProvider.addAdmin(admin1);
        dataProvider.addAdmin(admin2);
        dataProvider.addParent(parent1);
        dataProvider.addParent(parent2);
        dataProvider.addParent(parent3);
        dataProvider.addStudent(student1);
        dataProvider.addStudent(student2);
        dataProvider.addStudent(student3);
        dataProvider.addStudent(student4);
        dataProvider.addGroup(group1);
        dataProvider.addGroup(group2);
        dataProvider.addGroup(group3);
        dataProvider.addTeacher(teacher1);
        dataProvider.addTeacher(teacher2);
        dataProvider.addAttendance(att1);
        dataProvider.addAttendance(att2);
        dataProvider.addAttendance(att3);

    }

    @Test
    public void editGroupStudentsPositive(){
        assertTrue(dataProvider.editGroupStudents(2L, 3L, false));
        Group group = (Group) dataProvider.getGroupById(2L).get();
        assertEquals(List.of(3L, 4L), group.getStudentsId());
        assertTrue(dataProvider.editGroupStudents(2L, 3L, true));
        group = (Group) dataProvider.getGroupById(2L).get();
        assertEquals(List.of(4L), group.getStudentsId());
    }

    @Test
    public void editGroupStudentsNegative(){
        assertFalse(dataProvider.editGroupStudents(2L, 5L, false));
        Group group = (Group) dataProvider.getGroupById(2L).get();
        assertNotEquals(List.of(3L, 5L), group.getStudentsId());
        assertFalse(dataProvider.editGroupStudents(2L, 5L, true));
    }

    @Test
    public void deleteStudentFromGroupPositive(){
        Group group = (Group) dataProvider.getGroupById(1L).get();
        assertNotEquals(List.of(2L, 3L), group.getStudentsId());
        assertTrue(dataProvider.deleteStudentFromGroup(1L, 4L));
        group = (Group) dataProvider.getGroupById(1L).get();
        assertEquals(List.of(2L, 3L), group.getStudentsId());
    }

    @Test
    public void deleteStudentFromGroupNegative(){
        assertEquals(dataProvider.getStudentById(5L), Optional.empty());
        assertFalse(dataProvider.deleteStudentFromGroup(1L, 5L));
    }

    @Test
    public void editGroupPositive(){

    }

    @Test
    public void editGroupNegative(){

    }

    @Test
    public void editStudentPositive(){

    }

    @Test
    public void editStudentNegative(){

    }

    @Test
    public void editParentPositive(){

    }

    @Test
    public void editParentNegative(){

    }

    @Test
    public void editDebtPositive(){

    }

    @Test
    public void editDebtNegative(){

    }

    @Test
    public void setAttendancePositive(){

    }

    @Test
    public void setAttendanceNegative(){

    }

    @Test
    public void selectStudentPositive(){

    }

    @Test
    public void selectStudentNegative(){

    }

    @Test
    public void selectGroupPositive(){

    }

    @Test
    public void selectGroupNegative(){

    }

    @Test
    public void setDatePositive(){

    }

    @Test
    public void setDateNegative(){

    }

    @Test
    public void getAllDebtsPositive(){

    }

    @Test
    public void getAllDebtsNegative(){

    }
}
