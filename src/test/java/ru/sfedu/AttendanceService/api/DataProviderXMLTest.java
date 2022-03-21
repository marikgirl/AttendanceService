package ru.sfedu.AttendanceService.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataProviderXMLTest extends DataProviderTest{

    private static Logger log = LogManager.getLogger(DataProviderXMLTest.class);

    DataProviderXML dataProvider = new DataProviderXML();
    @Override
    public void deleteRecords(){
        dataProvider.cleanAllFiles();
    }


    @Override
    public IDataProvider getDataProvider() {
        return new DataProviderXML();
    }
}
