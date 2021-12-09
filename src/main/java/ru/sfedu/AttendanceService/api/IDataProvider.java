package ru.sfedu.AttendanceService.api;

import ru.sfedu.AttendanceService.model.beans.*;

import java.util.Optional;

public interface IDataProvider {

    boolean getAllAttendance();
    boolean getAllGroups();
    boolean getAllParents();
    boolean getAllStudents();
    boolean getAllTeachers();
    boolean getAllAdmins();

    boolean addAttendance(Attendance attendance);
    boolean addGroup(Group group);
    boolean addParent(Parent parent);
    boolean addStudent(Student student);
    boolean addTeacher(Teacher teacher);
    boolean addAdmin(Admin admin);

    boolean deleteAttendance(long id);
    boolean deleteGroup(long id);
    boolean deleteParent(long id);
    boolean deleteStudent(long id);
    boolean deleteTeacher(long id);
    boolean deleteAdmin(long id);

    boolean updateAttendance(long id, Attendance attendance);
    boolean updateGroup(long id, Group group);
    boolean updateParent(long id, Parent parent);
    boolean updateStudent(long id, Student student);
    boolean updateTeacher(long id, Teacher teacher);
    boolean updateAdmin(long id, Admin admin);

    Optional getAttendanceById(long id);
    Optional getGroupById(long id);
    Optional getParentById(long id);
    Optional getStudentById(long id);
    Optional getTeacherById(long id);
    Optional getAdminById(long id);
}
