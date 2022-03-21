package ru.sfedu.AttendanceService.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataProviderH2Test extends DataProviderTest{
    private static Logger log = LogManager.getLogger(DataProviderH2Test.class);

    DataProviderH2 dataProvider = new DataProviderH2();
    @Override
    public void deleteRecords(){
        dataProvider.dropTableRecords();
    }


    @Override
    public IDataProvider getDataProvider() {
        return new DataProviderH2();
    }
}
