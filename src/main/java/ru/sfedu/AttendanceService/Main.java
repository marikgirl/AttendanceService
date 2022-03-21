package ru.sfedu.AttendanceService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.AttendanceService.api.DataProviderCSV;
import ru.sfedu.AttendanceService.api.DataProviderH2;
import ru.sfedu.AttendanceService.api.DataProviderXML;
import ru.sfedu.AttendanceService.api.IDataProvider;
import ru.sfedu.AttendanceService.model.beans.*;

import static ru.sfedu.AttendanceService.Constants.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;


public class Main {
    private static final Logger log = LogManager.getLogger(Main.class);
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {

        IDataProvider dataProvider;
        try{
            if (args.length <= 0){
                dataProvider = getDP(args[0]);
                loadData(dataProvider);
                parseArguments(dataProvider, args);
            }
        } catch(Exception e){
            log.error("main[0]: PARSING ARGUMENTS ERROR. CHECK FLAGS!");
            log.error("main[1]: " + e.getClass().getName() + ":" + e.getMessage());
            log.error(e.getStackTrace());
        }
    }

    private static void loadData(IDataProvider dataProvider) {
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

        boolean checkExisting = dataProvider.getAdminById(admin1.getId()).isEmpty() & dataProvider.getUserById(admin1.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addAdmin(admin1);});
        checkExisting = dataProvider.getAdminById(admin2.getId()).isEmpty() & dataProvider.getUserById(admin2.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addAdmin(admin2);});
        checkExisting = dataProvider.getTeacherById(teacher1.getId()).isEmpty() & dataProvider.getUserById(teacher1.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addTeacher(teacher1);});
        checkExisting = dataProvider.getTeacherById(teacher2.getId()).isEmpty() & dataProvider.getUserById(teacher2.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addTeacher(teacher2);});

        checkExisting = dataProvider.getParentById(parent1.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addParent(parent1);});
        checkExisting = dataProvider.getParentById(parent2.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addParent(parent2);});
        checkExisting = dataProvider.getParentById(parent3.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addParent(parent3);});

        checkExisting = dataProvider.getStudentById(student1.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addStudent(student1);});
        checkExisting = dataProvider.getStudentById(student2.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addStudent(student2);});
        checkExisting = dataProvider.getStudentById(student3.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addStudent(student3);});
        checkExisting = dataProvider.getStudentById(student4.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addStudent(student4);});

        checkExisting = dataProvider.getGroupById(group1.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addGroup(group1);});
        checkExisting = dataProvider.getGroupById(group2.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addGroup(group2);});
        checkExisting = dataProvider.getGroupById(group3.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addGroup(group3);});

        checkExisting = dataProvider.getAttendanceById(att1.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addAttendance(att1);});
        checkExisting = dataProvider.getAttendanceById(att2.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addAttendance(att2);});
        checkExisting = dataProvider.getAttendanceById(att3.getId()).isEmpty();
        Optional.of(checkExisting).ifPresent(record->{dataProvider.addAttendance(att3);});
    }

    private static IDataProvider getDP(String arg) {
        IDataProvider dp;
        switch (arg){
            case PROVIDERCSV: dp = new DataProviderCSV();
            case PROVIDERXML: dp = new DataProviderXML();
            case PROVIDERH2: dp = new DataProviderH2();
            default: dp = null;
        }
        log.info("IDataProvider[0]: Current dataProvider: " + dp.getClass().getSimpleName());
        return dp;
    }

    private static boolean parseArguments(IDataProvider dataProvider, String[] args){
        try{
            log.debug(args[1]);
            switch (args[1].toLowerCase()){
                case EDIT_GROUP_STUDENTS:
                    log.info("Parsed parameters: groupId = " + args[2] + " studentId = " + args[3] + " isDelete = " + args[4]);
                    dataProvider.editGroupStudents(Long.parseLong(args[2]), Long.parseLong(args[3]), Boolean.parseBoolean(args[4]));
                case EDIT_GROUP:
                    log.info("Parsed parameters: groupId = " + args[2] + " groupName = " + args[3] + " isDeleteGroup = " + args[4]);
                    dataProvider.editGroup(Long.parseLong(args[2]), args[3], Boolean.parseBoolean(args[4]));
                case EDIT_STUDENT:
                    log.info("Parsed parameters: studentId = " + args[2] + " studentName = " + args[3] +
                            " parentId = " + args[4] + " classNumber = " + args[5] +
                            " school = " + args[6] + " isDeleteStudent = " + args[7]);
                    dataProvider.editStudent(Long.parseLong(args[2]), args[3], Long.parseLong(args[4]), Integer.parseInt(args[5]),
                            args[6], Boolean.parseBoolean(args[7]));
                case EDIT_PARENT:
                    log.info("Parsed parameters: parentId = " + args[2] + " parentName = " + args[3] + " isDeleteParent = " + args[4]);
                    dataProvider.editParent(Long.parseLong(args[2]), args[3], Boolean.parseBoolean(args[4]));
                case EDIT_DEBT:
                    log.info("Parsed parameters: parentId = " + args[2] + " debtAmount = " + args[3] + " isIncreasing = " + args[4]);
                    dataProvider.editDebt(Long.parseLong(args[2]), Integer.parseInt(args[3]), Boolean.parseBoolean(args[4]));
                case SET_ATTENDANCE:
                    log.info("Parsed parameters: date = " + args[2] + " groupName = " + args[3] +
                            " studentName = " + args[4] + " status = " + args[5] + " absenceDetailsString = " + args[6]);
                    dataProvider.setAttendance(args[2], args[3], args[4], Boolean.parseBoolean(args[5]), args[6]);
                case GET_ALL_DEBTS:
                    dataProvider.getAllDebts();
                case GET_ALL_GROUPS:
                    dataProvider.getAllGroups();
                default:
                    log.info("Incorrect command: " + args[1] + ". Check flag!");

            }

        } catch (Exception e){
            return false;
        }
        return true;
    }
}
