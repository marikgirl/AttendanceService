package ru.sfedu.AttendanceService.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataProviderCSVTest extends DataProviderTest {
    private static Logger log = LogManager.getLogger(DataProviderCSVTest.class);

    DataProviderCSV dataProvider = new DataProviderCSV();
    @Override
    public void deleteRecords(){
        dataProvider.cleanAllFiles();
    }


    @Override
    public IDataProvider getDataProvider() {
        return new DataProviderCSV();
    }
}
