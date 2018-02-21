package cn.mxleader.quickdoc.common.utils;

import org.springframework.lang.Nullable;

import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FileUtils {

    /**
     * 不同格式文件对应的预览资源路径前缀
     */
    private static final Map<String, String> typeLinkPrefix = new HashMap<String, String>() {{
        put("application/pdf", "/view-pdf/");
        put("image/png", "/view-png/");
        put("image/gif", "/view-gif/");
        put("image/jpeg", "/view-jpeg/");
        put("text/plain", "/view-text/");
        put("text/html", "/view-text/");
        put("application/xml", "/view-text/");
    }};

    /**
     * 以下ICON CLASS信息匹配 Font Awesome 5.0.6版本
     */
    private static final Map<String, String> typeIconMap = new HashMap<String, String>() {{
        put("application/pdf", "far fa-file-pdf");
        put("application/xml", "far fa-file-code");
        put("application/zip", "far fa-file-archive");
        put("image/png", "far fa-file-image");
        put("image/gif", "far fa-file-image");
        put("image/jpeg", "far fa-file-image");
        put("audio/mpeg", "far fa-file-audio");
        put("text/plain", "far fa-file-alt");
        put("text/html", "fab fa-html5");
    }};

    /**
     * 根据文件类型获取预览链接前缀
     *
     * @param contentType
     * @return
     */
    public static String getLinkPrefix(String contentType) {
        return typeLinkPrefix.get(contentType) == null ? "/download/" : typeLinkPrefix.get(contentType);
    }

    /**
     * 根据文件类型获取显示图标
     *
     * @param contentType
     * @return
     */
    public static String getIconClass(String contentType) {
        return typeIconMap.get(contentType) == null ? "far fa-file" : typeIconMap.get(contentType);
    }

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

    public static String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = null;
        try {
            contentTypeFor = fileNameMap.getContentTypeFor(URLEncoder.encode(path, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    public static String read(InputStream input) throws IOException {
        return read(input, Charset.defaultCharset());
    }
    public static String read(InputStream input,Charset charset) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input,charset))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }
}
