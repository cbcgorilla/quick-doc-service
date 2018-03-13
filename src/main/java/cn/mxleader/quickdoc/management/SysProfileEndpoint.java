package cn.mxleader.quickdoc.management;

import cn.mxleader.quickdoc.entities.SysProfile;
import cn.mxleader.quickdoc.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Endpoint(id = "sys-profile")
@Component
public class SysProfileEndpoint {
    private final ConfigService configService;

    @Autowired
    public SysProfileEndpoint(ConfigService configService) {
        this.configService = configService;
    }

    @ReadOperation
    public SysProfile sysProfile() {
        return configService.getSysProfile();
    }

}
