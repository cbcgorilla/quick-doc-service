package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.SysDiskRepository;
import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.entities.SysDisk;
import cn.mxleader.quickdoc.service.DiskService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    public List<SysDisk> list(AccessAuthorization authorization) {
        return sysDiskRepository.findAllByAuthorizationsContains(authorization);
    }

    @Override
    public Optional<SysDisk> get(ObjectId id) {
        return sysDiskRepository.findById(id);
    }

    @Override
    public SysDisk save(String name, AccessAuthorization authorization) {
        return save(name, Arrays.asList(authorization));
    }

    @Override
    public SysDisk save(String name, List<AccessAuthorization> authorizations) {
        return sysDiskRepository.save(new SysDisk(ObjectId.get(), name, authorizations));
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
    public SysDisk addAuthorization(ObjectId id, AccessAuthorization authorization) {
        Optional<SysDisk> optionalSysDisk = sysDiskRepository.findById(id);
        if (optionalSysDisk.isPresent()) {
            SysDisk disk = optionalSysDisk.get();
            disk.getAuthorizations().add(authorization);
            return sysDiskRepository.save(disk);
        }
        return null;
    }

    @Override
    public SysDisk removeAuthorization(ObjectId id, AccessAuthorization authorization) {
        Optional<SysDisk> optionalSysDisk = sysDiskRepository.findById(id);
        if (optionalSysDisk.isPresent()) {
            SysDisk disk = optionalSysDisk.get();
            disk.getAuthorizations().remove(authorization);
            return sysDiskRepository.save(disk);
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
