package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.entities.ParentLink;
import cn.mxleader.quickdoc.entities.SysFolder;
import cn.mxleader.quickdoc.web.domain.WebFolder;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface FolderService {

    /**
     * 依据上级目录信息获取子目录列表
     *
     * @param parent 上级目录（可能为磁盘或目录）
     * @return
     */
    List<SysFolder> findAllByParent(ParentLink parent);

    List<WebFolder> findAllByParentInWebFormat(ParentLink parent);

    /**
     * 根据ID获取文件目录信息
     *
     * @param id 文件目录ID
     * @return
     */
    Optional<SysFolder> findById(ObjectId id);

    /**
     * 新增文件目录；
     *
     * @param name
     * @param parents
     * @param authorizations
     * @return
     */
    SysFolder save(String name, ParentLink[] parents, AccessAuthorization[] authorizations);


    /**
     * 重命名文件目录
     *
     * @param id
     * @param newName
     * @return
     */
    SysFolder rename(ObjectId id, String newName);

    /**
     * 迁移文件目录
     *
     * @param id        待迁移文件夹ID
     * @param oldParent 原始上级目录信息
     * @param newParent 新上级目录信息
     * @return
     */
    SysFolder move(ObjectId id, ParentLink oldParent, ParentLink newParent);

    /**
     * 删除文件目录
     *
     * @param folderId
     * @return
     */
    void delete(ObjectId folderId);


}
