package cn.mxleader.quickdoc.persistent.template;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;

public class QueryTemplate {

    public static Query keyQuery(String id) {
        return Query.query(GridFsCriteria.where(id));
    }

    public static Query keyQuery(ObjectId id) {
        return keyQuery(id.toString());
    }

}
