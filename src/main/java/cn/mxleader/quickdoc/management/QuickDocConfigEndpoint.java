package cn.mxleader.quickdoc.management;

import cn.mxleader.quickdoc.entities.QuickDocHealth;
import cn.mxleader.quickdoc.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Endpoint(id = "quick-doc-health")
@Component
public class QuickDocConfigEndpoint {
    private final ConfigService configService;

    @Autowired
    public QuickDocConfigEndpoint(ConfigService configService) {
        this.configService = configService;
    }

    @ReadOperation
    public QuickDocHealth quickDocConfig() {
        return configService.getQuickDocHealth();
    }

}
