package ru.sfedu.AttendanceService.api;

import ru.sfedu.AttendanceService.model.beans.*;

import java.util.Optional;

public interface IDataProvider {
    boolean editGroupStudents(long groupId, long studentId, boolean isDelete);
    boolean deleteStudentFromGroup(long groupId, long studentId);
    boolean editGroup(long groupId, String groupName, boolean isDeleteGroup);
    boolean editStudent(long studentId, String studentName, long parentId, int classNumber, String school, boolean isDeleteStudent);
    boolean editParent(long parentId, String parentName, boolean isDeleteParent);
    boolean editDebt(long parentId, int debtAmount, boolean isIncreasing);
    long setAttendance(String dateString, String groupName, String studentName, boolean status, String absenceDetailsString);
    Optional selectStudent(String studentStringName);
    Optional selectGroup(String groupStringName);
    Optional setDate(String dateString);
    Optional getAllDebts();

    Optional getAllAttendance();
    Optional getAllGroups();
    Optional getAllParents();
    Optional getAllStudents();
    Optional getAllTeachers();
    Optional getAllAdmins();
    Optional getAllUsers();

    boolean addAttendance(Attendance attendance);
    boolean addGroup(Group group);
    boolean addParent(Parent parent);
    boolean addStudent(Student student);
    boolean addTeacher(Teacher teacher);
    boolean addAdmin(Admin admin);
    boolean addUser(User user);

    boolean deleteAttendance(long id);
    boolean deleteGroup(long id);
    boolean deleteParent(long id);
    boolean deleteStudent(long id);
    boolean deleteTeacher(long id);
    boolean deleteAdmin(long id);
    boolean deleteUser(long id);

    boolean updateAttendance(long id, Attendance attendance);
    boolean updateGroup(long id, Group group);
    boolean updateParent(long id, Parent parent);
    boolean updateStudent(long id, Student student);
    boolean updateTeacher(long id, Teacher teacher);
    boolean updateAdmin(long id, Admin admin);
    boolean updateUser(long id, User user);

    Optional getAttendanceById(long id);
    Optional getGroupById(long id);
    Optional getParentById(long id);
    Optional getStudentById(long id);
    Optional getTeacherById(long id);
    Optional getAdminById(long id);
    Optional getUserById(long id);
}
