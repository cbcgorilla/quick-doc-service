package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.QuickDocHealth;

public interface ConfigService {

    QuickDocHealth getQuickDocHealth();
    QuickDocHealth saveQuickDocHealth(QuickDocHealth quickDocHealth);
}
