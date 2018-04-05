package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.SysDiskRepository;
import cn.mxleader.quickdoc.entities.AuthAction;
import cn.mxleader.quickdoc.entities.Authorization;
import cn.mxleader.quickdoc.entities.SysDisk;
import cn.mxleader.quickdoc.service.DiskService;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class DiskServiceImpl implements DiskService {
    private final SysDiskRepository sysDiskRepository;

    DiskServiceImpl(SysDiskRepository sysDiskRepository) {
        this.sysDiskRepository = sysDiskRepository;
    }

    @Override
    public List<SysDisk> list() {
        return sysDiskRepository.findAll();
    }

    @Override
    public Page<SysDisk> list(Pageable pageable) {
        return sysDiskRepository.findAll(pageable);
    }

    @Override
    public List<SysDisk> list(Authorization authorization) {
        return sysDiskRepository.findAllByAuthorizations(
                authorization.getName(),
                authorization.getType(),
                authorization.getActions());
    }

    @Override
    public Optional<SysDisk> get(ObjectId id) {
        return sysDiskRepository.findById(id);
    }

    @Override
    public SysDisk save(String name, Authorization authorization) {
        return sysDiskRepository.save(new SysDisk(ObjectId.get(), name,
                new HashSet<Authorization>() {{
                    add(authorization);
                }}));
    }

    @Override
    public SysDisk rename(ObjectId id, String newName) {
        Optional<SysDisk> optionalSysDisk = sysDiskRepository.findById(id);
        if (optionalSysDisk.isPresent()) {
            SysDisk disk = optionalSysDisk.get();
            disk.setName(newName);
            return sysDiskRepository.save(disk);
        }
        return null;
    }

    @Override
    public SysDisk addAuthorization(ObjectId id, Authorization authorization) {
        Optional<SysDisk> optionalSysDisk = sysDiskRepository.findById(id);
        if (optionalSysDisk.isPresent()) {
            SysDisk disk = optionalSysDisk.get();
            Set<Authorization> authorizations = disk.getAuthorizations();
            for (Authorization item : authorizations) {
                if (item.getName().equalsIgnoreCase(authorization.getName())
                        && item.getType().equals(authorization.getType())) {
                    for (AuthAction action : authorization.getActions()) {
                        item.add(action);
                    }
                    return sysDiskRepository.save(disk);
                }
            }
            disk.addAuthorization(authorization);
            return sysDiskRepository.save(disk);
        }
        return null;
    }

    @Override
    public SysDisk removeAuthorization(ObjectId id, Authorization authorization) {
        Optional<SysDisk> optionalSysDisk = sysDiskRepository.findById(id);
        if (optionalSysDisk.isPresent()) {
            SysDisk disk = optionalSysDisk.get();
            Set<Authorization> authorizations = disk.getAuthorizations();
            for (Authorization item : authorizations) {
                if (item.getName().equalsIgnoreCase(authorization.getName())
                        && item.getType().equals(authorization.getType())) {
                    for (AuthAction action : authorization.getActions()) {
                        item.remove(action);
                    }
                    if (item.getActions().size() == 0) {
                        disk.removeAuthorization(item);
                    }
                    return sysDiskRepository.save(disk);
                }
            }
        }
        return null;
    }

    @Override
    public void delete(ObjectId id) {
        Optional<SysDisk> optionalSysDisk = sysDiskRepository.findById(id);
        if (optionalSysDisk.isPresent()) {
            SysDisk disk = optionalSysDisk.get();
            sysDiskRepository.delete(disk);
        }
    }
}
