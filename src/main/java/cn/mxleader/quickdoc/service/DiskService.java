package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.entities.SysDisk;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface DiskService {

    List<SysDisk> list(AccessAuthorization authorization);
    Optional<SysDisk> get(ObjectId id);
    SysDisk save(String name, AccessAuthorization authorization);
    SysDisk rename(ObjectId id, String newName);
    void delete(ObjectId id);
}
