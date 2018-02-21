package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.FileMetadata;
import cn.mxleader.quickdoc.entities.TFMatchValue;
import org.bson.types.ObjectId;

public interface TensorFlowService {
    TFMatchValue getImageMatch(byte[] imageBytes);

    void updateImageMetadata(ObjectId fileId, FileMetadata metadata);
}
