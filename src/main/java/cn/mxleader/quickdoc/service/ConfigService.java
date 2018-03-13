package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.SysProfile;

public interface ConfigService {

    SysProfile getSysProfile();
    SysProfile saveSysProfile(SysProfile sysProfile);
}
