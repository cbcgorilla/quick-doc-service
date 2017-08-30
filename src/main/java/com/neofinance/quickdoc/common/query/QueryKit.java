package com.neofinance.quickdoc.common.query;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;

public class QueryKit {

    public static Query keyQuery(String id) {
        return Query.query(GridFsCriteria.where(id));
    }

    public static Query keyQuery(ObjectId id) {
        return keyQuery(id.toString());
    }

}
