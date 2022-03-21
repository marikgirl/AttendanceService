package ru.sfedu.AttendanceService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import com.mongodb.MongoClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import ru.sfedu.AttendanceService.model.beans.HistoryContent;

import java.io.IOException;

import static ru.sfedu.AttendanceService.utils.ConfigurationUtil.getConfigurationEntry;


public class MongoChanges {

    private static final Logger log = LogManager.getLogger(Main.class);

    /**
     * Creates connection to MongoDB
     * @return <T>MongoClient mongoClient
     */
    public static <T> MongoClient getConnectionToDB() throws IOException {
        log.debug("getConnectionToDB[0]: Connecting to mongoDB");

        MongoClient mongoClient = new MongoClient(
                new ServerAddress(getConfigurationEntry(Constants.MONGODB_HOST),
                        Integer.parseInt(getConfigurationEntry(Constants.MONGODB_PORT))),
                MongoClientOptions.builder().serverSelectionTimeout(10).build());

        log.debug("getConnectionToDB[1]: Connected to mongoDB");
        return mongoClient;
    }

    /**
     * Makes String out of Bean with changes
     * @param obj object of HistoryContent with changes
     * @return String ObjectMapper as String
     */
    private static String beanToString(HistoryContent obj) throws JsonProcessingException {
        return new ObjectMapper().findAndRegisterModules().writeValueAsString(obj);
    }

    /**
     * Inserts HistoryContent object into MongoDB
     * @param bean object of HistoryContent with changes
     * @return boolean isInserted
     */
    public <T> boolean insertBeanIntoCollection(HistoryContent bean) throws IOException {
        log.debug("insertBeanIntoCollection[0]: Inserting bean into collection");

        getConnectionToDB().getDatabase(getConfigurationEntry(Constants.MONGODB_DATABASE)).getCollection(bean.getClass().getSimpleName())
                .insertOne(Document.parse(beanToString(bean)));
        log.debug("insertBeanIntoCollection[1]: Inserting bean into collection complete");
        return true;
    }

}
