package cn.mxleader.quickdoc.common;

import cn.mxleader.quickdoc.entities.FsOwner;

public class CommonCode {

    public static final FsOwner SYSTEM_PUBLIC_OWNER = new FsOwner("public",
            FsOwner.Type.TYPE_PUBLIC, 1);
    public static final FsOwner SYSTEM_ADMIN_GROUP_OWNER = new FsOwner("admin",
            FsOwner.Type.TYPE_GROUP, 7);

}
