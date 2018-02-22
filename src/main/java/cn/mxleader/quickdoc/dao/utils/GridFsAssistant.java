package cn.mxleader.quickdoc.dao.utils;

import cn.mxleader.quickdoc.entities.FileMetadata;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.QueryMapper;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mongodb.assertions.Assertions.notNull;
import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.gridfs.GridFsCriteria.whereFilename;

@Component
public class GridFsAssistant implements GridFsOperations, ResourcePatternResolver {

    static final String CONTENT_TYPE_FIELD = "_contentType";

    private final MongoDbFactory dbFactory;

    private final String bucketName;
    private final MongoConverter converter;
    private final QueryMapper queryMapper;
    private final MongoCollection<GridFSFile> filesCollection;

    @Autowired
    public GridFsAssistant(MongoDbFactory dbFactory, MongoConverter converter) {
        this(dbFactory, converter, null);
    }

    public GridFsAssistant(MongoDbFactory dbFactory, MongoConverter converter, String bucketName) {
        Assert.notNull(dbFactory, "MongoDbFactory must not be null!");
        Assert.notNull(converter, "MongoConverter must not be null!");

        this.dbFactory = dbFactory;
        this.converter = converter;
        this.queryMapper = new QueryMapper(converter);
        this.bucketName = bucketName == null ? "fs" : bucketName;
        this.filesCollection = getFilesCollection(notNull("database", dbFactory.getDb()), this.bucketName);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, java.lang.String)
     */
    public ObjectId store(InputStream content, String filename) {
        return store(content, filename, (Object) null);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, java.lang.Object)
     */
    @Override
    public ObjectId store(InputStream content, @Nullable Object metadata) {
        return store(content, null, metadata);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, com.mongodb.Document)
     */
    @Override
    public ObjectId store(InputStream content, @Nullable Document metadata) {
        return store(content, null, metadata);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, java.lang.String, java.lang.String)
     */
    public ObjectId store(InputStream content, @Nullable String filename, @Nullable String contentType) {
        return store(content, filename, contentType, (Object) null);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, java.lang.String, java.lang.Object)
     */
    public ObjectId store(InputStream content, @Nullable String filename, @Nullable Object metadata) {
        return store(content, filename, null, metadata);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, java.lang.String, java.lang.String, java.lang.Object)
     */
    public ObjectId store(InputStream content, @Nullable String filename, @Nullable String contentType, @Nullable Object metadata) {

        Document document = null;

        if (metadata != null) {
            document = new Document();
            converter.write(metadata, document);
        }

        return store(content, filename, contentType, document);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, java.lang.String, com.mongodb.Document)
     */
    public ObjectId store(InputStream content, @Nullable String filename, @Nullable Document metadata) {
        return this.store(content, filename, null, metadata);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, java.lang.String, com.mongodb.Document)
     */
    public ObjectId store(InputStream content, @Nullable String filename, @Nullable String contentType, @Nullable Document metadata) {

        Assert.notNull(content, "InputStream must not be null!");

        GridFSUploadOptions options = new GridFSUploadOptions();

        Document mData = new Document();

        if (StringUtils.hasText(contentType)) {
            mData.put(CONTENT_TYPE_FIELD, contentType);
        }

        if (metadata != null) {
            mData.putAll(metadata);
        }

        options.metadata(mData);

        return getGridFs().uploadFromStream(filename, content, options);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#find(com.mongodb.Document)
     */
    public GridFSFindIterable find(Query query) {

        Assert.notNull(query, "Query must not be null!");

        Document queryObject = getMappedQuery(query.getQueryObject());
        Document sortObject = getMappedQuery(query.getSortObject());

        return getGridFs().find(queryObject).sort(sortObject);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#findOne(com.mongodb.Document)
     */
    public GridFSFile findOne(Query query) {
        return find(query).first();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#delete(org.springframework.data.mongodb.core.query.Query)
     */
    public void delete(Query query) {

        for (GridFSFile x : find(query)) {
            getGridFs().delete(((BsonObjectId) x.getId()).getValue());
        }
    }

    public void rename(ObjectId fileId, String newFilename) {
        FindIterable<GridFSFile> files = filesCollection.find(eq("_id", fileId));
        if (files.first() != null) {
            getGridFs().rename(fileId, newFilename);
        }
    }

    public GridFSFile updateMetadata(ObjectId fileId, FileMetadata metadata) {
        Document document = null;

        if (metadata != null) {
            document = new Document();
            converter.write(metadata, document);
        }
        return filesCollection.findOneAndUpdate(eq("_id", fileId),
                new Document("$set", new Document("metadata", document)));
    }


    /*
     * (non-Javadoc)
     * @see org.springframework.core.io.ResourceLoader#getClassLoader()
     */
    public ClassLoader getClassLoader() {
        return dbFactory.getClass().getClassLoader();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.core.io.ResourceLoader#getResource(org.bson.types.ObjectId)
     */
    public GridFsResource getResource(ObjectId fileId) {
        GridFSFile file = findOne(query(where("_id").is(fileId)));
        return file != null ? new GridFsResource(file, getGridFs().openDownloadStream(fileId)) : null;
    }

    public GridFSDownloadStream getFSDownloadStream(ObjectId fileId) {
        GridFSFile file = findOne(query(where("_id").is(fileId)));
        return file != null ? getGridFs().openDownloadStream(fileId) : null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.core.io.ResourceLoader#getResource(java.lang.String)
     */
    public GridFsResource getResource(String location) {
        GridFSFile file = findOne(query(whereFilename().is(location)));
        return file != null ? new GridFsResource(file, getGridFs().openDownloadStream(location)) : null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.core.io.support.ResourcePatternResolver#getResources(java.lang.String)
     */
    public GridFsResource[] getResources(String locationPattern) {
        if (!StringUtils.hasText(locationPattern)) {
            return new GridFsResource[0];
        }

        AntPath path = new AntPath(locationPattern);

        if (path.isPattern()) {

            GridFSFindIterable files = find(query(whereFilename().regex(path.toRegex())));
            List<GridFsResource> resources = new ArrayList<GridFsResource>();

            for (GridFSFile file : files) {
                resources.add(new GridFsResource(file, getGridFs().openDownloadStream(file.getFilename())));
            }

            return resources.toArray(new GridFsResource[resources.size()]);
        }

        return new GridFsResource[]{getResource(locationPattern)};
    }

    private Document getMappedQuery(Document query) {
        return queryMapper.getMappedObject(query, Optional.empty());
    }

    private GridFSBucket getGridFs() {
        MongoDatabase db = dbFactory.getDb();
        return bucketName == null ? GridFSBuckets.create(db) : GridFSBuckets.create(db, bucketName);
    }

    private static MongoCollection<GridFSFile> getFilesCollection(final MongoDatabase database, final String bucketName) {
        return database.getCollection(bucketName + ".files", GridFSFile.class).withCodecRegistry(
                fromRegistries(database.getCodecRegistry(), MongoClient.getDefaultCodecRegistry())
        );
    }

}
