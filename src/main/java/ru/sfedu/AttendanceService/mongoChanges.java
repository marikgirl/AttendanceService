package ru.sfedu.AttendanceService;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class mongoChanges {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static <T> MongoClient getConnectionToDB(Class<T> tClass) throws IOException {
        //сделать константы для хоста и порта
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB database = mongoClient.getDB("myMongoDb");
        return mongoClient;
    }

}
