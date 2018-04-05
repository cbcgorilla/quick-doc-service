package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.SysDiskRepository;
import cn.mxleader.quickdoc.dao.SysFolderRepository;
import cn.mxleader.quickdoc.entities.*;
import cn.mxleader.quickdoc.service.FolderService;
import cn.mxleader.quickdoc.web.domain.TreeNode;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FolderServiceImpl implements FolderService {

    private final SysDiskRepository sysDiskRepository;
    private final SysFolderRepository sysFolderRepository;

    FolderServiceImpl(SysDiskRepository sysDiskRepository,
                      SysFolderRepository sysFolderRepository) {
        this.sysDiskRepository = sysDiskRepository;
        this.sysFolderRepository = sysFolderRepository;
    }

    @Override
    public List<SysFolder> list(ParentLink parent) {
        return sysFolderRepository.findAllByParent(parent);
    }

    @Override
    public List<SysFolder> listFoldersInDisk(ObjectId diskId) {
        return sysFolderRepository.findAllByParentDiskId(diskId);
    }

    @Override
    public List<TreeNode> getFolderTree(ParentLink parent) {
        return sysFolderRepository.findAllByParent(parent)
                .stream()
                .map(sysFolder -> new TreeNode(sysFolder.getId().toString(), sysFolder.getName(),
                        parent.getId().toString(),
                        getFolderTree(new ParentLink(sysFolder.getId(), AuthTarget.FOLDER, parent.getDiskId())))
                )
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取文件目录信息
     *
     * @param id 文件目录ID
     * @return
     */
    @Override
    public Optional<SysFolder> get(ObjectId id) {
        return sysFolderRepository.findById(id);
    }

    @Override
    public SysFolder save(String name, ParentLink parent) {
        Optional<SysFolder> optionalSysFolder = sysFolderRepository.findByParentAndName(parent, name);
        if (!optionalSysFolder.isPresent()) {
            if (parent.getTarget().equals(AuthTarget.DISK)) {
                Optional<SysDisk> sysDisk = sysDiskRepository.findById(parent.getId());
                if (sysDisk.isPresent()) {
                    return sysFolderRepository.save(new SysFolder(ObjectId.get(), name, parent,
                            sysDisk.get().getAuthorizations()));
                }
            } else {
                Optional<SysFolder> sysFolder = sysFolderRepository.findById(parent.getId());
                if (sysFolder.isPresent()) {
                    return sysFolderRepository.save(new SysFolder(ObjectId.get(), name, parent,
                            sysFolder.get().getAuthorizations()));
                }
            }
        }
        return null;
    }

    @Override
    public SysFolder save(String name, ParentLink parent, Authorization authorization) {
        Optional<SysFolder> optionalSysFolder = sysFolderRepository.findByParentAndName(parent, name);
        if (!optionalSysFolder.isPresent()) {
            return sysFolderRepository.save(new SysFolder(ObjectId.get(), name, parent,
                    new HashSet<Authorization>() {{
                        add(authorization);
                    }}));
        }
        return null;
    }

    @Override
    public SysFolder rename(ObjectId id, String newName) {
        Optional<SysFolder> optionalSysFolder = sysFolderRepository.findById(id);
        if (optionalSysFolder.isPresent()) {
            SysFolder folder = optionalSysFolder.get();
            //@TODO 检查名称是否冲突
            folder.setName(newName);
            return sysFolderRepository.save(folder);
        }
        return null;
    }

    @Override
    public void delete(ObjectId id) {
        Optional<SysFolder> optionalSysFolder = sysFolderRepository.findById(id);
        if (optionalSysFolder.isPresent()) {
            SysFolder folder = optionalSysFolder.get();
            // @TODO 方案1：检查子目录和文件是否存在，方案2：同步删除子目录和文件 (隐患，子目录有可能与多组共享)
            sysFolderRepository.delete(folder);
        }
    }

    @Override
    public SysFolder addAuthorization(ObjectId id, Authorization authorization) {
        Optional<SysFolder> sysFolder = sysFolderRepository.findById(id);
        if (sysFolder.isPresent()) {
            SysFolder folder = sysFolder.get();
            Set<Authorization> authorizations = folder.getAuthorizations();
            for (Authorization item : authorizations) {
                if (item.getName().equalsIgnoreCase(authorization.getName())
                        && item.getType().equals(authorization.getType())) {
                    for (AuthAction action : authorization.getActions()) {
                        item.add(action);
                    }
                    return sysFolderRepository.save(folder);
                }
            }
            folder.addAuthorization(authorization);
            return sysFolderRepository.save(folder);
        }
        return null;
    }

    @Override
    public SysFolder removeAuthorization(ObjectId id, Authorization authorization) {
        Optional<SysFolder> sysFolder = sysFolderRepository.findById(id);
        if (sysFolder.isPresent()) {
            SysFolder folder = sysFolder.get();
            Set<Authorization> authorizations = folder.getAuthorizations();
            for (Authorization item : authorizations) {
                if (item.getName().equalsIgnoreCase(authorization.getName())
                        && item.getType().equals(authorization.getType())) {
                    for (AuthAction action : authorization.getActions()) {
                        item.remove(action);
                    }
                    if(item.getActions().size()==0){
                        folder.removeAuthorization(item);
                    }
                    return sysFolderRepository.save(folder);
                }
            }
        }
        return null;
    }

}
