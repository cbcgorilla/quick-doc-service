package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.SysFolderRepository;
import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.entities.ParentLink;
import cn.mxleader.quickdoc.entities.SysFolder;
import cn.mxleader.quickdoc.service.FolderService;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class FolderServiceImpl implements FolderService {

    private final SysFolderRepository sysFolderRepository;
    private final MongoTemplate mongoTemplate;

    FolderServiceImpl(SysFolderRepository sysFolderRepository,
                      MongoTemplate mongoTemplate) {
        this.sysFolderRepository = sysFolderRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<SysFolder> findAllByParent(ParentLink parent) {
        return sysFolderRepository.findAllByParentsContains(parent);
    }

    /**
     * 根据ID获取文件目录信息
     *
     * @param id 文件目录ID
     * @return
     */
    @Override
    public Optional<SysFolder> findById(ObjectId id) {
        return sysFolderRepository.findById(id);
    }

    @Override
    public SysFolder save(String name, ParentLink parent, List<AccessAuthorization> authorizations) {
        Optional<SysFolder> optionalSysFolder = sysFolderRepository.findByParentsContainsAndName(parent, name);
        if(!optionalSysFolder.isPresent()){
            return sysFolderRepository.save(new SysFolder(ObjectId.get(),name,
                    new ArrayList<>(Arrays.asList(parent)),authorizations));
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
    public void delete(ObjectId id) {
        Optional<SysFolder> optionalSysFolder = sysFolderRepository.findById(id);
        if (optionalSysFolder.isPresent()) {
            SysFolder folder = optionalSysFolder.get();
            // @TODO 方案1：检查子目录和文件是否存在，方案2：同步删除子目录和文件 (隐患，子目录有可能与多组共享)
            sysFolderRepository.delete(folder);
        }
    }

}
