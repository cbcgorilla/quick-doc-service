package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.FsDescription;
import cn.mxleader.quickdoc.entities.TfMatch;

public interface TensorFlowService {
    TfMatch getImageMatch(byte[] imageBytes);
    void updateImageLabel(FsDescription fsDescription) ;
}
