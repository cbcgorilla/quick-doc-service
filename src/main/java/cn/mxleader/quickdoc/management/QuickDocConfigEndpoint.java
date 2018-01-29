package cn.mxleader.quickdoc.management;

import cn.mxleader.quickdoc.entities.QuickDocConfig;
import cn.mxleader.quickdoc.service.QuickDocConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Endpoint(id = "quick-doc-config")
@Component
public class QuickDocConfigEndpoint {
    private final QuickDocConfigService quickDocConfigService;

    @Autowired
    public QuickDocConfigEndpoint(QuickDocConfigService quickDocConfigService) {
        this.quickDocConfigService = quickDocConfigService;
    }

    @ReadOperation
    public QuickDocConfig quickDocConfig() {
        return quickDocConfigService.getQuickDocConfig();
    }

}
