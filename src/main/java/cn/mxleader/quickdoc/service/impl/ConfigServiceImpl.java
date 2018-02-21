package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.QuickDocHealthRepository;
import cn.mxleader.quickdoc.entities.QuickDocHealth;
import cn.mxleader.quickdoc.service.ConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class ConfigServiceImpl implements ConfigService {
    private final QuickDocHealthRepository quickDocHealthRepository;

    @Value("${server.port}")
    String serverPort;

    private String serviceAddress() {
        try {
            return InetAddress.getLocalHost().getHostName() + ":" + serverPort;
        } catch (UnknownHostException e) {
            return "localhost:" + serverPort;
        }
    }

    ConfigServiceImpl(QuickDocHealthRepository quickDocHealthRepository) {
        this.quickDocHealthRepository = quickDocHealthRepository;
    }

    public QuickDocHealth getQuickDocHealth() {
        return quickDocHealthRepository.findByServiceAddress(serviceAddress());
    }

    public QuickDocHealth saveQuickDocHealth(QuickDocHealth quickDocHealth) {
        return quickDocHealthRepository.save(quickDocHealth);
    }
}
