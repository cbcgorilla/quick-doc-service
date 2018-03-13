package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.SysProfileRepository;
import cn.mxleader.quickdoc.entities.SysProfile;
import cn.mxleader.quickdoc.service.ConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class ConfigServiceImpl implements ConfigService {
    private final SysProfileRepository sysProfileRepository;

    @Value("${server.port}")
    String serverPort;

    private String serviceAddress() {
        try {
            return InetAddress.getLocalHost().getHostName() + ":" + serverPort;
        } catch (UnknownHostException e) {
            return "localhost:" + serverPort;
        }
    }

    ConfigServiceImpl(SysProfileRepository sysProfileRepository) {
        this.sysProfileRepository = sysProfileRepository;
    }

    public SysProfile getSysProfile() {
        return sysProfileRepository.findByServiceAddress(serviceAddress());
    }

    public SysProfile saveSysProfile(SysProfile sysProfile) {
        return sysProfileRepository.save(sysProfile);
    }
}
