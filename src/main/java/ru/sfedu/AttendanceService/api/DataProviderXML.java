package ru.sfedu.AttendanceService.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import ru.sfedu.AttendanceService.Main;
import ru.sfedu.AttendanceService.Constants;
import ru.sfedu.AttendanceService.MongoChanges;
import ru.sfedu.AttendanceService.model.beans.*;
import ru.sfedu.AttendanceService.utils.ConfigurationUtil;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

import static java.util.Objects.isNull;

public class DataProviderXML implements IDataProvider{
    private static final Logger log = LogManager.getLogger(Main.class);

    @Override
    public boolean editGroupStudents(long groupId, long studentId, boolean isDelete) {
        boolean isGroupStudentsEdited = true;
        try{
            log.debug("editGroupStudents[0]: Start editing Group students");
            Optional.of(isDelete).filter(p->p).ifPresent(p->deleteStudentFromGroup(groupId, studentId));
            Optional.of(!isDelete).filter(p->p).ifPresent(p-> {
                Group group = (Group) getGroupById(groupId).get();
                List<Long> groupStudents = group.getStudentsId();
                Optional.of(studentId).filter(i->i != 0).ifPresent(i->groupStudents.add(studentId));
                group.setStudentsId(groupStudents);
                updateGroup(groupId, group);
            });
            log.debug("editGroupStudents[1]: Editing Group students complete");
        } catch(Exception e){
            log.error("editGroupStudents[0]: Editing Group students Error");
            log.error("editGroupStudents[1]: " + e.getClass().getName() + ":" + e.getMessage());
            isGroupStudentsEdited = false;
        }
        return isGroupStudentsEdited;
    }

    @Override
    public boolean deleteStudentFromGroup(long groupId, long studentId){
        boolean isStudentDeleted = true;
        try{
            log.debug("deleteStudentFromGroup[0]: Start deleting student from group");
            Group group = (Group) getGroupById(groupId).get();
            List<Long> groupStudents = group.getStudentsId();
            Predicate<Long> isExisting = studentIndex -> studentIndex == groupStudents.indexOf(studentId);
            groupStudents.removeIf(isExisting);
            log.debug("deleteStudentFromGroup[1]: Deleting student from group complete");
        } catch (Exception e){
            log.error("deleteStudentFromGroup[0]: Deleting student from group Error");
            log.error("deleteStudentFromGroup[1]: " + e.getClass().getName() + ":" + e.getMessage());
            isStudentDeleted = false;
        }
        return isStudentDeleted;
    }

    @Override
    public boolean editGroup(long groupId, String groupName, boolean isDeleteGroup){
        boolean isGroupEdited = true;
        try{
            log.debug("editGroup[0]: Start editing Group");
            Group group = (Group) getGroupById(groupId).get();
            group.setName(groupName);
            Optional.of(group.getId()).filter(p->p != 0 && !isDeleteGroup).ifPresent(p->updateGroup(groupId, group));
            Optional.of(group.getId()).filter(p->p != 0 && isDeleteGroup).ifPresent(p->deleteGroup(groupId));
            log.debug("editGroup[1]: Editing Group complete");
        } catch (Exception e){
            log.error("editGroup[0]: Editing Group Error");
            log.error("editGroup[1]: " + e.getClass().getName() + ":" + e.getMessage());
            isGroupEdited = false;
        }
        return isGroupEdited;
    }

    @Override
    public boolean editStudent(long studentId, String studentName, long parentId, int classNumber, String school, boolean isDeleteStudent){
        boolean isStudentEdited = true;
        try{
            log.debug("editStudent[0]: Start editing Student");
            Student student = (Student) getStudentById(studentId).get();
            student.setName(studentName);
            student.setParentId(parentId);
            student.setClassNumber(classNumber);
            student.setSchool(school);
            Optional.of(student.getId()).filter(p->p != 0 && !isDeleteStudent).ifPresent(p->updateStudent(studentId, student));
            Optional.of(student.getId()).filter(p->p != 0 && isDeleteStudent).ifPresent(p->deleteStudent(studentId));
            log.debug("editStudent[1]: Editing student complete");
        } catch (Exception e){
            log.error("editStudent[0]: Editing student Error");
            log.error("editStudent[1]: " + e.getClass().getName() + ":" + e.getMessage());
            isStudentEdited = false;
        }
        return isStudentEdited;
    }

    @Override
    public boolean editParent(long parentId, String parentName, boolean isDeleteParent){
        boolean isParentEdited = true;
        try{
            log.debug("editParent[0]: Start editing Parent");
            Parent parent = (Parent) getParentById(parentId).get();
            parent.setName(parentName);
            Optional.of(parent.getId()).filter(p->p != 0 && !isDeleteParent).ifPresent(p->updateParent(parentId, parent));
            Optional.of(parent.getId()).filter(p->p != 0 && isDeleteParent).ifPresent(p->deleteParent(parentId));
            log.debug("editParent[1]: Editing parent complete");
        } catch (Exception e){
            log.error("editParent[0]: Editing parent Error");
            log.error("editParent[1]: " + e.getClass().getName() + ":" + e.getMessage());
            isParentEdited = false;
        }
        return isParentEdited;
    }

    @Override
    public boolean editDebt(long parentId, int debtAmount, boolean isIncreasing){
        boolean isDebtEdited = true;
        try{
            log.debug("editDebt[0]: Start editing parent debt");
            Parent parent = (Parent) getParentById(parentId).get();
            int parentDebt = parent.getDebt();
            int finalDebtAmount = Math.abs(debtAmount);
            Optional.of(isIncreasing).filter(p->p).ifPresent(p->parent.setDebt(parentDebt + finalDebtAmount));
            Optional.of(!isIncreasing).filter(p->p).ifPresent(p->parent.setDebt(parentDebt - finalDebtAmount));
            isDebtEdited = updateParent(parentId, parent);
            log.debug("editDebt[1]: Editing parent debt completed");
        } catch(Exception e){
            log.error("editDebt[0]: Editing parent debt Error");
            log.error("editDebt[1]: " + e.getClass().getName() + ":" + e.getMessage());
            isDebtEdited = false;
        }
        return isDebtEdited;
    }

    @Override
    public long setAttendance(String dateString, String groupName, String studentName, boolean status, String absenceDetailsString){
        Attendance attBean = new Attendance();
        try{
            log.debug("setAttendance[0]: Start setting new Attendance");
            attBean.setDate((LocalDate) setDate(dateString).orElse(
                    LocalDate.parse("01-01-1970", DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
            attBean.setGroupById((long) selectGroup(groupName).get());
            attBean.setStudentById((long) selectStudent(studentName).get());
            attBean.setStatus(status);
            attBean.setDetails((AbsenceDetails) getDetailFromString(absenceDetailsString).orElse(AbsenceDetails.NONE));
            attBean.setId();
            log.info(attBean.getId());
            addAttendance(attBean);
            log.debug("setAttendance[1]: Setting Attendance complete");
        } catch(Exception e){
            log.error("setAttendance[0]: Attendance setting Error");
            log.error("setAttendance[1]: " + e.getClass().getName() + ":" + e.getMessage());
        }
        return attBean.getId();
    }

    private Optional getDetailFromString(String enumString){
        AbsenceDetails enumDetails = null;
        try{
            log.debug("getDetailFromString[0]: Transforming String to Enum");
            enumString = enumString.toUpperCase(Locale.ROOT);
            enumDetails = AbsenceDetails.valueOf(enumString);
            log.debug("getDetailFromString[1]: Transforming String to Enum complete");
        } catch(Exception e){
            log.error("getDetailFromString[0]: Enum parsing from String error");
            log.error("getDetailFromString[1]: " + e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.ofNullable(enumDetails);
    }

    @Override
    public Optional selectStudent(String studentStringName){
        Student student = new Student();
        try{
            log.debug("selectStudent[0]: Start searching student record");
            student = (Student) getAllBeans(Constants.STUDENT_XML_SOURCE).stream()
                    .filter(studentBean -> Objects.equals(((Student) studentBean).getName(), studentStringName))
                    .findAny().get();
            log.debug("selectStudent[1]: Searching complete");
        } catch(Exception e){
            log.error("selectStudent[0]: Student searching Error");
            log.error("selectStudent[1]: " + e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.of(student.getId());
    }

    @Override
    public Optional selectGroup(String groupStringName){
        Group group = new Group();
        try{
            log.debug("selectGroup[0]: Start searching group record");
            group = (Group) getAllBeans(Constants.GROUP_XML_SOURCE).stream()
                    .filter(groupBean -> Objects.equals(((Group) groupBean).getName(), groupStringName))
                    .findAny().get();
            log.debug("selectGroup[1]: Searching Group complete");
        } catch(Exception e){
            log.error("selectGroup[0]: Group searching Error");
            log.error("selectGroup[1]: " + e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.of(group.getId());
    }

    @Override
    public Optional setDate(String dateString){
        LocalDate date = null;
        try{
            log.debug("setDate[0]: Start parsing Date");
            date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            log.debug("setDate[1]: Date parsing complete");
        } catch(Exception e){
            log.error("selectGroup[0]: Parsing date error");
            log.error("selectGroup[1]: " + e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.ofNullable(date);
    }

    @Override
    public Optional getAllDebts() {
        return getAllParents();
    }


    @Override
    public Optional getAllAttendance() {
        List<Attendance> attBeanList = null;
        try {
            log.debug("getAllAttendance[0]: Start getting all Attendance");
            attBeanList = getAllBeans(Constants.ATTENDANCE_XML_SOURCE);
            attBeanList.stream().forEach(bean -> log.info(bean));
            log.debug("getAllAttendance[1]: Getting all Attendance complete");
        } catch (Exception e){
            log.error("getAllAttendance[0]: Getting all Attendance error");
            log.error("getAllAttendance[1]: " + e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.ofNullable(attBeanList);
    }

    @Override
    public Optional getAllGroups() {
        List<Group> groupBeanList = null;
        try {
            log.debug("getAllGroups[0]: Start getting all Group");
            groupBeanList = getAllBeans(Constants.GROUP_XML_SOURCE);
            groupBeanList.stream().forEach(bean -> log.info(bean));
            log.debug("getAllGroups[1]: Getting all Group complete");
        } catch (Exception e){
            log.error("getAllGroups[0]: Getting all Group error");
            log.error("getAllGroups[1]: " + e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.ofNullable(groupBeanList);
    }

    @Override
    public Optional getAllParents() {
        List<Parent> parentBeanList = null;
        try {
            log.debug("getAllParents[0]: Start getting all Parent");
            parentBeanList = getAllBeans(Constants.PARENT_XML_SOURCE);
            parentBeanList.stream().forEach(bean -> log.info(bean));
            log.debug("getAllParents[1]: Getting all Parent complete");
        } catch (Exception e){
            log.error("getAllParents[0]: Getting all Parent error");
            log.error("getAllParents[1]: " + e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.ofNullable(parentBeanList);
    }

    @Override
    public Optional getAllStudents() {
        List<Student> studentBeanList = null;
        try {
            log.debug("getAllStudents[0]: Start getting all Student");
            studentBeanList = getAllBeans(Constants.STUDENT_XML_SOURCE);
            studentBeanList.stream().forEach(bean -> log.info(bean));
            log.debug("getAllStudents[1]: Getting all Student complete");
        } catch (Exception e){
            log.error("getAllStudents[0]: Getting all Student error");
            log.error("getAllStudents[1]: " + e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.ofNullable(studentBeanList);
    }

    @Override
    public Optional getAllTeachers() {
        List<Teacher> teacherBeanList = null;
        try {
            log.debug("getAllTeachers[0]: Start getting all Teacher");
            teacherBeanList = getAllBeans(Constants.TEACHER_XML_SOURCE);
            teacherBeanList.stream().forEach(bean -> log.info(bean));
            log.debug("getAllTeachers[1]: Getting all Teacher complete");
        } catch (Exception e){
            log.error("getAllTeachers[0]: Getting all Teacher error");
            log.error("getAllTeachers[1]: " + e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.ofNullable(teacherBeanList);
    }

    @Override
    public Optional getAllAdmins() {
        List<Admin> adminBeanList = null;
        try {
            log.debug("getAllAdmins[0]: Start getting all Admin");
            adminBeanList = getAllBeans(Constants.ADMIN_XML_SOURCE);
            adminBeanList.stream().forEach(bean -> log.info(bean));
            log.debug("getAllAdmins[1]: Getting all Admin complete");
        } catch (Exception e){
            log.error("getAllAdmins[0]: Getting all Admin error");
            log.error("getAllAdmins[1]: " + e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.ofNullable(adminBeanList);
    }

    @Override
    public Optional getAllUsers(){
        List<User> userBeanList = null;
        try {
            log.debug("getAllUsers[0]: Start getting all User");
            userBeanList = getAllBeans(Constants.USER_XML_SOURCE);
            userBeanList.stream().forEach(bean -> log.info(bean));
            log.debug("getAllUsers[1]: Getting all User complete");
        } catch (Exception e){
            log.error("getAllUsers[0]: Getting all User error");
            log.error("getAllUsers[1]: " + e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.ofNullable(userBeanList);
    }

    @Override
    public boolean addAttendance(Attendance attendance) {
        try {
            if (attendance == null){
                throw new Exception("addAttendance[0]: Adding a null data");
            }
            log.debug("addAttendance[0]: Start adding attendance record");
            List<Attendance> attBeanList = getAllBeans(Constants.ATTENDANCE_XML_SOURCE);
            attBeanList.add(attendance);
            log.debug("addAttendance[1]: Adding attendance record complete");
            writeToMongo("addAttendance", attendance, saveFile(attBeanList));
        }
        catch(Exception e) {
            log.error("addAttendance[0]: Adding Attendance error");
            log.error("addAttendance[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean addGroup(Group group) {
        try {
            if (group == null){
                throw new Exception("addGroup[0]: Adding a null data");
            }
            log.debug("addGroup[0]: Start adding group record");
            List<Group> groupBeanList = getAllBeans(Constants.GROUP_XML_SOURCE);
            groupBeanList.add(group);
            log.debug("addGroup[1]: Adding group record complete");
            writeToMongo("addGroup", group, saveFile(groupBeanList));
        }
        catch(Exception e) {
            log.error("addGroup[0]: Adding Group error");
            log.error("addGroup[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean addParent(Parent parent) {
        try {
            if (parent == null){
                throw new Exception("addParent[0]: Adding a null data");
            }
            log.debug("addParent[0]: Start adding parent record");
            List<Parent> parentBeanList = getAllBeans(Constants.PARENT_XML_SOURCE);
            parentBeanList.add(parent);
            log.debug("addParent[1]: Adding parent record complete");
            writeToMongo("addParent", parent, saveFile(parentBeanList));
        }
        catch(Exception e) {
            log.error("addParent[0]: Adding Parent error");
            log.error("addParent[1]: "  + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean addStudent(Student student) {
        try {
            if (student == null){
                throw new Exception("addStudent[0]: Adding a null data");
            }
            log.debug("addStudent[0]: Start adding student record");
            List<Student> studentBeanList = getAllBeans(Constants.STUDENT_XML_SOURCE);
            studentBeanList.add(student);
            log.debug("addStudent[1]: Adding student record complete");
            writeToMongo("addStudent", student, saveFile(studentBeanList));
        }
        catch(Exception e) {
            log.error("addStudent[0]: Adding Student error");
            log.error("addStudent[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean addTeacher(Teacher teacher) {
        try {
            if (teacher == null){
                throw new Exception("addTeacher[0]: Adding a null data");
            }
            log.debug("addTeacher[0]: Start adding teacher record");
            List<Teacher> teacherBeanList = getAllBeans(Constants.TEACHER_XML_SOURCE);
            teacherBeanList.add(teacher);
            log.debug("addTeacher[1]: Adding Teacher complete");
            writeToMongo("addTeacher", teacher, saveFile(teacherBeanList));
        }
        catch(Exception e) {
            log.error("addTeacher[0]: Adding Teacher error");
            log.error("addTeacher[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean addAdmin(Admin admin) {
        try {
            if (admin == null){
                throw new Exception("addAdmin[0]: Adding a null data");
            }
            log.debug("addAdmin[0]: Start adding admin record");
            List<Admin> adminBeanList = getAllBeans(Constants.ADMIN_XML_SOURCE);
            adminBeanList.add(admin);
            log.debug("addAdmin[1]: Adding Admin complete");
            writeToMongo("addAdmin", admin, saveFile(adminBeanList));
        }
        catch(Exception e) {
            log.error("addAdmin[0]: Adding Admin error");
            log.error("addAdmin[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean addUser(User user){
        try {
            if (user == null){
                throw new Exception("addUser[0]: Adding a null data");
            }
            log.debug("addUser[0]: Start adding User record");
            List<User> userBeanList = getAllBeans(Constants.USER_XML_SOURCE);
            userBeanList.add(user);
            log.debug("addUser[1]: Adding User complete");
            writeToMongo("addUser", user, saveFile(userBeanList));
        }
        catch(Exception e) {
            log.error("addUser[0]: Adding User error");
            log.error("addUser[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteAttendance(long id) {
        try {
            log.debug("deleteAttendance[0]: Start deleting Attendance");
            List<Attendance> attendanceBeanList = getAllBeans(Constants.ATTENDANCE_XML_SOURCE);
            log.debug("deleteAttendance[1]: Searching required record");
            Predicate<Attendance> isDeletable = attendance -> attendance.getId() == id;
            log.debug("deleteAttendance[2]: Removing required record");
            Attendance attendanceBean = (Attendance) getAttendanceById(id).get();
            if (!attendanceBeanList.removeIf(isDeletable)) throw new Exception("Trying to delete non-existent record");
            log.debug("deleteAttendance[1]: Saving file after delete");
            writeToMongo("deleteAttendance", attendanceBean, saveFile(attendanceBeanList));
        }
        catch(Exception e) {
            log.error("deleteAttendance[0]: Deleting Attendance Error");
            log.error("deleteAttendance[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteGroup(long id) {
        try {
            log.debug("deleteGroup[0]: Start deleting Group");
            List<Group> groupBeanList = getAllBeans(Constants.GROUP_XML_SOURCE);
            log.debug("deleteGroup[1]: Searching required record");
            Predicate<Group> isDeletable = group -> group.getId() == id;
            log.debug("deleteGroup[2]: Removing required record");
            Group groupBean = (Group) getGroupById(id).get();
            if (!groupBeanList.removeIf(isDeletable)) throw new Exception("Trying to delete non-existent record");
            log.debug("deleteGroup[1]: Saving file after delete");
            writeToMongo("deleteGroup", groupBean, saveFile(groupBeanList));
        }
        catch(Exception e) {
            log.error("deleteGroup[0]: Deleting Group Error");
            log.error("deleteGroup[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteParent(long id) {
        try {
            log.debug("deleteParent[0]: Start deleting Parent");
            List<Parent> parentBeanList = getAllBeans(Constants.PARENT_XML_SOURCE);
            log.debug("deleteParent[1]: Searching Parent record");
            Predicate<Parent> isDeletable = parent -> parent.getId() == id;
            log.debug("deleteParent[2]: Removing Parent record");
            Parent parentBean = (Parent) getParentById(id).get();
            if (!parentBeanList.removeIf(isDeletable)) throw new Exception("Trying to delete non-existent record");
            log.debug("deleteParent[1]: Saving file after delete");
            writeToMongo("deleteParent", parentBean, saveFile(parentBeanList));
        }
        catch(Exception e) {
            log.error("deleteParent[0]: Deleting Parent Error");
            log.error("deleteParent[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteStudent(long id) {
        try {
            log.debug("deleteStudent[0]: Start deleting Student");
            List<Student> studentBeanList = getAllBeans(Constants.STUDENT_XML_SOURCE);
            log.debug("deleteStudent[1]: Searching Student record");
            Predicate<Student> isDeletable = student -> student.getId() == id;
            log.debug("deleteStudent[2]: Removing Student record");
            Student studentBean = (Student) getStudentById(id).get();
            if (!studentBeanList.removeIf(isDeletable)) throw new Exception("Trying to delete non-existent record");
            log.debug("deleteStudent[1]: Saving file after delete");
            writeToMongo("deleteStudent", studentBean, saveFile(studentBeanList));
        }
        catch(Exception e) {
            log.error("deleteStudent[0]: Deleting Student Error");
            log.error("deleteStudent[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteTeacher(long id) {
        try {
            log.debug("deleteTeacher[0]: Start deleting Teacher");
            List<Teacher> teacherBeanList = getAllBeans(Constants.TEACHER_XML_SOURCE);
            log.debug("deleteTeacher[1]: Searching Teacher record");
            Predicate<Teacher> isDeletable = teacher -> teacher.getId() == id;
            log.debug("deleteTeacher[2]: Removing Teacher record");
            Teacher teacherBean = (Teacher) getTeacherById(id).get();
            if (!teacherBeanList.removeIf(isDeletable)) throw new Exception("Trying to delete non-existent record");
            log.debug("deleteTeacher[1]: Saving file after delete");
            writeToMongo("deleteTeacher", teacherBean, saveFile(teacherBeanList));
        }
        catch(Exception e) {
            log.error("deleteTeacher[0]: Deleting Teacher Error");
            log.error("deleteTeacher[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteAdmin(long id) {
        try {
            log.debug("deleteAdmin[0]: Start deleting record");
            List<Admin> adminBeanList = getAllBeans(Constants.ADMIN_XML_SOURCE);
            log.debug("deleteAdmin[1]: Searching Admin record");
            Predicate<Admin> isDeletable = admin -> admin.getId() == id;
            log.debug("deleteAdmin[2]: Removing Admin record");
            Admin adminBean = (Admin) getAdminById(id).get();
            if (!adminBeanList.removeIf(isDeletable)) throw new Exception("Trying to delete non-existent record");
            log.debug("deleteAdmin[1]: Saving file after delete");
            writeToMongo("deleteAdmin", adminBean, saveFile(adminBeanList));
        }
        catch(Exception e) {
            log.error("deleteAdmin[0]: Deleting Admin Error");
            log.error("deleteAdmin[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteUser(long id){
        try {
            log.debug("deleteUser[0]: Start deleting User");
            List<User> userBeanList = getAllBeans(Constants.USER_XML_SOURCE);
            log.debug("deleteUser[1]: Searching User record");
            Predicate<User> isDeletable = user -> user.getId() == id;
            log.debug("deleteUser[2]: Removing User record");
            User userBean = (User) getUserById(id).get();
            if (!userBeanList.removeIf(isDeletable)) throw new Exception("Trying to delete non-existent record");
            log.debug("deleteUser[1]: Saving file after delete");
            writeToMongo("deleteUser", userBean, saveFile(userBeanList));
        }
        catch(Exception e) {
            log.error("deleteUser[0]: Deleting User Error");
            log.error("deleteUser[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateAttendance(long id, Attendance attendance) {
        try {
            log.debug("updateAttendance[0]: Start updating Attendance");
            List<Attendance> attBeanList = getAllBeans(Constants.ATTENDANCE_XML_SOURCE);
            log.debug("updateAttendance[1]: Searching Attendance by id");
            int index = attBeanList.indexOf(getAttendanceById(id).get());
            log.debug("updateAttendance[2]: Insert new values");
            attBeanList.set(index, attendance);
            writeToMongo("updateAttendance", attendance, saveFile(attBeanList));
            log.debug("updateAttendance[1]: Updating Attendance complete");
        }
        catch(Exception e) {
            log.error("updateAttendance[0]: Updating Attendance Error");
            log.error("updateAttendance[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateGroup(long id, Group group) {
        try {
            log.debug("updateGroup[0]: Start updating Group");
            List<Group> groupBeanList = getAllBeans(Constants.GROUP_XML_SOURCE);
            log.debug("updateGroup[1]: Searching Group by id");
            int index = groupBeanList.indexOf(getGroupById(id).get());
            log.debug("updateGroup[0]: Insert new values");
            groupBeanList.set(index, group);
            writeToMongo("updateGroup", group, saveFile(groupBeanList));
            log.debug("updateGroup[1]: Updating Group complete");
        }
        catch(Exception e) {
            log.error("updateGroup[0]: Updating Group Error");
            log.error("updateGroup[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateParent(long id, Parent parent) {
        try {
            log.debug("updateParent[0]: Start updating Parent ");
            List<Parent> parentBeanList = getAllBeans(Constants.PARENT_XML_SOURCE);
            log.debug("updateParent[1]: Searching Parent by id");
            int index = parentBeanList.indexOf(getParentById(id).get());
            log.debug("updateParent[2]: Insert new values");
            parentBeanList.set(index, parent);
            writeToMongo("updateParent", parent, saveFile(parentBeanList));
            log.debug("updateParent[1]: Updating Parent complete");
        }
        catch(Exception e) {
            log.error("updateParent[0]: Updating Parent Error");
            log.error("updateParent[0]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateStudent(long id, Student student) {
        try {
            log.debug("updateStudent[0]: Start updating Student");
            List<Student> studentBeanList = getAllBeans(Constants.STUDENT_XML_SOURCE);
            log.debug("updateStudent[1]: Searching Student by id");
            int index = studentBeanList.indexOf(getStudentById(id).get());
            log.debug("updateStudent[2]: Inserting new values");
            studentBeanList.set(index, student);
            writeToMongo("updateStudent", student, saveFile(studentBeanList));
            log.debug("updateStudent[1]: Updating Student complete");
        }
        catch(Exception e) {
            log.error("updateStudent[0]: Updating Student Error");
            log.error("updateStudent[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateTeacher(long id, Teacher teacher) {
        try {
            log.debug("updateTeacher[0]: Start updating Teacher");
            List<Teacher> teacherBeanList = getAllBeans(Constants.TEACHER_XML_SOURCE);
            log.debug("updateTeacher[1]: Searching Teacher by id");
            int index = teacherBeanList.indexOf(getTeacherById(id).get());
            log.debug("updateTeacher[2]: Inserting new values");
            teacherBeanList.set(index, teacher);
            writeToMongo("updateTeacher", teacher, saveFile(teacherBeanList));
            log.debug("updateTeacher[1]: Updating Teacher complete");
        }
        catch(Exception e) {
            log.error("updateTeacher[0]: Updating Teacher Error");
            log.error("updateTeacher[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateAdmin(long id, Admin admin) {
        try {
            log.debug("updateAdmin[0]: Start updating Admin");
            List<Admin> adminBeanList = getAllBeans(Constants.ADMIN_XML_SOURCE);
            log.debug("updateAdmin[1]: Searching Admin by id");
            int index = adminBeanList.indexOf(getAdminById(id).get());
            log.debug("updateAdmin[2]: Inserting new values");
            adminBeanList.set(index, admin);
            writeToMongo("updateAdmin", admin, saveFile(adminBeanList));
            log.debug("updateAdmin[1]: Updating Admin complete");
        }
        catch(Exception e) {
            log.error("updateAdmin[0]: Updating Admin Error");
            log.error("updateAdmin[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateUser(long id, User user){
        try {
            log.debug("updateUser[0]: Start updating User");
            List<User> userBeanList = getAllBeans(Constants.USER_XML_SOURCE);
            log.debug("updateUser[1]: Searching User record by Id");
            int index = userBeanList.indexOf(getUserById(id).get());
            log.debug("updateUser[2]: Inserting new values");
            userBeanList.set(index, user);
            writeToMongo("updateUser", user, saveFile(userBeanList));
            log.debug("updateUser[1]: Updating User complete");
        }
        catch(Exception e) {
            log.error("updateUser[0]: Updating User Error");
            log.error("updateUser[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public Optional getAttendanceById(long id) {
        Attendance attBean = new Attendance();
        try {
            log.debug("getAttendanceById[0]: Start getting Attendance by id");
            attBean = (Attendance) getAllBeans(Constants.ATTENDANCE_XML_SOURCE).stream()
                    .filter(bean-> ((Attendance) bean).getId() == id)
                    .findAny().get();
            log.debug("getAttendanceById[1]: Getting Attendance complete");
        }
        catch(Exception e) {
            log.error("getAttendanceById[0]: Getting Attendance by id Error ");
            log.error("getAttendanceById[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(attBean);
    }

    @Override
    public Optional getGroupById(long id) {
        Group groupBean = new Group();
        try {
            log.debug("getGroupById[0]: Start getting Group by id");
            groupBean = (Group) getAllBeans(Constants.GROUP_XML_SOURCE).stream()
                    .filter(bean-> ((Group) bean).getId() == id)
                    .findAny().get();
            log.debug("getGroupById[1]: Getting Group complete");
        }
        catch(Exception e) {
            log.error("getGroupById[0]: Getting Group by id Error ");
            log.error("getGroupById[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(groupBean);
    }

    @Override
    public Optional getParentById(long id) {
        Parent parentBean = new Parent();
        try {
            log.debug("getParentById[0]: Start getting Parent by id");
            parentBean = (Parent) getAllBeans(Constants.PARENT_XML_SOURCE).stream()
                    .filter(bean-> ((Parent) bean).getId() == id)
                    .findAny().get();
            log.debug("getParentById[1]: Getting Parent complete");
        }
        catch(Exception e) {
            log.error("getParentById[0]: Getting Parent by id Error ");
            log.error("getParentById[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(parentBean);
    }

    @Override
    public Optional getStudentById(long id) {
        Student studentBean = new Student();
        try {
            log.debug("getStudentById[0]: Start getting Student by id");
            studentBean = (Student) getAllBeans(Constants.STUDENT_XML_SOURCE).stream()
                    .filter(bean-> ((Student) bean).getId() == id)
                    .findAny().get();
            log.debug("getStudentById[1]: Receiving record complete");
        }
        catch(Exception e) {
            log.error("getStudentById[0]: Getting Student by id Error ");
            log.error("getStudentById[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(studentBean);
    }

    @Override
    public Optional getTeacherById(long id) {
        Teacher teacherBean = new Teacher();
        try {
            log.debug("getTeacherById[0]: Start getting Teacher by id");
            teacherBean = (Teacher) getAllBeans(Constants.TEACHER_XML_SOURCE).stream()
                    .filter(bean-> ((Teacher) bean).getId() == id)
                    .findAny().get();
            log.debug("getTeacherById[1]: Getting Teacher complete");
        }
        catch(Exception e) {
            log.error("getTeacherById[0]: Getting Teacher by id Error ");
            log.error("getTeacherById[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(teacherBean);
    }

    @Override
    public Optional getAdminById(long id) {
        Admin adminBean = new Admin();
        try {
            log.debug("getAdminById[0]: Start getting Admin by id");
            adminBean = (Admin) getAllBeans(Constants.ADMIN_XML_SOURCE).stream()
                    .filter(bean-> ((Admin) bean).getId() == id)
                    .findAny().get();
            log.debug("getAdminById[1]: Getting Admin complete");
        }
        catch(Exception e) {
            log.error("getAdminById[0]: Getting Admin by id Error ");
            log.error("getAdminById[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(adminBean);
    }

    @Override
    public Optional getUserById(long id){
        User userBean = new User();
        try {
            log.debug("getUserById[0]: Start getting User by id");
            userBean = (User) getAllBeans(Constants.USER_XML_SOURCE).stream()
                    .filter(bean-> ((User) bean).getId() == id)
                    .findAny().get();
            log.debug("getUserById[1]: Getting User complete");
        }
        catch(Exception e) {
            log.error("getUserById[0]: Getting User by id Error ");
            log.error("getUserById[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(userBean);
    }

    private <T> boolean saveFile(List<T> beans){
        try {
            log.debug("saveFile[0]: Start writing into file");
            Serializer serializer = new Persister();
            File result = new File(ConfigurationUtil.
                    getConfigurationEntry(findPath(beans)));
            Writer writer = new FileWriter(result);
            WrapperXML<T> xml = new WrapperXML<>(beans);
            serializer.write(xml, writer);
            log.debug("saveFile[1]: Writing into file complete");
        }
        catch (Exception e){
            log.error("saveFile[0]: XML writing file Error");
            log.error("saveFile[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }


    private <T> List<T> getAllBeans(String path) {
        List<T> loadedBeans = new ArrayList<>();
        try {
            log.debug("getAllBeans[0]: Start reading file");
            Serializer serializer = new Persister();
            FileReader file = new FileReader(getFile(path));

            WrapperXML<T> xml;
            xml = serializer.read(WrapperXML.class, file);
            loadedBeans = xml.getList();
            if(isNull(loadedBeans)){
                loadedBeans = new ArrayList<>();
            };
            file.close();
            log.debug("getAllBeans[1]: Reading file complete");
        }
        catch(Exception e){
            log.error("getAllBeans[0]: XML loading Error");
            log.error("getAllBeans[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return loadedBeans;
    }

    private File getFile(String path) throws IOException {
        log.debug("getFile[0]: Start getting file");
        File file = new File(ConfigurationUtil.getConfigurationEntry(path));
        Optional.of(!file.exists()).ifPresent(f->{
            try {
                log.debug("getFile[0]: Creating non-existent file");
                file.createNewFile();
                saveFile(new ArrayList<>());
            } catch (IOException e) {
                log.error("getFile[1]: Creating non-existent file error");
                e.printStackTrace();
            }
        });

        log.debug("getFile[1]: Getting file complete");
        return file;
    }

    private <T> boolean writeToMongo(String methodName, T bean, boolean isSuccessful) throws IOException {
        try{
            HistoryContent changedBean = new HistoryContent();
            changedBean.setActor("system");
            changedBean.setClassName(bean.getClass().getSimpleName());
            changedBean.setCreatedDate(LocalDateTime.now().toString());
            changedBean.setObject(bean);
            changedBean.setMethodName(methodName);
            changedBean.setIsSuccessful(isSuccessful);
            MongoChanges mngdb = new MongoChanges();
            mngdb.insertBeanIntoCollection(changedBean);
        } catch(Exception e){
            log.error("writeToMongo[0]: Writing to Mongo Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    private <T> String findPath(List<T> bean){
        switch(bean.get(0).getClass().getSimpleName()) {
            case "Attendance":
                return Constants.ATTENDANCE_XML_SOURCE;
            case "Group":
                return Constants.GROUP_XML_SOURCE;
            case "Parent":
                return Constants.PARENT_XML_SOURCE;
            case "Student":
                return Constants.STUDENT_XML_SOURCE;
            case "Teacher":
                return Constants.TEACHER_XML_SOURCE;
            case "Admin":
                return Constants.ADMIN_XML_SOURCE;
            case "User":
                return Constants.USER_XML_SOURCE;

            default: return Constants.UNKNOWN_SOURCE_XML;
        }
    }

    public boolean cleanAllFiles() {
        log.debug("cleanAllFile[0]: Starting cleaning all XML files");
        try{
            List<String> filesList = new ArrayList<>();
            filesList.add(ConfigurationUtil.getConfigurationEntry(Constants.GROUP_XML_SOURCE));
            filesList.add(ConfigurationUtil.getConfigurationEntry(Constants.STUDENT_XML_SOURCE));
            filesList.add(ConfigurationUtil.getConfigurationEntry(Constants.PARENT_XML_SOURCE));
            filesList.add(ConfigurationUtil.getConfigurationEntry(Constants.USER_XML_SOURCE));
            filesList.add(ConfigurationUtil.getConfigurationEntry(Constants.ADMIN_XML_SOURCE));
            filesList.add(ConfigurationUtil.getConfigurationEntry(Constants.TEACHER_XML_SOURCE));
            filesList.add(ConfigurationUtil.getConfigurationEntry(Constants.ATTENDANCE_XML_SOURCE));
            filesList.forEach(file ->{
                try {
                    FileWriter fileWriter = new FileWriter(file);
                    BufferedWriter bw = new BufferedWriter(fileWriter);
                    bw.write("<main/>");
                    bw.close();
                    log.info("cleanAllFiles[1]: File " + file + " cleaning completed" );
                } catch (Exception e) {
                    log.error("cleanAllFiles[1]: File " + file + " cleaning error");
                    log.error("cleaningAllFiles[2]: " + e.getMessage());
                }
            });
            log.debug("cleanAllFiles[1]: Cleaning of all XML files completed");
        }
        catch(Exception e){
            log.error("cleanAllFile[0]: Cleaning all XML files error");
            log.error("cleanAllFile[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }
}
