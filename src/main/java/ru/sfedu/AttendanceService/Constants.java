package ru.sfedu.AttendanceService;

public class Constants {
    public static final String DEFAULT_CONFIG_PATH = "./src/main/resources/environment.properties";

    public static final String PROVIDERCSV = "csv";
    public static final String PROVIDERXML = "xml";
    public static final String PROVIDERH2 = "h2";

    public static final String H2_CONFIG_URL = "H2.URL";
    public static final String H2_CONFIG_USER = "H2.USER";
    public static final String H2_CONFIG_PASSWORD = "H2.PASSWORD";
    public static final String H2_CONFIG_DRIVER = "H2.DRIVER";

    public static final String MONGODB_HOST = "MONGODB.HOST";
    public static final String MONGODB_PORT = "MONGODB.PORT";
    public static final String MONGODB_DATABASE = "MONGODB.DATABASE";

    public static final String ATTENDANCE_CSV_SOURCE= "CSV.ATTENDANCE.SOURCE";
    public static final String ATTENDANCE_XML_SOURCE = "XML.ATTENDANCE.SOURCE";

    public static final String GROUP_CSV_SOURCE = "CSV.GROUP.SOURCE";
    public static final String GROUP_XML_SOURCE = "XML.GROUP.SOURCE";

    public static final String PARENT_CSV_SOURCE = "CSV.PARENT.SOURCE";
    public static final String PARENT_XML_SOURCE = "XML.PARENT.SOURCE";

    public static final String STUDENT_CSV_SOURCE = "CSV.STUDENT.SOURCE";
    public static final String STUDENT_XML_SOURCE = "XML.STUDENT.SOURCE";

    public static final String TEACHER_CSV_SOURCE = "CSV.TEACHER.SOURCE";
    public static final String TEACHER_XML_SOURCE = "XML.TEACHER.SOURCE";

    public static final String ADMIN_CSV_SOURCE = "CSV.ADMIN.SOURCE";
    public static final String ADMIN_XML_SOURCE = "XML.ADMIN.SOURCE";

    public static final String USER_CSV_SOURCE = "CSV.USER.SOURCE";
    public static final String USER_XML_SOURCE = "XML.USER.SOURCE";

    public static final String UNKNOWN_SOURCE_CSV = "CSV.UNKNOWN.SOURCE";
    public static final String UNKNOWN_SOURCE_XML = "XML.UNKNOWN.SOURCE";

    public static final String EDIT_GROUP_STUDENTS = "-editGroupStudents";
    public static final String EDIT_GROUP = "-editGroup";
    public static final String EDIT_STUDENT = "-editStudent";
    public static final String EDIT_PARENT = "-editParent";
    public static final String EDIT_DEBT = "-editDebt";
    public static final String SET_ATTENDANCE = "-setAttendance";
    public static final String GET_ALL_DEBTS = "-getAllDebts";
    public static final String GET_ALL_GROUPS = "-getAllGroups";
}
