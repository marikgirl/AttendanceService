package ru.sfedu.AttendanceService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        serviceClient a = new serviceClient();

        a.logBasicSystemInfo();
    }
}
