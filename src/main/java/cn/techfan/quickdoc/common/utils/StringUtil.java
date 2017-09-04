package cn.techfan.quickdoc.common.utils;

import org.springframework.lang.Nullable;

public class StringUtil {

    @Nullable
    public static String getFilename(String path) {
        if (path == null) {
            return null;
        } else {
            int startUnix = path.lastIndexOf("/");
            int startWindows = path.lastIndexOf("\\");
            int start = startUnix > startWindows ? startUnix : startWindows;
            if (start > -1) {
                return path.substring(start + 1);
            } else {
                return path;
            }
        }
    }
}
