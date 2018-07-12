package cn.mxleader.quickdoc.config;

import cn.mxleader.quickdoc.common.condition.WindowsCondition;
import cn.mxleader.quickdoc.service.PreviewService;
import cn.mxleader.quickdoc.service.impl.PreviewServiceOnWindowsImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(WindowsCondition.class)
public class PreviewWindowsConfiguration {
    @Bean
    public PreviewService getWindowsPreviewService(){
        return new PreviewServiceOnWindowsImpl();
    }
}
