package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.common.annotation.PreAuth;
import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.entities.AuthTarget;
import cn.mxleader.quickdoc.entities.SysDisk;
import cn.mxleader.quickdoc.entities.SysFolder;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface DiskService {

    List<SysDisk> list();

    List<SysDisk> list(AccessAuthorization authorization);

    @PreAuth(target = AuthTarget.DISK)
    Optional<SysDisk> get(ObjectId id);
    SysDisk save(String name, AccessAuthorization authorization);
    SysDisk save(String name, List<AccessAuthorization> authorizations);
    SysDisk rename(ObjectId id, String newName);

    /**
     * 增加磁盘授权信息
     *
     * @param id            文件夹ID
     * @param authorization 新增授权
     * @return
     */
    SysDisk addAuthorization(ObjectId id, AccessAuthorization authorization);

    /**
     * 删除磁盘授权信息
     *
     * @param id            文件夹ID
     * @param authorization 待删除授权
     * @return
     */
    SysDisk removeAuthorization(ObjectId id, AccessAuthorization authorization);

    void delete(ObjectId id);
}
