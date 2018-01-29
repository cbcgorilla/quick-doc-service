package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.QuickDocConfig;

public interface QuickDocConfigService {

    QuickDocConfig getQuickDocConfig();
    QuickDocConfig saveQuickDocConfig(QuickDocConfig quickDocConfig);
}
