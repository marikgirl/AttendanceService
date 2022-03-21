Сигнатура запуска команд:

java -Dlog4j.configurationFile="<file path>" -Dconfig.path="<file path>" -jar AttendanceService.jar <provider> <method> <params>

<provider>:
    csv
    xml
    h2

<method>:
    <-editGroupStudents>
    <-editGroup>
    <-editStudent>
    <-editParent>
    <-editDebt>
    <-setAttendance>
    <-getAllDebts>
    <-getAllGroups>

<params>:
    -editGroupStudents: <long groupId>, <long studentId>, <boolean isDelete>
    -editGroup: <long groupId>, <String groupName>, <boolean isDeleteGroup>
    -editStudent: <long studentId>, <String studentName>, <long parentId>, <int classNumber>, <String school>, <boolean isDeleteStudent>
    -editParent: <long parentId>, <String parentName>, boolean <isDeleteParent>
    -editDebt: <long parentId>, <int debtAmount>, <boolean isIncreasing>
    -setAttendance: <String dateString>, <String groupName>, <String studentName>, <boolean status>, <String absenceDetailsString>
    -getAllDebts: none
    -getAllGroups: none