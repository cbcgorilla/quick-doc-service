package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.security.authorization.PreAuth;
import cn.mxleader.quickdoc.entities.*;
import cn.mxleader.quickdoc.web.domain.TreeNode;
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
    List<SysFolder> list(ParentLink parent);

    @PreAuth(target = AuthTarget.DISK)
    List<SysFolder> listFoldersInDisk(ObjectId diskId);

    @PreAuth(field = ParentLink.class)
    List<TreeNode> getFolderTree(ParentLink parent);

    /**
     * 根据ID获取文件目录信息
     *
     * @param id 文件目录ID
     * @return
     */
    Optional<SysFolder> get(ObjectId id);

    /**
     * 新增文件目录；
     *
     * @param name
     * @param parent
     * @return
     */
    SysFolder save(String name, ParentLink parent);

    /**
     * 新增文件目录；
     *
     * @param name
     * @param parent
     * @param authorization
     * @return
     */
    SysFolder save(String name, ParentLink parent, Authorization authorization);

    /**
     * 重命名文件目录
     *
     * @param id
     * @param newName
     * @return
     */
    @PreAuth(actions = AuthAction.WRITE)
    SysFolder rename(ObjectId id, String newName);

    /**
     * 删除文件目录
     *
     * @param folderId
     * @return
     */
    void delete(ObjectId folderId);

    /**
     * 增加磁盘授权信息
     *
     * @param id            文件夹ID
     * @param authorization 新增授权
     * @return
     */
    SysFolder addAuthorization(ObjectId id, Authorization authorization);

    /**
     * 删除磁盘授权信息
     *
     * @param id            文件夹ID
     * @param authorization 待删除授权
     * @return
     */
    SysFolder removeAuthorization(ObjectId id, Authorization authorization);


}
