package com.neofinance.quickdoc.common.utils;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.stereotype.Component;

import static com.mongodb.client.model.Filters.eq;

@Component
public class GridFsAssistant {

    @Value("${spring.data.mongodb.database}")
    private String database;

    private final MongoClient mongoClient;

    @Autowired
    public GridFsAssistant(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public MongoDatabase getMongoDB(String databaseName) {
        return mongoClient.getDatabase(databaseName);
    }

    public MongoDatabase getDefaultMongoDB() {
        return getMongoDB(database);
    }

    public GridFSDownloadStream getResource(ObjectId storedId) {
        GridFSBucket gridFSBucket = GridFSBuckets.create(getDefaultMongoDB());
        MongoCollection<Document> coll = getDefaultMongoDB().getCollection("fs.files");
        FindIterable<Document> file = coll.find(eq("_id", storedId));
        return file.first() != null ? gridFSBucket.openDownloadStream(storedId) : null;
    }
}
