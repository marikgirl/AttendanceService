package ru.sfedu.AttendanceService.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.AttendanceService.Main;
import ru.sfedu.AttendanceService.Constants;
import ru.sfedu.AttendanceService.model.beans.*;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static ru.sfedu.AttendanceService.utils.ConfigurationUtil.getConfigurationEntry;

public class DataProviderH2 implements IDataProvider{
    private static final Logger log = LogManager.getLogger(Main.class);

    public static final String ATTENDANCE_TABLE = "ATTENDANCE_TABLE";
    public static final String GROUP_TABLE = "GROUP_TABLE";
    public static final String PARENT_TABLE = "PARENT_TABLE";
    public static final String STUDENT_TABLE = "STUDENT_TABLE";
    public static final String TEACHER_TABLE = "TEACHER_TABLE";
    public static final String ADMIN_TABLE = "ADMIN_TABLE";
    public static final String USER_TABLE = "USER_TABLE";
    public static final String STUDENT_BY_GROUP_TABLE = "STUDENT_BY_GROUP";
    public static final String GROUP_BY_TEACHER_TABLE = "GROUP_BY_TEACHER";

    private static final String CREATE_STATEMENT = "CREATE TABLE IF NOT EXISTS %s (%s)";
    public static final String DROP_ALL_TABLE_RECORDS = "DROP TABLE IF EXISTS ATTENDANCE_TABLE, GROUP_TABLE, PARENT_TABLE," +
            " STUDENT_TABLE, TEACHER_TABLE, ADMIN_TABLE, USER_TABLE, STUDENT_BY_GROUP, GROUP_BY_TEACHER";
    private static final String ATTENDANCE_TABLE_PARAMS = "id BIGINT not NULL PRIMARY KEY, " +
            " date VARCHAR(255), " +
            " groupId BIGINT, " +
            " studentId BIGINT, " +
            " status BOOLEAN, " +
            " details VARCHAR(255)";
    public static final String GROUP_TABLE_PARAMS = "id BIGINT not NULL PRIMARY KEY, " +
            " name VARCHAR(255)";
    public static final String PARENT_TABLE_PARAMS = "id BIGINT not NULL PRIMARY KEY, " +
            " name VARCHAR(255), " +
            " debt INTEGER";
    public static final String STUDENT_TABLE_PARAMS = "id BIGINT not NULL PRIMARY KEY, " +
            " name VARCHAR(255), " +
            " parent BIGINT, " +
            " class INTEGER, " +
            " school VARCHAR(255)";
    public static final String TEACHER_TABLE_PARAMS = "id BIGINT not NULL PRIMARY KEY, " +
            " name VARCHAR(255)";
    public static final String ADMIN_TABLE_PARAMS = "id BIGINT not NULL PRIMARY KEY, " +
            " name VARCHAR(255), " +
            " position VARCHAR(255)";
    public static final String USER_TABLE_PARAMS = "id BIGINT not NULL PRIMARY KEY, " +
            " name VARCHAR(255)";
    public static final String STUDENT_BY_GROUP_TABLE_PARAMS = "id IDENTITY not NULL PRIMARY KEY, " +
            " groupId BIGINT not NULL, "  +
            " studentId BIGINT not NULL";
    public static final String GROUP_BY_TEACHER_TABLE_PARAMS = "id IDENTITY not NULL PRIMARY KEY, " +
            " teacherId BIGINT not NULL, " +
            " groupId BIGINT not NULL";

    public DataProviderH2() {
        try {
            log.debug("Creating tables in DB if not exist");
            Class.forName(getConfigurationEntry(Constants.H2_CONFIG_DRIVER));
            Connection connection = getConnection();
            connection.createStatement().executeUpdate(String.format(CREATE_STATEMENT, ATTENDANCE_TABLE, ATTENDANCE_TABLE_PARAMS));
            connection.createStatement().executeUpdate(String.format(CREATE_STATEMENT, GROUP_TABLE, GROUP_TABLE_PARAMS));
            connection.createStatement().executeUpdate(String.format(CREATE_STATEMENT, PARENT_TABLE, PARENT_TABLE_PARAMS));
            connection.createStatement().executeUpdate(String.format(CREATE_STATEMENT, STUDENT_TABLE, STUDENT_TABLE_PARAMS));
            connection.createStatement().executeUpdate(String.format(CREATE_STATEMENT, TEACHER_TABLE, TEACHER_TABLE_PARAMS));
            connection.createStatement().executeUpdate(String.format(CREATE_STATEMENT, ADMIN_TABLE, ADMIN_TABLE_PARAMS));
            connection.createStatement().executeUpdate(String.format(CREATE_STATEMENT, USER_TABLE, USER_TABLE_PARAMS));
            connection.createStatement().executeUpdate(String.format(CREATE_STATEMENT, STUDENT_BY_GROUP_TABLE, STUDENT_BY_GROUP_TABLE_PARAMS));
            connection.createStatement().executeUpdate(String.format(CREATE_STATEMENT, GROUP_BY_TEACHER_TABLE, GROUP_BY_TEACHER_TABLE_PARAMS));
        } catch (Exception e){
            log.error("Initializing tables Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @Override
    public boolean editGroupStudents(long groupId, long studentId, boolean isDelete) {
        boolean isGroupStudentsEdited = true;
        try{
            log.debug("editGroupStudents[0]: Start editing Group Students");
            Optional.of(isDelete).filter(p->p).ifPresent(p->deleteStudentFromGroup(groupId, studentId));
            Optional.of(!isDelete).filter(p->p).ifPresent(p-> {
                Group group = (Group) getGroupById(groupId).get();
                List<Long> groupStudents = group.getStudentsId();
                Optional.of(studentId).filter(i->i != 0).ifPresent(i->groupStudents.add(studentId));
                group.setStudentsId(groupStudents);
                updateGroup(groupId, group);
            });
            log.debug("editGroupStudents[1]: Editing Group Students complete");
        } catch(Exception e){
            log.error("editGroupStudents[0]: Editing Group Students Error");
            log.error("editGroupStudents[1]: " + e.getClass().getName() + ":" + e.getMessage());
            isGroupStudentsEdited = false;
        }
        return isGroupStudentsEdited;
    }

    @Override
    public boolean deleteStudentFromGroup(long groupId, long studentId){
        boolean isStudentDeleted = true;
        try{
            log.debug("deleteStudentFromGroup[0]: Start deleting Student from group");
            Group group = (Group) getGroupById(groupId).get();
            List<Long> groupStudents = group.getStudentsId();
            Predicate<Long> isExisting = studentIndex -> studentIndex == groupStudents.indexOf(studentId);
            groupStudents.removeIf(isExisting);
            group.setStudentsId(groupStudents);
            updateGroup(groupId, group);
            log.debug("deleteStudentFromGroup[1]: Deleting Student from group complete");
        } catch (Exception e){
            log.error("deleteStudentFromGroup[0]: Deleting Student from group Error");
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
            log.debug("editStudent[1]: Editing Student complete");
        } catch (Exception e){
            log.error("editStudent[0]: Editing Student Error");
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
            log.debug("editParent[1]: Editing Parent complete");
        } catch (Exception e){
            log.error("editParent[0]: Editing Parent Error");
            log.error("editParent[1]: " + e.getClass().getName() + ":" + e.getMessage());
            isParentEdited = false;
        }
        return isParentEdited;
    }

    @Override
    public boolean editDebt(long parentId, int debtAmount, boolean isIncreasing){
        boolean isDebtEdited = true;
        try{
            log.debug("editDebt[0]: Start editing Parent debt");
            Parent parent = (Parent) getParentById(parentId).get();
            int parentDebt = parent.getDebt();
            int finalDebtAmount = Math.abs(debtAmount);
            Optional.of(isIncreasing).filter(p->p).ifPresent(p->parent.setDebt(parentDebt + finalDebtAmount));
            Optional.of(!isIncreasing).filter(p->p).ifPresent(p->parent.setDebt(parentDebt - finalDebtAmount));
            isDebtEdited = updateParent(parentId, parent);
            log.debug("editDebt[1]: Editing Parent debt completed");
        } catch(Exception e){
            log.error("editDebt[0]: Editing Parent Error");
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
            log.debug("selectStudent[0]: Start select Student record");
            student = getAllStudents().get().stream()
                    .filter(studentBean -> Objects.equals((studentBean).getName(), studentStringName))
                    .findAny().get();
            log.trace("selectStudent[1]: Select Student complete");
        } catch(Exception e){
            log.error("selectStudent[0]: Select Student Error");
            log.error("selectStudent[1]: " + e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.of(student.getId());
    }

    @Override
    public Optional selectGroup(String groupStringName){
        Group group = new Group();
        try{
            log.debug("selectGroup[0]: Start select Group record");
            group = getAllGroups().get().stream()
                    .filter(groupBean -> Objects.equals((groupBean).getName(), groupStringName))
                    .findAny().get();
            log.trace("selectGroup[1]: Select Group complete");
        } catch(Exception e){
            log.error("selectGroup[0]: Select Group Error");
            log.error("selectGroup[1]: " + e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.of(group.getId());
    }

    @Override
    public Optional setDate(String dateString){
        LocalDate date = null;
        try{
            date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            log.trace("setDate[1]: Parsing Date complete");
        } catch(Exception e){
            log.error("setDate[0]: Parsing Date error");
            log.error("setDate[1]: " + e.getClass().getName() + ":" + e.getMessage());
        }
        return Optional.ofNullable(date);
    }

    @Override
    public Optional getAllDebts() {
        return getAllParents();
    }

    @Override
    public Optional<List<Attendance>> getAllAttendance() {
        List<Attendance> attendanceList = new ArrayList<Attendance>();
        try{
            log.debug("getAllAttendance[0]: Start getting all Attendance record");
            String sqlStmt = String.format("SELECT %s FROM %s %s", "*", ATTENDANCE_TABLE, "");
            ResultSet rs = getConnection().createStatement().executeQuery(sqlStmt);
            Attendance attendance = new Attendance();
            while(rs.next()){
                attendance.setId(rs.getLong("id"));
                attendance.setDate((LocalDate) setDate(rs.getString("date")).get());
                attendance.setGroupById(rs.getLong("groupId"));
                attendance.setStudentById(rs.getLong("studentId"));
                attendance.setStatus(rs.getBoolean("status"));
                attendance.setDetails((AbsenceDetails) getDetailFromString(rs.getString("details")).get());
                attendanceList.add(attendance);
            }
            log.debug("getAllAttendance[1]: Getting all Attendance complete");
        } catch(Exception e){
            log.error("getAllAttendance[0]: Getting all Attendance Error");
            log.error("getAllAttendance[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(attendanceList);
    }

    @Override
    public Optional<List<Group>> getAllGroups() {
        List<Group> groupList = new ArrayList<Group>();
        try{
            log.debug("getAllGroups[0]: Start getting all Group record");
            String sqlStmt = String.format("SELECT %s FROM %s %s", "*", GROUP_TABLE, "");
            ResultSet rs = getConnection().createStatement().executeQuery(sqlStmt);
            Group group = new Group();
            while(rs.next()){
                group.setId(rs.getLong("id"));
                group.setName(rs.getString("name"));
                group.setStudentsId((List<Long>) getConnectedId("studentId",
                        STUDENT_BY_GROUP_TABLE, group.getId()).get());
                groupList.add(group);
            }
            log.debug("getAllGroups[1]: Getting all Group complete");
        } catch(Exception e){
            log.error("getAllGroups[0]: Getting all Group Error");
            log.error("getAllGroups[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(groupList);
    }

    @Override
    public Optional<List<Parent>> getAllParents() {
        List<Parent> parentList = new ArrayList<Parent>();
        try{
            log.debug("getAllParents[0]: Start getting all Parent record");
            String sqlStmt = String.format("SELECT %s FROM %s %s", "*", PARENT_TABLE, "");
            ResultSet rs = getConnection().createStatement().executeQuery(sqlStmt);
            Parent parent = new Parent();
            while(rs.next()){
                parent.setId(rs.getLong(1));
                parent.setName(rs.getString(2));
                parent.setDebt(rs.getInt(3));
                parentList.add(parent);
            }
            log.debug("getAllParents[1]: Getting all Parent complete");
        } catch(Exception e){
            log.error("getAllParent[0]: Getting all Parent Error");
            log.error("getAllParent[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(parentList);
    }

    @Override
    public Optional<List<Student>> getAllStudents() {
        List<Student> studentList = new ArrayList<Student>();
        try{
            log.debug("getAllStudents[0]: Start getting all Student record");
            String sqlStmt = String.format("SELECT %s FROM %s %s", "*", STUDENT_TABLE, "");
            ResultSet rs = getConnection().createStatement().executeQuery(sqlStmt);
            Student student = new Student();
            while(rs.next()){
                student.setId(rs.getLong("id"));
                student.setName(rs.getString("name"));
                student.setParentId(rs.getLong("parent"));
                student.setClassNumber(rs.getInt("class"));
                student.setSchool(rs.getString("school"));
                studentList.add(student);
            }
            log.debug("getAllStudents[1]: Getting all Student complete");
        } catch(Exception e){
            log.error("getAllStudents[0]: Getting all Student Error");
            log.error("getAllStudents[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(studentList);
    }

    @Override
    public Optional<List<Teacher>> getAllTeachers() {
        List<Teacher> teacherList = new ArrayList<Teacher>();
        try{
            log.debug("getAllTeachers[0]: Start getting all Teacher record");
            String sqlStmt = String.format("SELECT %s FROM %s %s", "*", TEACHER_TABLE, "");
            ResultSet rs = getConnection().createStatement().executeQuery(sqlStmt);
            Teacher teacher = new Teacher();
            while(rs.next()){
                teacher.setId(rs.getLong("id"));
                teacher.setName(rs.getString("name"));
                teacher.setGroupsId((List<Long>) getConnectedId("groupId",
                        GROUP_BY_TEACHER_TABLE, teacher.getId()).get());
                teacherList.add(teacher);
            }
            log.debug("getAllTeachers[1]: Getting all Teacher complete");
        } catch(Exception e){
            log.error("getAllTeachers[0]: Getting all Teacher Error");
            log.error("getAllTeachers[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(teacherList);
    }

    @Override
    public Optional<List<Admin>> getAllAdmins() {
        List<Admin> adminList = new ArrayList<Admin>();
        try{
            log.debug("getAllAdmins[0]: Start getting all Admin record");
            String sqlStmt = String.format("SELECT %s FROM %s %s", "*", ADMIN_TABLE, "");
            ResultSet rs = getConnection().createStatement().executeQuery(sqlStmt);
            Admin admin = new Admin();
            while(rs.next()){
                admin.setId(rs.getLong("id"));
                admin.setName(rs.getString("name"));
                admin.setPosition(rs.getString("position"));
                adminList.add(admin);
            }
            log.debug("getAllAdmins[1]: Getting all Admin complete");
        } catch(Exception e){
            log.error("getAllAdmins[0]: Getting all Admin Error");
            log.error("getAllAdmins[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(adminList);
    }

    @Override
    public Optional<List<User>> getAllUsers() {
        List<User> userList = new ArrayList<User>();
        try{
            log.debug("getAllUsers[0]: Start getting all User record");
            String sqlStmt = String.format("SELECT %s FROM %s %s", "*", USER_TABLE, "");
            ResultSet rs = getConnection().createStatement().executeQuery(sqlStmt);
            User user = new User();
            while(rs.next()){
                user.setId(rs.getLong("id"));
                user.setName(rs.getString("name"));
                userList.add(user);
            }
            log.debug("getAllUsers[1]: Getting all User complete");
        } catch(Exception e){
            log.error("getAllUsers[0]: Getting all User Error");
            log.error("getAllUsers[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(userList);
    }

    @Override
    public boolean addAttendance(Attendance attendance) {
        boolean addResult = false;
        try{
            log.debug("addAttendance[0]: Start adding Attendance record");
            String values = String.format("%d, '%s', %d, %d, %s, '%s'", attendance.getId(),
                    attendance.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), attendance.getGroupById(),
                    attendance.getStudentById(), String.valueOf(attendance.getStatus()), attendance.getDetails().name());
            addResult = insertRecord(ATTENDANCE_TABLE, values);
            log.debug("addAttendance[1]: Adding Attendance complete");
        } catch(Exception e){
            log.error("addAttendance[0]: Adding Attendance Error");
            log.error("addAttendance[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return addResult;
    }

    @Override
    public boolean addGroup(Group group) {
        AtomicBoolean addResult = new AtomicBoolean(false);
        try{
            log.debug("addGroup[0]: Start adding Group record");
            String values = String.format("%d, '%s'", group.getId(), group.getName());
            addResult.set(insertRecord(GROUP_TABLE, values));
            Optional.of(addResult.get()).filter(p->p).ifPresent(p->{
                group.getStudentsId().forEach(studentId -> {
                    String connectedValues = String.format("%d, %d", group.getId(), studentId);
                    addResult.compareAndSet(true, insertRecord(STUDENT_BY_GROUP_TABLE, connectedValues));
                });
            });
            log.debug("addGroup[1]: Adding Group complete");
        } catch(Exception e){
            log.error("addGroup[0]: Adding Group Error");
            log.error("addGroup[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return addResult.get();
    }

    @Override
    public boolean addParent(Parent parent) {
        boolean addResult = false;
        try{
            log.debug("addParent[0]: Start adding Parent record");
            String values = String.format("%d, '%s', %d", parent.getId(), parent.getName(), parent.getDebt());
            addResult = insertRecord(PARENT_TABLE, values);
            log.debug("addParent[1]: Adding Parent complete");
        } catch(Exception e){
            log.error("addParent[0]: Adding Parent Error");
            log.error("addParent[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return addResult;
    }

    @Override
    public boolean addStudent(Student student) {
        boolean addResult = false;
        try{
            log.debug("addStudent[0]: Start adding Student record");
            String values = String.format("%d, '%s', %d, %d, '%s'",
                    student.getId(), student.getName(), student.getParentId(),
                    student.getClassNumber(), student.getSchool());
            addResult = insertRecord(STUDENT_TABLE, values);
            log.debug("addStudent[1]: Adding Student complete");
        } catch(Exception e){
            log.error("addStudent[0]: Adding Student Error");
            log.error("addStudent[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return addResult;
    }

    @Override
    public boolean addTeacher(Teacher teacher) {
        AtomicBoolean addResult = new AtomicBoolean(false);
        try{
            log.debug("addTeacher[0]: Start adding Teacher record");
            String values = String.format("%d, '%s'", teacher.getId(), teacher.getName());
            addResult.set(insertRecord(TEACHER_TABLE, values));
            Optional.of(addResult.get()).filter(p->p).ifPresent(p->{
                teacher.getGroupsId().forEach(groupId -> {
                    String connectedValues = String.format("%d, %d", teacher.getId(), groupId);
                    addResult.compareAndSet(true, insertRecord(GROUP_BY_TEACHER_TABLE, connectedValues));
                });
            });
            log.debug("addTeacher[1]: Adding Teacher complete");
        } catch(Exception e){
            log.error("addTeacher[0]: Adding Teacher Error");
            log.error("addTeacher[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return addResult.get();
    }

    @Override
    public boolean addAdmin(Admin admin) {
        boolean addResult = false;
        try{
            log.debug("addAdmin[0]: Start adding Admin record");
            String values = String.format("%d, '%s', '%s'", admin.getId(), admin.getName(), admin.getPosition());
            addResult = insertRecord(ADMIN_TABLE, values);
            log.debug("addAdmin[1]: Adding Admin complete");
        } catch(Exception e){
            log.error("addAdmin[0]: Adding Admin Error");
            log.error("addAdmin[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return addResult;
    }

    @Override
    public boolean addUser(User user) {
        boolean addResult = false;
        try{
            log.debug("addUser[0]: Start adding User record");
            String values = String.format("%d, '%s'", user.getId(), user.getName());
            addResult = insertRecord(USER_TABLE, values);
            log.debug("addUser[1]: Adding User complete");
        } catch(Exception e){
            log.error("addUser[0]: Adding User Error");
            log.error("addUser[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return addResult;
    }

    @Override
    public boolean deleteAttendance(long id) {
        boolean deleteResult = false;
        try{
            log.debug("deleteAttendance[0]: Start deleting Attendance record");
            deleteResult = deleteRecord(ATTENDANCE_TABLE, "id", id);
            log.debug("deleteAttendance[1]: Deleting Attendance complete");
        } catch(Exception e){
            log.error("deleteAttendance[0]: Deleting Attendance Error");
            log.error("deleteAttendance[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return deleteResult;
    }

    @Override
    public boolean deleteGroup(long id) {
        boolean deleteResult = false;
        try{
            log.debug("deleteGroup[0]: Start deleting Group record");
            deleteResult = deleteRecord(GROUP_TABLE, "id", id) &&
                    deleteRecord(STUDENT_BY_GROUP_TABLE, "groupId", id);
            log.debug("deleteGroup[1]: Deleting Group complete");
        } catch(Exception e){
            log.error("deleteGroup[0]: Deleting Group Error");
            log.error("deleteGroup[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return deleteResult;
    }

    @Override
    public boolean deleteParent(long id) {
        boolean deleteResult = false;
        try{
            log.debug("deleteParent[0]: Start deleting Parent record");
            deleteResult = deleteRecord(PARENT_TABLE, "id", id);
            log.debug("deleteParent[1]: Deleting Parent complete");
        } catch(Exception e){
            log.error("deleteParent[0]: Deleting Parent Error");
            log.error("deleteParent[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return deleteResult;
    }

    @Override
    public boolean deleteStudent(long id) {
        boolean deleteResult = false;
        try{
            log.debug("deleteStudent[0]: Start deleting Student record");
            deleteResult = deleteRecord(STUDENT_TABLE, "id", id);
            log.debug("deleteStudent[1]: Deleting Student complete");
        } catch(Exception e){
            log.error("deleteStudent[0]: Deleting Student Error");
            log.error("deleteStudent[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return deleteResult;
    }

    @Override
    public boolean deleteTeacher(long id) {
        boolean deleteResult = false;
        try{
            log.debug("deleteTeacher[0]: Start deleting Teacher record");
            deleteResult = deleteRecord(TEACHER_TABLE, "id", id) &&
                    deleteRecord(GROUP_BY_TEACHER_TABLE, "teacherId", id);
            log.debug("deleteTeacher[1]: Updating Teacher complete");
        } catch(Exception e){
            log.error("deleteTeacher[0]: Deleting Teacher Error");
            log.error("deleteTeacher[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return deleteResult;
    }

    @Override
    public boolean deleteAdmin(long id) {
        boolean deleteResult = false;
        try{
            log.debug("deleteAdmin[0]: Start deleting Admin record");
            deleteResult = deleteRecord(ADMIN_TABLE, "id", id);
            log.debug("deleteAdmin[1]: Deleting Admin complete");
        } catch(Exception e){
            log.error("deleteAdmin[0]: Deleting Admin Error");
            log.error("deleteAdmin[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return deleteResult;
    }

    @Override
    public boolean deleteUser(long id) {
        boolean deleteResult = false;
        try{
            log.debug("deleteUser[0]: Start deleting User record");
            deleteResult = deleteRecord(USER_TABLE, "id", id);
            log.debug("deleteUser[1]: Deleting User complete");
        } catch(Exception e){
            log.error("deleteUser[0]: Deleting User Error");
            log.error("deleteUser[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return deleteResult;
    }

    @Override
    public boolean updateAttendance(long id, Attendance attendance) {
        boolean updResult = false;
        try{
            log.debug("updateAttendance[0]: Start updating Attendance record");
            String params = String.format("date = '%s', groupId = %d, studentId = %d, status = %s, details = '%s'",
                    attendance.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), attendance.getGroupById(),
                    attendance.getStudentById(), String.valueOf(attendance.getStatus()), attendance.getDetails().name());
            updResult = updateRecord(ATTENDANCE_TABLE, params, id);
            log.debug("updateAttendance[1]: Updating Attendance complete");
        } catch(Exception e){
            log.error("updateAttendance[0]: Updating Attendance Error");
            log.error("updateAttendance[1]: " +e.getClass().getName() + ": " + e.getMessage());
        }
        return updResult;
    }

    @Override
    public boolean updateGroup(long id, Group group) {
        boolean updResult = false;
        try{
            log.debug("updateGroup[0]: Start updating Group record");
            String params = String.format("name = '%s'", group.getName());
            //
            updResult = updateRecord(GROUP_TABLE, params, id);
            log.debug("updateGroup[1]: Updating Group complete");
        } catch(Exception e){
            log.error("updateGroup[0]: Updating Group Error");
            log.error("updateGroup[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return updResult;
    }

    @Override
    public boolean updateParent(long id, Parent parent) {
        boolean updResult = false;
        try{
            log.debug("updateParent[0]: Start updating Parent record");
            String params = String.format("name = '%s', debt = %d", parent.getName(), parent.getDebt());
            updResult = updateRecord(PARENT_TABLE, params, id);
            log.debug("updateParent[1]: Updating Parent complete");
        } catch(Exception e){
            log.error("updateParent[0]: Updating Parent Error");
            log.error("updateParent[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return updResult;
    }

    @Override
    public boolean updateStudent(long id, Student student) {
        boolean updResult = false;
        try{
            log.debug("updateStudent[0]: Start updating Student record");
            String params = String.format("name = '%s', parent = %d, class = %d, school = '%s'",
                    student.getName(), student.getParentId(), student.getClassNumber(), student.getSchool());
            updResult = updateRecord(STUDENT_TABLE, params, id);
            log.debug("updateStudent[1]: Updating Student complete");
        } catch(Exception e){
            log.error("updateStudent[0]: Updating Student Error");
            log.error("updateStudent[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return updResult;
    }

    @Override
    public boolean updateTeacher(long id, Teacher teacher) {
        boolean updResult = false;
        try{
            log.debug("updateTeacher[0]: Start updating Teacher record");
            String params = String.format("name = '%s'", teacher.getName());
            //
            updResult = updateRecord(TEACHER_TABLE, params, id);

//            Optional.of(teacher.getGroupsId().equals());
            log.debug("updateTeacher[1]: Updating Teacher complete");
        } catch(Exception e){
            log.error("updateTeacher[0]: Updating Teacher Error");
            log.error("updateTeacher[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return updResult;
    }

    @Override
    public boolean updateAdmin(long id, Admin admin) {
        boolean updResult = false;
        try{
            log.debug("updateAdmin[0]: Start updating Admin record");
            String params = String.format("name = '%s', position = '%s'", admin.getName(), admin.getPosition());
            updResult = updateRecord(ADMIN_TABLE, params, id);
            log.debug("updateAdmin[1]: Updating Admin complete");
        } catch(Exception e){
            log.error("updateAdmin[0]: Updating Admin Error");
            log.error("updateAdmin[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return updResult;
    }

    @Override
    public boolean updateUser(long id, User user) {
        boolean updResult = false;
        try{
            log.debug("updateUser[0]: Start updating User record");
            String params = String.format("name = '%s'", user.getName());
            updResult = updateRecord(USER_TABLE, params, id);
            log.debug("updateUser[1]: Updating User complete");
        } catch(Exception e){
            log.error("updateUser[0]: Updating User Error");
            log.error("updateUser[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return updResult;
    }

    @Override
    public Optional getAttendanceById(long id) {
        Attendance attendance = new Attendance();
        try{
            log.debug("getAllAttendanceById[0]: Start getting Attendance record");
            String sqlStmt = String.format("SELECT %s FROM %s WHERE id in (%d)", "*", ATTENDANCE_TABLE, id);
            ResultSet rs = getConnection().createStatement().executeQuery(sqlStmt);
            rs.next();
            attendance.setId(rs.getLong("id"));
            attendance.setDate((LocalDate) setDate(rs.getString("name")).get());
            attendance.setGroupById(rs.getLong("groupId"));
            attendance.setStudentById(rs.getLong("studentId"));
            attendance.setStatus(rs.getBoolean("status"));
            attendance.setDetails((AbsenceDetails) getDetailFromString(rs.getString("details")).get());
            log.debug("getAllAttendanceById[1]: Getting Attendance record complete");
        } catch(Exception e){
            log.error("getAllAttendanceById[0]: Getting Attendance Error");
            log.error("getAllAttendanceById[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(attendance);
    }

    @Override
    public Optional getGroupById(long id) {
        Group group = new Group();
        try{
            log.debug("getGroupById[0]: Start getting Group record");
            String sqlStmt = String.format("SELECT %s FROM %s WHERE id in (%d)", "*", GROUP_TABLE, id);
            ResultSet rs = getConnection().createStatement().executeQuery(sqlStmt);
            rs.next();
            group.setId(rs.getLong("id"));
            group.setName(rs.getString("name"));
            group.setStudentsId((List<Long>) getConnectedId("studentId",
                    STUDENT_BY_GROUP_TABLE, group.getId()).get());
            log.debug("getGroupById[1]: Getting Group record complete");
        } catch(Exception e){
            log.error("getGroupById[0]: Getting Group Error");
            log.error("getGroupById[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(group);
    }

    @Override
    public Optional getParentById(long id) {
        Parent parent = new Parent();
        try{
            log.debug("getParentById[0]: Start getting Parent record");
            String sqlStmt = String.format("SELECT %s FROM %s WHERE id in (%d)", "*", PARENT_TABLE, id);
            ResultSet rs = getConnection().createStatement().executeQuery(sqlStmt);
            rs.next();
            parent.setId(rs.getLong("id"));
            parent.setName(rs.getString("name"));
            parent.setDebt(rs.getInt("debt"));
            log.debug("getParentById[1]: Getting Parent record complete");
        } catch(Exception e){
            log.error("getParentById[0]: Getting Parent Error");
            log.error("getParentById[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(parent);
    }

    @Override
    public Optional getStudentById(long id) {
        Student student = new Student();
        try{
            log.debug("getStudentById[0]: Start getting Student record");
            String sqlStmt = String.format("SELECT %s FROM %s WHERE id in (%d)", "*", STUDENT_TABLE, id);
            ResultSet rs = getConnection().createStatement().executeQuery(sqlStmt);
            rs.next();
            student.setId(rs.getLong("id"));
            student.setName(rs.getString("name"));
            student.setParentId(rs.getLong("parent"));
            student.setClassNumber(rs.getInt("class"));
            student.setSchool(rs.getString("school"));
            log.debug("getStudentById[1]: Getting Student record complete");
        } catch(Exception e){
            log.error("getStudentById[0]: Getting Student Error");
            log.error("getStudentById[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(student);
    }

    @Override
    public Optional getTeacherById(long id) {
        Teacher teacher = new Teacher();
        try{
            log.debug("getTeacherById[0]: Start getting Teacher record");
            String sqlStmt = String.format("SELECT %s FROM %s WHERE id in (%d)", "*", TEACHER_TABLE, id);
            ResultSet rs = getConnection().createStatement().executeQuery(sqlStmt);
            rs.next();
            teacher.setId(rs.getLong("id"));
            teacher.setName(rs.getString("name"));
            teacher.setGroupsId((List<Long>) getConnectedId("groupId",
                    GROUP_BY_TEACHER_TABLE, teacher.getId()).get());
            log.debug("getTeacherById[1]: Getting Teacher record complete");
        } catch(Exception e){
            log.error("getTeacherById[0]: Getting Teacher Error");
            log.error("getTeacherById[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(teacher);
    }

    @Override
    public Optional getAdminById(long id) {
        Admin admin = new Admin();
        try{
            log.debug("getAdminById[0]: Start getting Admin record");
            String sqlStmt = String.format("SELECT %s FROM %s WHERE id in (%d)", "*", ADMIN_TABLE, id);
            ResultSet rs = getConnection().createStatement().executeQuery(sqlStmt);
            rs.next();
            admin.setId(rs.getLong("id"));
            admin.setName(rs.getString("name"));
            admin.setPosition(rs.getString("position"));
            log.debug("getAdminById[1]: Getting Admin record complete");
        } catch(Exception e){
            log.error("getAdminById[0]: Getting Admin Error");
            log.error("getAdminById[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(admin);
    }

    @Override
    public Optional getUserById(long id) {
        User user = new User();
        try{
            log.debug("getUserById[0]: Start getting User record");
            String sqlStmt = String.format("SELECT %s FROM %s WHERE id in (%d)", "*", USER_TABLE, id);
            ResultSet rs = getConnection().createStatement().executeQuery(sqlStmt);
            rs.next();
            user.setId(rs.getLong("id"));
            user.setName(rs.getString("name"));
            log.debug("getUserById[1]: Getting User record complete");
        } catch(Exception e){
            log.error("getUserById[0]: Getting User Error");
            log.error("getUserById[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(user);
    }

    private Connection getConnection() throws IOException, SQLException {
        log.debug("getConnection[0]: Start getting connection to DB");
        Connection conn = DriverManager.getConnection(getConfigurationEntry(Constants.H2_CONFIG_URL),
                getConfigurationEntry(Constants.H2_CONFIG_USER),
                getConfigurationEntry(Constants.H2_CONFIG_PASSWORD));
        log.debug("getConnection[1]: Getting connection to DB complete");
        return conn;
    }


    private Optional getConnectedId(String selectedObj, String tableName, long id){
        List<Long> idList = new ArrayList<Long>();
        try {
            log.debug("getConnectedId[0]: Start getting id by List");
            String restriction = String.format("WHERE id in (%d)", id);
            String sqlStmt = String.format("SELECT %s FROM %s %s", selectedObj, tableName, restriction);
            ResultSet rs = getConnection().createStatement().executeQuery(sqlStmt);
            while (rs.next()){
                idList.add(rs.getLong(selectedObj));
            }
            log.debug("getConnectedId[1]: Getting id by List complete");
        } catch (Exception e){
            log.error("getConnectedId[0]: Getting id by List Error");
            log.error("getConnectedId[1]: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.of(idList);
    }

    private boolean updateRecord(String tableName, String params, long id){
        try{
            log.debug("updateRecord[0]: Start updating record");
            String sqlStmt = String.format("UPDATE %s SET %s WHERE id in (%d)", tableName, params, id);
            getConnection().createStatement().executeUpdate(sqlStmt);
            log.debug("updateRecord[1]: Updating record complete");
        } catch (Exception e){
            log.error("updateRecord[0]: Updating record Error");
            log.error("updateRecord[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean deleteRecord(String tableName, String columnName, long id){
        try {
            log.debug("deleteRecord[0]: Start deleting record");
            String sqlStmt = String.format("DELETE FROM %s WHERE %s in (%d)", tableName, columnName, id);
            getConnection().createStatement().executeUpdate(sqlStmt);
            log.debug("deleteRecord[1]: Deleting record complete");
        } catch (Exception e){
            log.error("deleteRecord[0]: Deleting record Error");
            log.error("deleteRecord[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean insertRecord(String tableName, String values){
        try {
            log.debug("insertRecord[0]: Start inserting record");
            String sqlStmt = String.format("INSERT INTO %s VALUES (%s)", tableName, values);
            getConnection().createStatement().executeUpdate(sqlStmt);
            log.debug("insertRecord[1]: Inserting record complete");
        } catch (Exception e){
            log.error("insertRecord[0]: Inserting record Error");
            log.error("insertRecord[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean dropTableRecords(){
        log.debug("dropTableRecords[0]: Start dropping records of all tables");
        try{
            Connection connection = getConnection();
            connection.createStatement().executeUpdate(DROP_ALL_TABLE_RECORDS);
            log.debug("dropTableRecords[0]: Dropping records of all tables complete");
        }
        catch(Exception e){
            log.error("dropTableRecords[0]: Dropping records of all tables Error");
            log.error("dropTableRecords[1]: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }
}
