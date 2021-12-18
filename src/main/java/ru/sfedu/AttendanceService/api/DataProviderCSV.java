package ru.sfedu.AttendanceService.api;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.AttendanceService.Constants;
import ru.sfedu.AttendanceService.Main;
import ru.sfedu.AttendanceService.MongoChanges;
import ru.sfedu.AttendanceService.model.beans.*;
import ru.sfedu.AttendanceService.utils.ConfigurationUtil;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class DataProviderCSV implements IDataProvider{

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
            student = loadBeanList(Constants.STUDENT_CSV_SOURCE, student).stream()
                    .filter(studentBean -> Objects.equals(((Student) studentBean).getName(), studentStringName))
                    .findAny().get();
            log.trace("Searching complete");
        } catch(Exception e){
            log.error("Student setting Error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.of(student.getId());
    }

    private Optional selectGroup(String groupStringName){
        Group group = new Group();
        try{
            log.debug("Start searching group record");
            group = loadBeanList(Constants.GROUP_CSV_SOURCE, group).stream()
                    .filter(groupBean -> Objects.equals(((Group) groupBean).getName(), groupStringName))
                    .findAny().get();
            log.trace("Searching complete");
        } catch(Exception e){
            log.error("Group setting Error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.of(group.getId());
    }

    private Optional setDate(String dateString){
        LocalDate date = null;
        try{
            date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            log.trace("Date parsing complete");
        } catch(Exception e){
            log.error("Parsing date error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.ofNullable(date);
    }

    @Override
    public boolean getAllAttendance() {
        try {
            log.debug("Start getting Attendance");
            Attendance attBean = new Attendance();
            List<Attendance> attBeanList = loadBeanList(Constants.ATTENDANCE_CSV_SOURCE, attBean);
            attBeanList.stream().forEach(bean -> log.info(bean));
            log.trace("Reading complete");
        } catch (Exception e){
            log.error("Reading attendance error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean getAllGroups() {
        try {
            log.debug("Start getting Groups");
            Group groupBean= new Group();
            List<Group> groupBeanList = loadBeanList(Constants.GROUP_CSV_SOURCE, groupBean);
            groupBeanList.stream().forEach(bean -> log.info(bean));
            log.trace("Reading complete");
        } catch (Exception e){
            log.error("Reading groups error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean getAllParents() {
        try {
            log.debug("Start getting Parents");
            Parent parentBean = new Parent();
            List<Parent> parentBeanList = loadBeanList(Constants.PARENT_CSV_SOURCE, parentBean);
            parentBeanList.stream().forEach(bean -> log.info(bean));
            log.trace("Reading complete");
        } catch (Exception e){
            log.error("Reading parent error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean getAllStudents() {
        try {
            log.debug("Start getting Students");
            Student studentBean = new Student();
            List<Student> studentBeanList = loadBeanList(Constants.STUDENT_CSV_SOURCE, studentBean);
            studentBeanList.stream().forEach(bean -> log.info(bean));
            log.trace("Reading complete");
        } catch (Exception e){
            log.error("Reading students error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean getAllTeachers() {
        try {
            log.debug("Start getting Teachers");
            Teacher teacherBean = new Teacher();
            List<Teacher> teacherBeanList = loadBeanList(Constants.TEACHER_CSV_SOURCE, teacherBean);
            teacherBeanList.stream().forEach(bean -> log.info(bean));
            log.trace("Reading complete");
        } catch (Exception e){
            log.error("Reading teachers error");
            log.error(e.getClass().getName() + ":" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean getAllAdmins() {
        try {
            log.debug("Start getting Admins");
            Admin adminBean = new Admin();
            List<Admin> adminBeanList = loadBeanList(Constants.ADMIN_CSV_SOURCE, adminBean);
            adminBeanList.stream().forEach(bean -> log.info(bean));
            log.trace("Reading complete");
        } catch (Exception e){
            log.error("Reading admins error");
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
            log.debug("Start adding attendance record");
            Attendance attBean = new Attendance();
            List<Attendance> attBeanList = loadBeanList(Constants.ATTENDANCE_CSV_SOURCE, attBean);
            attBeanList.add(attendance);
            log.trace("Adding attendance record complete");
            saveFile(attBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(attendance);
            writeToMongo("addAttendance", attendance);
        }
        catch(Exception e) {
            log.error("Adding attendance error");
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
            log.debug("Start adding group record");
            Group groupBean = new Group();
            List<Group> groupBeanList = loadBeanList(Constants.GROUP_CSV_SOURCE, groupBean);
            groupBeanList.add(group);
            log.trace("Adding group record complete");
            saveFile(groupBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(group);
        }
        catch(Exception e) {
            log.error("Adding group error");
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
            log.debug("Start adding parent record");
            Parent parentBean = new Parent();
            List<Parent> parentBeanList = loadBeanList(Constants.PARENT_CSV_SOURCE, parentBean);
            parentBeanList.add(parent);
            log.trace("Adding parent record complete");
            saveFile(parentBeanList);
//            MongoChanges mongo = new MongoChanges();
//            mongo.insertBeanIntoCollection(parent);
        }
        catch(Exception e) {
            log.error("Adding parent error");
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
            log.debug("Start adding student record");
            Student studentBean = new Student();
            List<Student> studentBeanList = loadBeanList(Constants.STUDENT_CSV_SOURCE, studentBean);
            studentBeanList.add(student);
            log.trace("Adding student record complete");
            saveFile(studentBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(student);
        }
        catch(Exception e) {
            log.error("Adding student error");
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
            log.debug("Start adding teacher record");
            Teacher teacherBean = new Teacher();
            List<Teacher> teacherBeanList = loadBeanList(Constants.TEACHER_CSV_SOURCE, teacherBean);
            teacherBeanList.add(teacher);
            log.trace("Adding teacher record complete");
            saveFile(teacherBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(teacher);
        }
        catch(Exception e) {
            log.error("Adding teacher error");
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
            log.debug("Start adding admin record");
            Admin adminBean = new Admin();
            List<Admin> adminBeanList = loadBeanList(Constants.ADMIN_CSV_SOURCE, adminBean);
            adminBeanList.add(admin);
            log.trace("Adding record complete");
            saveFile(adminBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(admin);
        }
        catch(Exception e) {
            log.error("Adding admin error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteAttendance(long id) {
        try {
            log.info("Start deleting record");
            Attendance attendanceBean = new Attendance();
            List<Attendance> attendanceBeanList = loadBeanList(Constants.ATTENDANCE_CSV_SOURCE, attendanceBean);
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
            log.info("Start deleting record");
            Group groupBean = new Group();
            List<Group> groupBeanList = loadBeanList(Constants.GROUP_CSV_SOURCE, groupBean);
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
            log.info("Start deleting record");
            Parent parentBean = new Parent();
            List<Parent> parentBeanList = loadBeanList(Constants.PARENT_CSV_SOURCE, parentBean);
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
            log.info("Start deleting record");
            Student studentBean = new Student();
            List<Student> studentBeanList = loadBeanList(Constants.STUDENT_CSV_SOURCE, studentBean);
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
            log.info("Start deleting record");
            Teacher teacherBean = new Teacher();
            List<Teacher> teacherBeanList = loadBeanList(Constants.TEACHER_CSV_SOURCE, teacherBean);
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
            log.info("Start deleting record");
            Admin adminBean = new Admin();
            List<Admin> adminBeanList = loadBeanList(Constants.ADMIN_CSV_SOURCE, adminBean);
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
            log.info("Start updating attendance record: reading file");
            Attendance attBean = new Attendance();
            List<Attendance> attBeanList = loadBeanList(Constants.ATTENDANCE_CSV_SOURCE, attBean);
            log.info("Searching required record: searching id");
            int index = attBeanList.indexOf(getAttendanceById(id).get());
            log.info("Insert new values");
            attBeanList.set(index, attendance);
            saveFile(attBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(getAttendanceById(id).get());
            log.info("Updating complete");
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
            log.info("Start updating group record: reading file");
            Group groupBean = new Group();
            List<Group> groupBeanList = loadBeanList(Constants.GROUP_CSV_SOURCE, groupBean);
            log.info("Searching required record: searching id");
            int index = groupBeanList.indexOf(getGroupById(id).get());
            log.info("Insert new values");
            groupBeanList.set(index, group);
            saveFile(groupBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(getGroupById(id).get());
            log.info("Updating complete");
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
            log.info("Start updating parent record: reading file");
            Parent parentBean = new Parent();
            List<Parent> parentBeanList = loadBeanList(Constants.PARENT_CSV_SOURCE, parentBean);
            log.info("Searching required record: searching id");
            int index = parentBeanList.indexOf(getParentById(id).get());
            log.debug("parent index = " + index);
            log.info("Insert new values");
            parentBeanList.set(index, parent);
            saveFile(parentBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(getParentById(id).get());
            log.info("Updating complete");
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
            log.info("Start updating student record: reading file");
            Student studentBean = new Student();
            List<Student> studentBeanList = loadBeanList(Constants.STUDENT_CSV_SOURCE, studentBean);
            log.info("Searching required record: searching id");
            int index = studentBeanList.indexOf(getStudentById(id).get());
            log.info("Insert new values");
            studentBeanList.set(index, student);
            saveFile(studentBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(getStudentById(id).get());
            log.info("Updating complete");
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
            log.info("Start updating teacher record: reading file");
            Teacher teacherBean = new Teacher();
            List<Teacher> teacherBeanList = loadBeanList(Constants.TEACHER_CSV_SOURCE, teacherBean);
            log.info("Searching required record: searching id");
            int index = teacherBeanList.indexOf(getTeacherById(id).get());
            log.info("Insert new values");
            teacherBeanList.set(index, teacher);
            saveFile(teacherBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(getTeacherById(id).get());
            log.info("Updating complete");
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
            log.info("Start updating admin record: reading file");
            Admin adminBean = new Admin();
            List<Admin> adminBeanList = loadBeanList(Constants.ADMIN_CSV_SOURCE, adminBean);
            log.info("Searching required record: searching id");
            int index = adminBeanList.indexOf(getAdminById(id).get());
            log.info("Insert new values");
            adminBeanList.set(index, admin);
            saveFile(adminBeanList);
//            MongoChanges mongoInsert = new MongoChanges();
//            mongoInsert.insertBeanIntoCollection(getAdminById(id).get());
            writeToMongo("updateAdmin", adminBean);
            log.info("Updating complete");
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
            log.info("Start receiving student record by id");
            attBean = loadBeanList(Constants.ATTENDANCE_CSV_SOURCE, attBean).stream()
                    .filter(bean-> ((Attendance) bean).getId() == id)
                    .findAny().get();
            log.info("Receiving record complete");
        }
        catch(Exception e) {
            log.error("Receiving attendance by id Error ");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(attBean);
    }

    @Override
    public Optional getGroupById(long id) {
        Group groupBean = new Group();
        try {
            log.info("Start receiving student record by id");
            groupBean = loadBeanList(Constants.GROUP_CSV_SOURCE, groupBean).stream()
                    .filter(bean-> ((Group) bean).getId() == id)
                    .findAny().get();
            log.info("Receiving record complete");
        }
        catch(Exception e) {
            log.error("Receiving group by id Error ");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(groupBean);
    }

    @Override
    public Optional getParentById(long id) {
        Parent parentBean = new Parent();
        try {
            log.info("Start receiving parent record by id");
            parentBean = loadBeanList(Constants.PARENT_CSV_SOURCE, parentBean).stream()
                    .filter(bean-> ((Parent) bean).getId() == id)
                    .findAny().get();
            log.info("Receiving record complete");
        }
        catch(Exception e) {
            log.error("Receiving parent by id Error ");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(parentBean);
    }

    @Override
    public Optional getStudentById(long id) {
        Student studentBean = new Student();
        try {
            log.info("Start receiving student record by id");
            studentBean = loadBeanList(Constants.STUDENT_CSV_SOURCE, studentBean).stream()
                    .filter(bean-> ((Student) bean).getId() == id)
                    .findAny().get();
            log.info("Receiving record complete");
        }
        catch(Exception e) {
            log.error("Receiving student by id Error ");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(studentBean);
    }

    @Override
    public Optional getTeacherById(long id) {
        Teacher teacherBean = new Teacher();
        try {
            log.info("Start receiving teacher record by id");
            teacherBean = loadBeanList(Constants.TEACHER_CSV_SOURCE, teacherBean).stream()
                    .filter(bean-> ((Teacher) bean).getId() == id)
                    .findAny().get();
            log.info("Receiving record complete");
        }
        catch(Exception e) {
            log.error("Receiving teacher by id Error ");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(teacherBean);
    }

    @Override
    public Optional getAdminById(long id) {
        Admin adminBean = new Admin();
        try {
            log.info("Start receiving admin record by id");
            adminBean = loadBeanList(Constants.ADMIN_CSV_SOURCE, adminBean).stream()
                    .filter(bean-> ((Admin) bean).getId() == id)
                    .findAny().get();
            log.info("Receiving record complete");
        }
        catch(Exception e) {
            log.error("Receiving admin by id Error ");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(adminBean);
    }

    private <T> boolean saveFile(List<T> beanList)  {
        try {
            log.info("Saving file: initializing file writer");
            log.info(findPath(beanList));
            FileWriter sw = new FileWriter(ConfigurationUtil.getConfigurationEntry(findPath(beanList)));
            log.info("Initializing CSVWriter");
            CSVWriter writer = new CSVWriter(sw);
            log.info("Initializing BeanToCsv object");
            StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(writer).build();
            log.info("Writing beanList into file");
            beanToCsv.write(beanList);
            log.info("Writing complete");
            writer.close();
        }
        catch(Exception e) {
            log.error("Saving file error ");
            log.info(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        log.info("File Saved");
        return true;
    }

    private <T> List<T> loadBeanList(String path, T bean) {
        List<T> loadedBeanList = null;
        try {
            log.info("Reading file");
            loadedBeanList = new CsvToBeanBuilder(new FileReader(ConfigurationUtil
                    .getConfigurationEntry(path)))
                    .withType(bean.getClass())
                    .build()
                    .parse();
            log.info("Beans loaded successfully");
        }
        catch(Exception e){
            log.error("Beans loading Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }

        return loadedBeanList;
    }

    private <T> boolean writeToMongo(String methodName, T bean) throws IOException {
        HistoryContent changedBean = new HistoryContent();
        changedBean.setActor("system");
        changedBean.setClassName(bean.getClass().getSimpleName());
        changedBean.setCreatedDate(LocalDateTime.now().toString());
        changedBean.setObject(bean);
        changedBean.setMethodName(methodName);
        MongoChanges mngdb = new MongoChanges();
        mngdb.insertBeanIntoCollection(changedBean);
        return true;
    }

    private  <T> String findPath(List<T> bean){
        switch(bean.get(0).getClass().getSimpleName()) {
            case "Attendance":
                return Constants.ATTENDANCE_CSV_SOURCE;
            case "Group":
                return Constants.GROUP_CSV_SOURCE;
            case "Parent":
                return Constants.PARENT_CSV_SOURCE;
            case "Student":
                return Constants.STUDENT_CSV_SOURCE;
            case "Teacher":
                return Constants.TEACHER_CSV_SOURCE;
            case "Admin":
                return Constants.ADMIN_CSV_SOURCE;

            default: return Constants.UNKNOWN_SOURCE_CSV;
        }
    }
}
