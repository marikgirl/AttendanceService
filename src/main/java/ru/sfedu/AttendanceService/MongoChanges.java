package ru.sfedu.AttendanceService;

import com.mongodb.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static com.mongodb.MongoClientOptions.builder;

public class MongoChanges {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static <T> MongoClient getConnectionToDB() throws IOException {
        MongoClient mongoClient = new MongoClient(new ServerAddress(Constants.MG_HOST,
                Integer.parseInt(Constants.MG_PORT)),
                MongoClientOptions.builder().build());
        return mongoClient;
    }

    public <T> boolean insertBeanIntoCollection(T bean) throws IOException {
        DB database = getConnectionToDB().getDB(Constants.MG_DATABASE);
        DBCollection collection = database.getCollection(String.valueOf(bean.getClass()));
        collection.insert((DBObject) bean);
        return true;
    }

}
