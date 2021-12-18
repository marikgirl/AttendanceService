package ru.sfedu.AttendanceService.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.AttendanceService.Constants;
import ru.sfedu.AttendanceService.Main;
import ru.sfedu.AttendanceService.model.beans.*;
import ru.sfedu.AttendanceService.utils.ConfigurationUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataProviderH2 implements IDataProvider{
    private static final Logger log = LogManager.getLogger(Main.class);

    public static final String ATTENDANCE_TABLE = "ATTENDANCE";
    public static final String GROUP_TABLE = "GROUP";
    public static final String PARENT_TABLE = "PARENT";
    public static final String STUDENT_TABLE = "STUDENT";
    public static final String TEACHER_TABLE = "TEACHER";
    public static final String ADMIN_TABLE = "ADMIN";
    public static final String STUDENT_BY_GROUP_TABLE = "STUDENT_BY_GROUP";
    public static final String GROUP_BY_TEACHER_TABLE = "GROUP_BY_TEACHER";

//    public boolean editGroupStudents(long groupId, long studentId, boolean isDelete) {
//        boolean isGroupStudentsEdited = true;
//        try{
//            log.debug("Start editing Group students");
//            Optional.of(isDelete).filter(p->p).ifPresent(p->deleteStudentFromGroup(groupId, studentId));
//            Optional.of(!isDelete).filter(p->p).ifPresent(p-> {
//                Group group = (Group) getGroupById(groupId).get();
//                List<Long> groupStudents = group.getStudentsId();
//                Optional.of(studentId).filter(i->i != 0).ifPresent(i->groupStudents.add(studentId));
//                group.setStudentsId(groupStudents);
//                updateGroup(groupId, group);
//            });
//            log.trace("Editing Group students complete");
//        } catch(Exception e){
//            log.error("Editing parent Error");
//            log.error(e.getClass().getName() + ":" + e.getMessage());
//            isGroupStudentsEdited = false;
//        }
//        return isGroupStudentsEdited;
//    }
//
//    private boolean deleteStudentFromGroup(long groupId, long studentId){
//        boolean isStudentDeleted = true;
//        try{
//            log.debug("Start deleting student from group");
//            Group group = (Group) getGroupById(groupId).get();
//            List<Long> groupStudents = group.getStudentsId();
//            Predicate<Long> isExisting = studentIndex -> studentIndex == groupStudents.indexOf(studentId);
//            groupStudents.removeIf(isExisting);
//            group.setStudentsId(groupStudents);
//            updateGroup(groupId, group);
//            log.trace("Deleting student from group complete");
//        } catch (Exception e){
//            log.error("Deleting student from group Error");
//            log.error(e.getClass().getName() + ":" + e.getMessage());
//            isStudentDeleted = false;
//        }
//        return isStudentDeleted;
//    }
//
//    public boolean editGroup(long groupId, String groupName, boolean isDeleteGroup){
//        boolean isGroupEdited = true;
//        try{
//            log.debug("Start editing Group");
//            Group group = (Group) getGroupById(groupId).get();
//            group.setName(groupName);
//            Optional.of(group.getId()).filter(p->p != 0 && !isDeleteGroup).ifPresent(p->updateGroup(groupId, group));
//            Optional.of(group.getId()).filter(p->p != 0 && isDeleteGroup).ifPresent(p->deleteGroup(groupId));
//            log.trace("Editing student complete");
//        } catch (Exception e){
//            log.error("Editing student Error");
//            log.error(e.getClass().getName() + ":" + e.getMessage());
//            isGroupEdited = false;
//        }
//        return isGroupEdited;
//    }
//
//    public boolean editStudent(long studentId, String studentName, long parentId, int classNumber, String school, boolean isDeleteStudent){
//        boolean isStudentEdited = true;
//        try{
//            log.debug("Start editing Student");
//            Student student = (Student) getStudentById(studentId).get();
//            student.setName(studentName);
//            student.setParentId(parentId);
//            student.setClassNumber(classNumber);
//            student.setSchool(school);
//            Optional.of(student.getId()).filter(p->p != 0 && !isDeleteStudent).ifPresent(p->updateStudent(studentId, student));
//            Optional.of(student.getId()).filter(p->p != 0 && isDeleteStudent).ifPresent(p->deleteStudent(studentId));
//            log.trace("Editing student complete");
//        } catch (Exception e){
//            log.error("Editing student Error");
//            log.error(e.getClass().getName() + ":" + e.getMessage());
//            isStudentEdited = false;
//        }
//        return isStudentEdited;
//    }
//
//    public boolean editParent(long parentId, String parentName, boolean isDeleteParent){
//        boolean isParentEdited = true;
//        try{
//            log.debug("Start editing Parent");
//            Parent parent = (Parent) getParentById(parentId).get();
//            parent.setName(parentName);
//            Optional.of(parent.getId()).filter(p->p != 0 && !isDeleteParent).ifPresent(p->updateParent(parentId, parent));
//            Optional.of(parent.getId()).filter(p->p != 0 && isDeleteParent).ifPresent(p->deleteParent(parentId));
//            log.trace("Editing parent complete");
//        } catch (Exception e){
//            log.error("Editing parent Error");
//            log.error(e.getClass().getName() + ":" + e.getMessage());
//            isParentEdited = false;
//        }
//        return isParentEdited;
//    }
//
//    public boolean editDebt(long parentId, int debtAmount, boolean isIncreasing){
//        boolean isDebtEdited = true;
//        try{
//            log.debug("Start editing parent debt");
//            Parent parent = (Parent) getParentById(parentId).get();
//            int parentDebt = parent.getDebt();
//            int finalDebtAmount = Math.abs(debtAmount);
//            Optional.of(isIncreasing).filter(p->p).ifPresent(p->parent.setDebt(parentDebt + finalDebtAmount));
//            Optional.of(!isIncreasing).filter(p->p).ifPresent(p->parent.setDebt(parentDebt - finalDebtAmount));
//            isDebtEdited = updateParent(parentId, parent);
//            log.trace("Editing parent debt completed");
//        } catch(Exception e){
//            log.error("Editing parent Error");
//            log.error(e.getClass().getName() + ":" + e.getMessage());
//            isDebtEdited = false;
//        }
//        return isDebtEdited;
//    }
//
//    public long setAttendance(String dateString, String groupName, String studentName, boolean status, String absenceDetailsString){
//        Attendance attBean = new Attendance();
//        try{
//            log.debug("Start setting new Attendance");
//            attBean.setDate((LocalDate) setDate(dateString).orElse(
//                    LocalDate.parse("01-01-1970", DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
//            attBean.setGroupById((long) selectGroup(groupName).get());
//            attBean.setStudentById((long) selectStudent(studentName).get());
//            attBean.setStatus(status);
//            attBean.setDetails((AbsenceDetails) getDetailFromString(absenceDetailsString).orElse(AbsenceDetails.NONE));
//            attBean.setId();
//            log.info(attBean.getId());
//            addAttendance(attBean);
//            log.trace("Setting Attendance complete");
//        } catch(Exception e){
//            log.error("Attendance setting Error");
//            log.error(e.getClass().getName() + ":" + e.getMessage());
//        }
//        return attBean.getId();
//    }
//
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
//
//    private Optional selectStudent(String studentStringName){
//        Student student = new Student();
//        try{
//            log.debug("Start searching student record");
//            student = (Student) readBeanList(Constants.STUDENT_XML_SOURCE).stream()
//                    .filter(studentBean -> Objects.equals(((Student) studentBean).getName(), studentStringName))
//                    .findAny().get();
//            log.trace("Searching complete");
//        } catch(Exception e){
//            log.error("Setting Student Error");
//            log.error(e.getClass().getName() + ":" + e.getMessage());
//        }
//        return Optional.of(student.getId());
//    }
//
//    private Optional selectGroup(String groupStringName){
//        Group group = new Group();
//        try{
//            log.debug("Start searching group record");
//            group = (Group) readBeanList(Constants.GROUP_XML_SOURCE).stream()
//                    .filter(groupBean -> Objects.equals(((Group) groupBean).getName(), groupStringName))
//                    .findAny().get();
//            log.trace("Searching complete");
//        } catch(Exception e){
//            log.error("Setting Group Error");
//            log.error(e.getClass().getName() + ":" + e.getMessage());
//        }
//        return Optional.of(group.getId());
//    }

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
        try{

        } catch(Exception e){
            log.error(" Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean getAllGroups() {
        try{

        } catch(Exception e){
            log.error(" Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean getAllParents() {
        try{

        } catch(Exception e){
            log.error(" Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean getAllStudents() {
        try{

        } catch(Exception e){
            log.error(" Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean getAllTeachers() {
        try{

        } catch(Exception e){
            log.error(" Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean getAllAdmins() {
        try{

        } catch(Exception e){
            log.error(" Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean addAttendance(Attendance attendance) {
        boolean addResult = false;
        try{
            log.info("Start adding Attendance record");
            String values = String.format("%d, '%s', %d, %d, %s, '%s'", attendance.getId(),
                    attendance.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), attendance.getGroupById(),
                    attendance.getStudentById(), String.valueOf(attendance.getStatus()), attendance.getDetails().name());
            addResult = insertRecord(ATTENDANCE_TABLE, values);
            log.info("Adding Attendance finished");
        } catch(Exception e){
            log.error("Adding Attendance Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return addResult;
    }

    @Override
    public boolean addGroup(Group group) {
        AtomicBoolean addResult = new AtomicBoolean(false);
        try{
            log.info("Start adding Group record");
            String values = String.format("%d, '%s'", group.getId(), group.getName());
            addResult.set(insertRecord(GROUP_TABLE, values));
            Optional.of(addResult.get()).filter(p->p).ifPresent(p->{
                group.getStudentsId().forEach(studentId -> {
                    String connectedValues = String.format("%d, %d", group.getId(), studentId);
                    addResult.compareAndSet(true, insertRecord(STUDENT_BY_GROUP_TABLE, connectedValues));
                });
            });
            log.info("Adding Group finished");
        } catch(Exception e){
            log.error("Adding Group Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return addResult.get();
    }

    @Override
    public boolean addParent(Parent parent) {
        boolean addResult = false;
        try{
            log.info("Start adding Parent record");
            String values = String.format("%d, '%s', %d", parent.getId(), parent.getName(), parent.getDebt());
            addResult = insertRecord(PARENT_TABLE, values);
            log.info("Adding Parent finished");
        } catch(Exception e){
            log.error("Adding Parent Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return addResult;
    }

    @Override
    public boolean addStudent(Student student) {
        boolean addResult = false;
        try{
            log.info("Start adding Student record");
            String values = String.format("%d, '%s', %d, %d, '%s'",
                    student.getId(), student.getName(), student.getParentId(),
                    student.getClassNumber(), student.getSchool());
            addResult = insertRecord(STUDENT_TABLE, values);
            log.info("Adding Student finished");
        } catch(Exception e){
            log.error("Adding Student Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return addResult;
    }

    @Override
    public boolean addTeacher(Teacher teacher) {
        AtomicBoolean addResult = new AtomicBoolean(false);
        try{
            log.info("Start adding Teacher record");
            String values = String.format("%d, '%s'", teacher.getId(), teacher.getName());
            addResult.set(insertRecord(TEACHER_TABLE, values));
            Optional.of(addResult.get()).filter(p->p).ifPresent(p->{
                teacher.getGroupsId().forEach(groupId -> {
                    String connectedValues = String.format("%d, %d", teacher.getId(), groupId);
                    addResult.compareAndSet(true, insertRecord(GROUP_BY_TEACHER_TABLE, connectedValues));
                });
            });
            log.info("Adding Teacher finished");
        } catch(Exception e){
            log.error("Adding Teacher Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return addResult.get();
    }

    @Override
    public boolean addAdmin(Admin admin) {
        boolean addResult = false;
        try{
            log.info("Start adding Admin record");
            String values = String.format("%d, '%s', '%s'", admin.getId(), admin.getName(), admin.getPosition());
            addResult = insertRecord(ADMIN_TABLE, values);
            log.info("Adding Admin finished");
        } catch(Exception e){
            log.error("Adding Admin Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return addResult;
    }

    @Override
    public boolean deleteAttendance(long id) {
        boolean deleteResult = false;
        try{
            log.info("Start deleting Attendance record");
            deleteResult = deleteRecord(ATTENDANCE_TABLE, "id", id);
            log.info("Deleting Attendance finished");
        } catch(Exception e){
            log.error("Deleting Attendance Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return deleteResult;
    }

    @Override
    public boolean deleteGroup(long id) {
        boolean deleteResult = false;
        try{
            log.info("Start deleting Group record");
            deleteResult = deleteRecord(GROUP_TABLE, "id", id) &&
                    deleteRecord(STUDENT_BY_GROUP_TABLE, "group", id);
            log.info("Deleting Group finished");
        } catch(Exception e){
            log.error("Deleting Group Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return deleteResult;
    }

    @Override
    public boolean deleteParent(long id) {
        boolean deleteResult = false;
        try{
            log.info("Start deleting Parent record");
            deleteResult = deleteRecord(PARENT_TABLE, "id", id);
            log.info("Deleting Parent finished");
        } catch(Exception e){
            log.error("Deleting Parent Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return deleteResult;
    }

    @Override
    public boolean deleteStudent(long id) {
        boolean deleteResult = false;
        try{
            log.info("Start deleting Student record");
            deleteResult = deleteRecord(STUDENT_TABLE, "id", id);
            log.info("Deleting Student finished");
        } catch(Exception e){
            log.error("Deleting Student Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return deleteResult;
    }

    @Override
    public boolean deleteTeacher(long id) {
        boolean deleteResult = false;
        try{
            log.info("Start deleting Teacher record");
            deleteResult = deleteRecord(TEACHER_TABLE, "id", id) &&
                    deleteRecord(GROUP_BY_TEACHER_TABLE, "teacher", id);
            log.info("Updating Teacher finished");
        } catch(Exception e){
            log.error("Deleting Teacher Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return deleteResult;
    }

    @Override
    public boolean deleteAdmin(long id) {
        boolean deleteResult = false;
        try{
            log.info("Start deleting Admin record");
            deleteResult = deleteRecord(ADMIN_TABLE, "id", id);
            log.info("Deleting Admin finished");
        } catch(Exception e){
            log.error("Deleting Admin Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return deleteResult;
    }

    @Override
    public boolean updateAttendance(long id, Attendance attendance) {
        boolean updResult = false;
        try{
            log.info("Start updating Attendance record");
            String params = String.format("date = '%s', group = %d, student = %d, status = %s, details = '%s'",
                    attendance.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), attendance.getGroupById(),
                    attendance.getStudentById(), String.valueOf(attendance.getStatus()), attendance.getDetails().name());
            updResult = updateRecord(ATTENDANCE_TABLE, params, id);
            log.info("Updating Attendance finished");
        } catch(Exception e){
            log.error("Updating Attendance Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return updResult;
    }

    @Override
    public boolean updateGroup(long id, Group group) {
        boolean updResult = false;
        try{
            log.info("Start updating Group record");
            String params = String.format("name = '%s'", group.getName());
            //ПРОДУМАТЬ, А ТО ХУЕТА


            updResult = updateRecord(GROUP_TABLE, params, id);
            log.info("Updating Group finished");
        } catch(Exception e){
            log.error("Updating Group Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return updResult;
    }

    @Override
    public boolean updateParent(long id, Parent parent) {
        boolean updResult = false;
        try{
            log.info("Start updating Parent record");
            String params = String.format("name = '%s', debt = %d", parent.getName(), parent.getDebt());
            updResult = updateRecord(PARENT_TABLE, params, id);
            log.info("Updating Parent finished");
        } catch(Exception e){
            log.error("Updating Parent Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return updResult;
    }

    @Override
    public boolean updateStudent(long id, Student student) {
        boolean updResult = false;
        try{
            log.info("Start updating Student record");
            String params = String.format("name = '%s', parent = %d, class = %d, school = '%s'",
                    student.getName(), student.getParentId(), student.getClassNumber(), student.getSchool());
            updResult = updateRecord(STUDENT_TABLE, params, id);
            log.info("Updating Student finished");
        } catch(Exception e){
            log.error("Updating Student Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return updResult;
    }

    @Override
    public boolean updateTeacher(long id, Teacher teacher) {
        boolean updResult = false;
        try{
            log.info("Start updating Teacher record");
            String params = String.format("name = '%s'", teacher.getName());


            // добавить обноление списка


            log.info("Updating Teacher finished");
        } catch(Exception e){
            log.error("Updating Teacher Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return updResult;
    }

    @Override
    public boolean updateAdmin(long id, Admin admin) {
        boolean updResult = false;
        try{
            log.info("Start updating Admin record");
            String params = String.format("name = '%s', position = '%s'", admin.getName(), admin.getPosition());
            updResult = updateRecord(ADMIN_TABLE, params, id);
            log.info("Updating Admin finished");
        } catch(Exception e){
            log.error("Updating Admin Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return updResult;
    }

    @Override
    public Optional getAttendanceById(long id) {
        Attendance attendance = null;
        try{
            log.info("Start getting Attendance record");
            String sql = String.format("SELECT * FROM ATTENDANCE WHERE ID = %d;", id);
            ResultSet rs = getResultSet(sql).get();
            attendance.setId(rs.getLong("id"));
            attendance.setDate((LocalDate) setDate(rs.getString("name")).get());
            attendance.setGroupById(rs.getLong("groupId"));
            attendance.setStudentById(rs.getLong("studentId"));
            attendance.setStatus(rs.getBoolean("status"));
            attendance.setDetails((AbsenceDetails) getDetailFromString(rs.getString("details")).get());
            rs.close();
            log.info("Getting Attendance record finished");
        } catch(Exception e){
            log.error("Getting Attendance Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(attendance);
    }

    @Override
    public Optional getGroupById(long id) {
        Group group = null;
        try{
            log.info("Start getting Group record");
            String sql = String.format("SELECT * FROM GROUP WHERE ID = %d;", id);
            ResultSet rs = getResultSet(sql).get();
            group.setId(rs.getLong("id"));
            group.setName(rs.getString("name"));
            //доработатьт извлечение из другой таблицы


            rs.close();
            log.info("Getting Group record finished");
        } catch(Exception e){
            log.error("Getting Group Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(group);
    }

    @Override
    public Optional getParentById(long id) {
        Parent parent = null;
        try{
            log.info("Start getting Parent record");
            String sql = String.format("SELECT * FROM PARENT WHERE ID = %d;", id);
            ResultSet rs = getResultSet(sql).get();
            parent.setId(rs.getLong("id"));
            parent.setName(rs.getString("name"));
            parent.setDebt(rs.getInt("position"));
            rs.close();
            log.info("Getting Parent record finished");
        } catch(Exception e){
            log.error("Getting Parent Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(parent);
    }

    @Override
    public Optional getStudentById(long id) {
        Student student = null;
        try{
            log.info("Start getting Student record");
            String sql = String.format("SELECT * FROM STUDENT WHERE ID = %d;", id);
            ResultSet rs = getResultSet(sql).get();
            student.setId(rs.getLong("id"));
            student.setName(rs.getString("name"));
            student.setParentId(rs.getLong("parent"));
            student.setClassNumber(rs.getInt("class"));
            student.setSchool(rs.getString("school"));
            rs.close();
            log.info("Getting Student record finished");
        } catch(Exception e){
            log.error("Getting Student Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(student);
    }

    @Override
    public Optional getTeacherById(long id) {
        Teacher teacher = null;
        try{
            log.info("Start getting Teacher record");
            String sql = String.format("SELECT * FROM TEACHER WHERE ID = %d;", id);
            ResultSet rs = getResultSet(sql).get();
            teacher.setId(rs.getLong("id"));
            teacher.setName(rs.getString("name"));
            //обработать лист из отдельной таблицы


            rs.close();
            log.info("Getting Teacher record finished");
        } catch(Exception e){
            log.error("Getting Teacher Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(teacher);
    }

    @Override
    public Optional getAdminById(long id) {
        Admin admin = null;
        try{
            log.info("Start getting Admin record");
            String sql = String.format("SELECT * FROM ADMIN WHERE ID = %d;", id);
            ResultSet rs = getResultSet(sql).get();
            admin.setId(rs.getLong("id"));
            admin.setName(rs.getString("name"));
            admin.setPosition(rs.getString("position"));
            rs.close();
            log.info("Getting Admin record finished");
        } catch(Exception e){
            log.error("Getting Admin Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(admin);
    }

    private Optional<Connection> getConnection(){
        Connection conn = null;
        try{
            Class.forName(ConfigurationUtil.getConfigurationEntry(Constants.H2_CONFIG_DRIVER));
            conn = DriverManager.getConnection(ConfigurationUtil.getConfigurationEntry(Constants.H2_CONFIG_URL),
                    ConfigurationUtil.getConfigurationEntry(Constants.H2_CONFIG_USER),
                    ConfigurationUtil.getConfigurationEntry(Constants.H2_CONFIG_PASSWORD));
        } catch (Exception e) {
            log.error("Getting Connection Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(conn);
    }

    private Optional<ResultSet> getResultSet(String sqlStatement){
        ResultSet resultSet = null;
        try{
            Connection connection = getConnection().get();
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlStatement);
            statement.close();
            connection.close();
        } catch (Exception e){
            log.error("Reading records Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return Optional.ofNullable(resultSet);
    }

    private boolean updateRecord(String tableName, String params, long id){
        try{
            log.info("Start updating record");
            Connection connection = getConnection().get();
            Statement statement = connection.createStatement();
            String sqlStmt = String.format("UPDATE %s SET %s WHERE id in (%d);", tableName, params, id);
            statement.executeUpdate(sqlStmt);
            statement.close();
            connection.close();
            log.info("Updating record finished");
        } catch (Exception e){
            log.error("Updating record Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean deleteRecord(String tableName, String columnName, long id){
        try {log.info("Start deleting record");
            Connection connection = getConnection().get();
            Statement statement = connection.createStatement();
            String sqlStmt = String.format("DELETE FROM %s WHERE %s in (%d);", tableName, columnName, id);
            statement.executeUpdate(sqlStmt);
            statement.close();
            connection.close();
            log.info("Deleting record finished");
        } catch (Exception e){
            log.error("Updating record Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean insertRecord(String tableName, String values){
        try {log.info("Start inserting record");
            Connection connection = getConnection().get();
            Statement statement = connection.createStatement();
            String sqlStmt = String.format("INSERT INTO %s VALUES (%s);", tableName, values);
            statement.executeUpdate(sqlStmt);
            statement.close();
            connection.close();
            log.info("Inserting record finished");
        } catch (Exception e){
            log.error("Inserting record Error");
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }
}
