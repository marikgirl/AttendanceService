package ru.sfedu.AttendanceService.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import ru.sfedu.AttendanceService.Constants;
import ru.sfedu.AttendanceService.Main;
import ru.sfedu.AttendanceService.MongoChanges;
import ru.sfedu.AttendanceService.model.beans.*;
import ru.sfedu.AttendanceService.utils.ConfigurationUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class DataProviderXML implements IDataProvider{
    private static final Logger log = LogManager.getLogger(Main.class);

    public boolean editGroupStudents(long groupId, long studentId, boolean isDelete) {
        boolean isGroupStudentsEdited = true;
        try{
            log.debug("Start editing Group students");
            Optional.of(isDelete).filter(p->p).ifPresent(p->deleteStudentFromGroup(groupId, studentId));
            Optional.of(!isDelete).filter(p->p).ifPresent(p-> {
                Group group = (Group) getGroupById(groupId).get();
                List<Long> groupStudents = group.getStudentsId();
                Optional.of(studentId).filter(i->i != 0).ifPresent(i->groupStudents.add(studentId));
                group.setStudentsId(groupStudents);
                updateGroup(groupId, group);
            });
            log.trace("Editing Group students complete");
        } catch(Exception e){
            log.error("Editing parent Error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            isGroupStudentsEdited = false;
        }
        return isGroupStudentsEdited;
    }

    private boolean deleteStudentFromGroup(long groupId, long studentId){
        boolean isStudentDeleted = true;
        try{
            log.debug("Start deleting student from group");
            Group group = (Group) getGroupById(groupId).get();
            List<Long> groupStudents = group.getStudentsId();
            Predicate<Long> isExisting = studentIndex -> studentIndex == groupStudents.indexOf(studentId);
            groupStudents.removeIf(isExisting);
            group.setStudentsId(groupStudents);
            updateGroup(groupId, group);
            log.trace("Deleting student from group complete");
        } catch (Exception e){
            log.error("Deleting student from group Error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            isStudentDeleted = false;
        }
        return isStudentDeleted;
    }

    public boolean editGroup(long groupId, String groupName, boolean isDeleteGroup){
        boolean isGroupEdited = true;
        try{
            log.debug("Start editing Group");
            Group group = (Group) getGroupById(groupId).get();
            group.setName(groupName);
            Optional.of(group.getId()).filter(p->p != 0 && !isDeleteGroup).ifPresent(p->updateGroup(groupId, group));
            Optional.of(group.getId()).filter(p->p != 0 && isDeleteGroup).ifPresent(p->deleteGroup(groupId));
            log.trace("Editing student complete");
        } catch (Exception e){
            log.error("Editing student Error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            isGroupEdited = false;
        }
        return isGroupEdited;
    }

    public boolean editStudent(long studentId, String studentName, long parentId, int classNumber, String school, boolean isDeleteStudent){
        boolean isStudentEdited = true;
        try{
            log.debug("Start editing Student");
            Student student = (Student) getStudentById(studentId).get();
            student.setName(studentName);
            student.setParentId(parentId);
            student.setClassNumber(classNumber);
            student.setSchool(school);
            Optional.of(student.getId()).filter(p->p != 0 && !isDeleteStudent).ifPresent(p->updateStudent(studentId, student));
            Optional.of(student.getId()).filter(p->p != 0 && isDeleteStudent).ifPresent(p->deleteStudent(studentId));
            log.trace("Editing student complete");
        } catch (Exception e){
            log.error("Editing student Error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            isStudentEdited = false;
        }
        return isStudentEdited;
    }

    public boolean editParent(long parentId, String parentName, boolean isDeleteParent){
        boolean isParentEdited = true;
        try{
            log.debug("Start editing Parent");
            Parent parent = (Parent) getParentById(parentId).get();
            parent.setName(parentName);
            Optional.of(parent.getId()).filter(p->p != 0 && !isDeleteParent).ifPresent(p->updateParent(parentId, parent));
            Optional.of(parent.getId()).filter(p->p != 0 && isDeleteParent).ifPresent(p->deleteParent(parentId));
            log.trace("Editing parent complete");
        } catch (Exception e){
            log.error("Editing parent Error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            isParentEdited = false;
        }
        return isParentEdited;
    }

    public boolean editDebt(long parentId, int debtAmount, boolean isIncreasing){
        boolean isDebtEdited = true;
        try{
            log.debug("Start editing parent debt");
            Parent parent = (Parent) getParentById(parentId).get();
            int parentDebt = parent.getDebt();
            int finalDebtAmount = Math.abs(debtAmount);
            Optional.of(isIncreasing).filter(p->p).ifPresent(p->parent.setDebt(parentDebt + finalDebtAmount));
            Optional.of(!isIncreasing).filter(p->p).ifPresent(p->parent.setDebt(parentDebt - finalDebtAmount));
            isDebtEdited = updateParent(parentId, parent);
            log.trace("Editing parent debt completed");
        } catch(Exception e){
            log.error("Editing parent Error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            isDebtEdited = false;
        }
        return isDebtEdited;
    }

    public long setAttendance(String dateString, String groupName, String studentName, boolean status, String absenceDetailsString){
        Attendance attBean = new Attendance();
        try{
            log.debug("Start setting new Attendance");
            attBean.setDate((LocalDate) setDate(dateString).orElse(
                    LocalDate.parse("01-01-1970", DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
            attBean.setGroupById((long) selectGroup(groupName).get());
            attBean.setStudentById((long) selectStudent(studentName).get());
            attBean.setStatus(status);
            attBean.setDetails((AbsenceDetails) getDetailFromString(absenceDetailsString).orElse(AbsenceDetails.NONE));
            attBean.setId();
            log.info(attBean.getId());
            addAttendance(attBean);
            log.trace("Setting Attendance complete");
        } catch(Exception e){
            log.error("Attendance setting Error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
        }
        return attBean.getId();
    }

    private Optional getDetailFromString(String enumString){
        AbsenceDetails enumDetails = null;
        try{
            log.debug("Transforming String to Enum");
            enumString = enumString.toUpperCase(Locale.ROOT);
            enumDetails = AbsenceDetails.valueOf(enumString);
        } catch(Exception e){
            log.error("Enum parsing from String error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.ofNullable(enumDetails);
    }

    private Optional selectStudent(String studentStringName){
        Student student = new Student();
        try{
            log.debug("Start searching student record");
            student = (Student) loadBeanList(Constants.STUDENT_XML_SOURCE).stream()
                    .filter(studentBean -> Objects.equals(((Student) studentBean).getName(), studentStringName))
                    .findAny().get();
            log.trace("Searching complete");
        } catch(Exception e){
            log.error("Setting Student Error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.of(student.getId());
    }

    private Optional selectGroup(String groupStringName){
        Group group = new Group();
        try{
            log.debug("Start searching group record");
            group = (Group) loadBeanList(Constants.GROUP_XML_SOURCE).stream()
                    .filter(groupBean -> Objects.equals(((Group) groupBean).getName(), groupStringName))
                    .findAny().get();
            log.trace("Searching complete");
        } catch(Exception e){
            log.error("Setting Group Error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.of(group.getId());
    }

    private Optional setDate(String dateString){
        LocalDate date = null;
        try{
            date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            log.trace("Parsing Date complete");
        } catch(Exception e){
            log.error("Parsing Date error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.ofNullable(date);
    }

    @Override
    public boolean getAllAttendance() {
        try {
            log.debug("Start getting all Attendance");
            List<Attendance> attBeanList = loadBeanList(Constants.ATTENDANCE_XML_SOURCE);
            attBeanList.stream().forEach(bean -> log.info(bean));
            log.trace("Getting complete");
        } catch (Exception e){
            log.error("Getting all Attendance error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean getAllGroups() {
        try {
            log.debug("Start getting all Group records");
            List<Group> groupBeanList = loadBeanList(Constants.GROUP_XML_SOURCE);
            groupBeanList.stream().forEach(bean -> log.info(bean));
            log.trace("Getting complete");
        } catch (Exception e){
            log.error("Getting all Group error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean getAllParents() {
        try {
            log.debug("Start getting all Parent records");
            List<Parent> parentBeanList = loadBeanList(Constants.PARENT_XML_SOURCE);
            parentBeanList.stream().forEach(bean -> log.info(bean));
            log.trace("Getting complete");
        } catch (Exception e){
            log.error("Getting all Parent error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean getAllStudents() {
        try {
            log.debug("Start getting Student records");
            List<Student> studentBeanList = loadBeanList(Constants.STUDENT_XML_SOURCE);
            studentBeanList.stream().forEach(bean -> log.info(bean));
            log.trace("Getting complete");
        } catch (Exception e){
            log.error("Getting all Student error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean getAllTeachers() {
        try {
            log.debug("Start getting Teacher records");
            List<Teacher> teacherBeanList = loadBeanList(Constants.TEACHER_XML_SOURCE);
            teacherBeanList.stream().forEach(bean -> log.info(bean));
            log.trace("Getting complete");
        } catch (Exception e){
            log.error("Getting all Teacher error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean getAllAdmins() {
        try {
            log.debug("Start getting Admin records");
            List<Admin> adminBeanList = loadBeanList(Constants.ADMIN_XML_SOURCE);
            adminBeanList.stream().forEach(bean -> log.info(bean));
            log.trace("Getting complete");
        } catch (Exception e){
            log.error("Getting all Admin error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean addAttendance(Attendance attendance) {
        try {
            if (attendance == null){
                throw new Exception("Adding a null data");
            }
            log.debug("Start adding Attendance record");
            List<Attendance> attBeanList = loadBeanList(Constants.ATTENDANCE_XML_SOURCE);
            attBeanList.add(attendance);
            log.trace("Adding record complete");
            saveFile(attBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(attendance);
        }
        catch(Exception e) {
            log.error("Adding Attendance error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean addGroup(Group group) {
        try {
            if (group == null){
                throw new Exception("Adding a null data");
            }
            log.debug("Start adding Group record");
            List<Group> groupBeanList = loadBeanList(Constants.GROUP_XML_SOURCE);
            groupBeanList.add(group);
            log.trace("Adding record complete");
            saveFile(groupBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(group);
        }
        catch(Exception e) {
            log.error("Adding Group error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean addParent(Parent parent) {
        try {
            if (parent == null){
                throw new Exception("Adding a null data");
            }
            log.debug("Start adding Parent record");
            List<Parent> parentBeanList = loadBeanList(Constants.PARENT_XML_SOURCE);
            parentBeanList.add(parent);
            log.trace("Adding record complete");
            saveFile(parentBeanList);
//            MongoChanges mongo = new MongoChanges();
//            mongo.insertBeanIntoCollection(parent);
        }
        catch(Exception e) {
            log.error("Adding Parent error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean addStudent(Student student) {
        try {
            if (student == null){
                throw new Exception("Adding a null data");
            }
            log.debug("Start adding Student record");
            List<Student> studentBeanList = loadBeanList(Constants.STUDENT_XML_SOURCE);
            studentBeanList.add(student);
            log.trace("Adding record complete");
            saveFile(studentBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(student);
        }
        catch(Exception e) {
            log.error("Adding Student error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean addTeacher(Teacher teacher) {
        try {
            if (teacher == null){
                throw new Exception("Adding a null data");
            }
            log.debug("Start adding Teacher record");
            List<Teacher> teacherBeanList = loadBeanList(Constants.TEACHER_XML_SOURCE);
            teacherBeanList.add(teacher);
            log.trace("Adding record complete");
            saveFile(teacherBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(teacher);
        }
        catch(Exception e) {
            log.error("Adding Teacher error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean addAdmin(Admin admin) {
        try {
            if (admin == null){
                throw new Exception("Adding a null data");
            }
            log.debug("Start adding Admin record");
            List<Admin> adminBeanList = loadBeanList(Constants.ADMIN_XML_SOURCE);
            adminBeanList.add(admin);
            log.trace("Adding record complete");
            saveFile(adminBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(admin);
        }
        catch(Exception e) {
            log.error("Adding Admin error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteAttendance(long id) {
        try {
            log.info("Start deleting Attendance record");
            List<Attendance> attendanceBeanList = loadBeanList(Constants.ATTENDANCE_XML_SOURCE);
            log.info("Searching required record");
            Predicate<Attendance> isDeletable = attendance -> attendance.getId() == id;
            log.info("Removing required record");
            if (!attendanceBeanList.removeIf(isDeletable)) throw new Exception("Trying to delete non-existent record");
            log.info("Saving file after delete");
            saveFile(attendanceBeanList);
        }
        catch(Exception e) {
            log.error("Deleting Attendance Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteGroup(long id) {
        try {
            log.info("Start deleting Group record");
            List<Group> groupBeanList = loadBeanList(Constants.GROUP_XML_SOURCE);
            log.info("Searching required record");
            Predicate<Group> isDeletable = group -> group.getId() == id;
            log.info("Removing required record");
            if (!groupBeanList.removeIf(isDeletable)) throw new Exception("Trying to delete non-existent record");
            log.info("Saving file after delete");
            saveFile(groupBeanList);
        }
        catch(Exception e) {
            log.error("Deleting Group Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteParent(long id) {
        try {
            log.info("Start deleting Parent record");
            List<Parent> parentBeanList = loadBeanList(Constants.PARENT_XML_SOURCE);
            log.info("Searching required record");
            Predicate<Parent> isDeletable = parent -> parent.getId() == id;
            log.info("Removing required record");
            if (!parentBeanList.removeIf(isDeletable)) throw new Exception("Trying to delete non-existent record");
            log.info("Saving file after delete");
            saveFile(parentBeanList);
        }
        catch(Exception e) {
            log.error("Deleting Parent Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteStudent(long id) {
        try {
            log.info("Start deleting Student record");
            List<Student> studentBeanList = loadBeanList(Constants.STUDENT_XML_SOURCE);
            log.info("Searching required record");
            Predicate<Student> isDeletable = student -> student.getId() == id;
            log.info("Removing required record");
            if (!studentBeanList.removeIf(isDeletable)) throw new Exception("Trying to delete non-existent record");
            log.info("Saving file after delete");
            saveFile(studentBeanList);
        }
        catch(Exception e) {
            log.error("Deleting Student Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteTeacher(long id) {
        try {
            log.info("Start deleting Teacher record");
            List<Teacher> teacherBeanList = loadBeanList(Constants.TEACHER_XML_SOURCE);
            log.info("Searching required record");
            Predicate<Teacher> isDeletable = teacher -> teacher.getId() == id;
            log.info("Removing required record");
            if (!teacherBeanList.removeIf(isDeletable)) throw new Exception("Trying to delete non-existent record");
            log.info("Saving file after delete");
            saveFile(teacherBeanList);
        }
        catch(Exception e) {
            log.error("Deleting Teacher Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteAdmin(long id) {
        try {
            log.info("Start deleting Admin record");
            List<Admin> adminBeanList = loadBeanList(Constants.ADMIN_XML_SOURCE);
            log.info("Searching required record");
            Predicate<Admin> isDeletable = admin -> admin.getId() == id;
            log.info("Removing required record");
            if (!adminBeanList.removeIf(isDeletable)) throw new Exception("Trying to delete non-existent record");
            log.info("Saving file after delete");
            saveFile(adminBeanList);
        }
        catch(Exception e) {
            log.error("Deleting Admin Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateAttendance(long id, Attendance attendance) {
        try {
            log.info("Start updating Attendance record");
            List<Attendance> attBeanList = loadBeanList(Constants.ATTENDANCE_XML_SOURCE);
            log.info("Searching required record");
            int index = attBeanList.indexOf(getAttendanceById(id).get());
            log.info("Insert new values");
            attBeanList.set(index, attendance);
            saveFile(attBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(getAttendanceById(id).get());
            log.info("Updating Attendance complete");
        }
        catch(Exception e) {
            log.error("Updating Attendance Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateGroup(long id, Group group) {
        try {
            log.info("Start updating Group record");
            List<Group> groupBeanList = loadBeanList(Constants.GROUP_XML_SOURCE);
            log.info("Searching required record");
            int index = groupBeanList.indexOf(getGroupById(id).get());
            log.info("Insert new values");
            groupBeanList.set(index, group);
            saveFile(groupBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(getGroupById(id).get());
            log.info("Updating Group complete");
        }
        catch(Exception e) {
            log.error("Updating Group Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateParent(long id, Parent parent) {
        try {
            log.info("Start updating Parent record");
            List<Parent> parentBeanList = loadBeanList(Constants.PARENT_XML_SOURCE);
            log.info("Searching required record");
            int index = parentBeanList.indexOf(getParentById(id).get());
            log.debug("parent index = " + index);
            log.info("Insert new values");
            parentBeanList.set(index, parent);
            saveFile(parentBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(getParentById(id).get());
            log.info("Updating Parent complete");
        }
        catch(Exception e) {
            log.error("Updating Parent Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateStudent(long id, Student student) {
        try {
            log.info("Start updating Student record");
            List<Student> studentBeanList = loadBeanList(Constants.STUDENT_XML_SOURCE);
            log.info("Searching required record");
            int index = studentBeanList.indexOf(getStudentById(id).get());
            log.info("Insert new values");
            studentBeanList.set(index, student);
            saveFile(studentBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(getStudentById(id).get());
            log.info("Updating Student complete");
        }
        catch(Exception e) {
            log.error("Updating Student Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateTeacher(long id, Teacher teacher) {
        try {
            log.debug("Start updating Teacher record");
            List<Teacher> teacherBeanList = loadBeanList(Constants.TEACHER_XML_SOURCE);
            log.debug("Searching required record");
            int index = teacherBeanList.indexOf(getTeacherById(id).get());
            log.debug("Inserting new values");
            teacherBeanList.set(index, teacher);
            saveFile(teacherBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(getTeacherById(id).get());
            log.trace("Updating Teacher complete");
        }
        catch(Exception e) {
            log.error("Updating Teacher Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateAdmin(long id, Admin admin) {
        try {
            log.debug("Start updating Admin record");
            List<Admin> adminBeanList = loadBeanList(Constants.ADMIN_XML_SOURCE);
            log.debug("Searching required record");
            int index = adminBeanList.indexOf(getAdminById(id).get());
            log.debug("Inserting new values");
            adminBeanList.set(index, admin);
            saveFile(adminBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(getAdminById(id).get());
            log.trace("Updating Admin complete");
        }
        catch(Exception e) {
            log.error("Updating Admin Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public Optional getAttendanceById(long id) {
        Attendance attBean = new Attendance();
        try {
            log.debug("Start receiving Attendance record");
            attBean = (Attendance) loadBeanList(Constants.ATTENDANCE_XML_SOURCE).stream()
                    .filter(bean-> ((Attendance) bean).getId() == id)
                    .findAny().get();
            log.trace("Receiving Attendance record complete");
        }
        catch(Exception e) {
            log.error("Receiving Attendance by id Error ");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(attBean);
    }

    @Override
    public Optional getGroupById(long id) {
        Group groupBean = new Group();
        try {
            log.debug("Start receiving Group record");
            groupBean = (Group) loadBeanList(Constants.GROUP_XML_SOURCE).stream()
                    .filter(bean-> ((Group) bean).getId() == id)
                    .findAny().get();
            log.trace("Receiving Group record complete");
        }
        catch(Exception e) {
            log.error("Receiving Group by id Error ");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(groupBean);
    }

    @Override
    public Optional getParentById(long id) {
        Parent parentBean = new Parent();
        try {
            log.debug("Start receiving Parent record");
            parentBean = (Parent) loadBeanList(Constants.PARENT_XML_SOURCE).stream()
                    .filter(bean-> ((Parent) bean).getId() == id)
                    .findAny().get();
            log.trace("Receiving Parent record complete");
        }
        catch(Exception e) {
            log.error("Receiving Parent by id Error ");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(parentBean);
    }

    @Override
    public Optional getStudentById(long id) {
        Student studentBean = new Student();
        try {
            log.debug("Start receiving Student record");
            studentBean = (Student) loadBeanList(Constants.STUDENT_XML_SOURCE).stream()
                    .filter(bean-> ((Student) bean).getId() == id)
                    .findAny().get();
            log.trace("Receiving Student record complete");
        }
        catch(Exception e) {
            log.error("Receiving Student by id Error ");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(studentBean);
    }

    @Override
    public Optional getTeacherById(long id) {
        Teacher teacherBean = new Teacher();
        try {
            log.debug("Start receiving Teacher record");
            teacherBean = (Teacher) loadBeanList(Constants.TEACHER_XML_SOURCE).stream()
                    .filter(bean-> ((Teacher) bean).getId() == id)
                    .findAny().get();
            log.trace("Receiving Teacher record complete");
        }
        catch(Exception e) {
            log.error("Receiving Teacher by id Error ");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(teacherBean);
    }

    @Override
    public Optional getAdminById(long id) {
        Admin adminBean = new Admin();
        try {
            log.debug("Start receiving Admin record");
            adminBean = (Admin) loadBeanList(Constants.ADMIN_XML_SOURCE).stream()
                    .filter(bean-> ((Admin) bean).getId() == id)
                    .findAny().get();
            log.trace("Receiving Admin record complete");
        }
        catch(Exception e) {
            log.error("Receiving Admin by id Error ");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(adminBean);
    }

    private <T> boolean saveFile(List<T> beans){
        try {
            log.debug("Start writing into file");
            Serializer serializer = new Persister();
            File result = new File(ConfigurationUtil.
                    getConfigurationEntry(findPath(beans)));
            Writer writer = new FileWriter(result);
            WrapperXML<T> xml = new WrapperXML<>(beans);
            serializer.write(xml, writer);
        }
        catch (Exception e){
            log.error("XML writing file Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }


    private <T> List<T> loadBeanList(String path) {
        List<T> loadedBeans = null;
        try {
            log.debug("Start reading file");
            Serializer serializer = new Persister();
            FileReader file = new FileReader(ConfigurationUtil.getConfigurationEntry(path));

            WrapperXML<T> xml;
            xml = serializer.read(WrapperXML.class, file);
            loadedBeans = xml.getList();
            log.trace("Reading file complete");
            file.close();
        }
        catch(Exception e){
            log.error("XML loading Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return loadedBeans;
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

            default: return Constants.UNKNOWN_SOURCE_XML;
        }
    }
}
