package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.QuickDocConfig;
import reactor.core.publisher.Mono;

public interface ReactiveQuickDocConfigService {

    Mono<QuickDocConfig> getQuickDocConfig();
    Mono<QuickDocConfig> saveQuickDocConfig(QuickDocConfig quickDocConfig);
}
