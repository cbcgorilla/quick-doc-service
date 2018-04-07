package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.security.authorization.PreAuth;
import cn.mxleader.quickdoc.entities.AuthAction;
import cn.mxleader.quickdoc.entities.AuthTarget;
import cn.mxleader.quickdoc.entities.Authorization;
import cn.mxleader.quickdoc.entities.SysDisk;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface DiskService {

    List<SysDisk> list();

    Page<SysDisk> list(Pageable pageable);

    List<SysDisk> list(Authorization authorization);

    @PreAuth(target = AuthTarget.DISK)
    Optional<SysDisk> get(ObjectId id);

    SysDisk save(String name, Authorization authorization);

    @PreAuth(target = AuthTarget.DISK, action = AuthAction.WRITE)
    SysDisk rename(ObjectId id, String newName);

    /**
     * 增加磁盘授权信息
     *
     * @param id            文件夹ID
     * @param authorization 新增授权
     * @return
     */
    SysDisk addAuthorization(ObjectId id, Authorization authorization);

    /**
     * 删除磁盘授权信息
     *
     * @param id            文件夹ID
     * @param authorization 待删除授权
     * @return
     */
    SysDisk removeAuthorization(ObjectId id, Authorization authorization);

    void delete(ObjectId id);
}
