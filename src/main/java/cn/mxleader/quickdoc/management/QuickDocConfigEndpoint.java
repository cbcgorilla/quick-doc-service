package cn.mxleader.quickdoc.management;

import cn.mxleader.quickdoc.entities.QuickDocConfig;
import cn.mxleader.quickdoc.service.ReactiveQuickDocConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Endpoint(id = "quick-doc-config")
@Component
public class QuickDocConfigEndpoint {
    private final ReactiveQuickDocConfigService reactiveQuickDocConfigService;

    @Autowired
    public QuickDocConfigEndpoint(ReactiveQuickDocConfigService reactiveQuickDocConfigService) {
        this.reactiveQuickDocConfigService = reactiveQuickDocConfigService;
    }

    @ReadOperation
    public Mono<QuickDocConfig> quickDocConfig() {
        return reactiveQuickDocConfigService.getQuickDocConfig();
    }

}
