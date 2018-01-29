package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.QuickDocConfigRepository;
import cn.mxleader.quickdoc.entities.QuickDocConfig;
import cn.mxleader.quickdoc.service.QuickDocConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class QuickDocConfigServiceImpl implements QuickDocConfigService {
    private final QuickDocConfigRepository quickDocConfigRepository;

    @Value("${server.port}")
    String serverPort;

    private String serviceAddress() {
        try {
            return InetAddress.getLocalHost().getHostName() + ":" + serverPort;
        } catch (UnknownHostException e) {
            return "localhost:"+serverPort;
        }
    }

    QuickDocConfigServiceImpl(QuickDocConfigRepository quickDocConfigRepository) {
        this.quickDocConfigRepository = quickDocConfigRepository;
    }

    public QuickDocConfig getQuickDocConfig(){
        return quickDocConfigRepository.findByServiceAddress(serviceAddress());
    }

    public QuickDocConfig saveQuickDocConfig(QuickDocConfig quickDocConfig) {
        return quickDocConfigRepository.save(quickDocConfig);
    }
}
