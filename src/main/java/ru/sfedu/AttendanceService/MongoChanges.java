package ru.sfedu.AttendanceService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.MongoClientSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import ru.sfedu.AttendanceService.model.beans.HistoryContent;
import ru.sfedu.AttendanceService.utils.ConfigurationUtil;

import java.io.IOException;

import static com.mongodb.MongoClientOptions.builder;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


public class MongoChanges {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static <T> MongoClient getConnectionToDB() throws IOException {
        log.trace("Connecting to mongoDB");

        ConnectionString connectionString = new ConnectionString("mongodb+srv://marik_girl:Wx28he3QNFVkqpG@cluster0.b2beu.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);

        return mongoClient;
    }

    private static String beanToString(HistoryContent obj) throws JsonProcessingException {
        return new ObjectMapper().findAndRegisterModules().writeValueAsString(obj);
    }

    public <T> boolean insertBeanIntoCollection(HistoryContent bean) throws IOException {
        log.trace("Inserting bean into collection");

        getConnectionToDB().getDatabase(ConfigurationUtil
                .getConfigurationEntry(Constants.MONGODB_DATABASE)).getCollection(bean.getClass().getSimpleName())
                .insertOne(Document.parse(beanToString(bean)));

        return true;
    }

}
