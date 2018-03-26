package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.SysFolderRepository;
import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.entities.AuthTarget;
import cn.mxleader.quickdoc.entities.ParentLink;
import cn.mxleader.quickdoc.entities.SysFolder;
import cn.mxleader.quickdoc.service.FolderService;
import cn.mxleader.quickdoc.web.domain.TreeNode;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FolderServiceImpl implements FolderService {

    private final SysFolderRepository sysFolderRepository;

    FolderServiceImpl(SysFolderRepository sysFolderRepository) {
        this.sysFolderRepository = sysFolderRepository;
    }

    @Override
    public List<SysFolder> list(ParentLink parent) {
        return sysFolderRepository.findAllByParentsContains(parent);
    }

    @Override
    public List<TreeNode> getFolderTree(ParentLink parent) {
        return sysFolderRepository.findAllByParentsContains(parent)
                .stream()
                .map(sysFolder -> new TreeNode(sysFolder.getId().toString(),
                        sysFolder.getName(),
                        parent.getId().toString(),
                        getFolderTree(new ParentLink(sysFolder.getId(), AuthTarget.FOLDER)))
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
    public SysFolder save(String name, ParentLink parent, AccessAuthorization authorization) {
        Optional<SysFolder> optionalSysFolder = sysFolderRepository.findByParentsContainsAndName(parent, name);
        if (!optionalSysFolder.isPresent()) {
            return sysFolderRepository.save(new SysFolder(ObjectId.get(), name,
                    Arrays.asList(parent), Arrays.asList(authorization)));
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
    public SysFolder addParent(ObjectId id, ParentLink parent) {
        Optional<SysFolder> optionalSysFolder = sysFolderRepository.findById(id);
        if (optionalSysFolder.isPresent()) {
            SysFolder folder = optionalSysFolder.get();
            folder.getParents().add(parent);
            sysFolderRepository.save(folder);
        }
        return null;
    }

    @Override
    public SysFolder removeParent(ObjectId id, ParentLink parent) {
        Optional<SysFolder> optionalSysFolder = sysFolderRepository.findById(id);
        if (optionalSysFolder.isPresent()) {
            SysFolder folder = optionalSysFolder.get();
            folder.getParents().remove(parent);
            return sysFolderRepository.save(folder);
        }
        return null;
    }

    @Override
    public SysFolder addAuthorization(ObjectId id, AccessAuthorization authorization) {
        // @TODO 待实现。。。。。
        return null;
    }

    @Override
    public SysFolder removeAuthorization(ObjectId id, AccessAuthorization authorization) {
        // @TODO 待实现。。。。。
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

}
