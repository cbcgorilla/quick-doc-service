package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.entities.QuickDocConfig;
import cn.mxleader.quickdoc.dao.ReactiveQuickDocConfigRepository;
import cn.mxleader.quickdoc.service.ReactiveQuickDocConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import static cn.mxleader.quickdoc.common.utils.KeyUtil.longID;

@Service
public class ReactiveQuickDocConfigServiceImpl implements ReactiveQuickDocConfigService {
    private final ReactiveQuickDocConfigRepository reactiveQuickDocConfigRepository;

    @Value("${server.port}")
    String serverPort;

    private String getServiceAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName() + ":" + serverPort;
    }

    ReactiveQuickDocConfigServiceImpl(ReactiveQuickDocConfigRepository reactiveQuickDocConfigRepository) {
        this.reactiveQuickDocConfigRepository = reactiveQuickDocConfigRepository;
    }

    public Mono<QuickDocConfig> getQuickDocConfig() {
        try {
            QuickDocConfig quickDocConfig = new QuickDocConfig(longID(), getServiceAddress(),
                    false, new Date(), null);
            return reactiveQuickDocConfigRepository.findByServiceAddress(quickDocConfig.getServiceAddress())
                    .defaultIfEmpty(quickDocConfig);
        } catch (UnknownHostException e) {
            return Mono.error(e);
        }
    }

    public Mono<QuickDocConfig> saveQuickDocConfig(QuickDocConfig quickDocConfig) {
        return reactiveQuickDocConfigRepository.save(quickDocConfig);
    }
}
