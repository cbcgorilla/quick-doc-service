package cn.mxleader.quickdoc.common.utils;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.springframework.lang.Nullable;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FileUtils {

    /**
     * 以下ICON CLASS信息匹配 Font Awesome 5.0.6版本
     */
    private static final Map<String, String> typeIconMap = new HashMap<String, String>() {{
        put("application/pdf", "far fa-file-pdf");
        put("text/xml", "far fa-file-code");
        put("application/x-zip-compressed", "far fa-file-archive");
        put("image/png", "far fa-file-image");
        put("image/gif", "far fa-file-image");
        put("image/jpeg", "far fa-file-image");
        put("audio/mpeg", "far fa-file-audio");
        put("text/plain", "far fa-file-alt");
        put("text/html", "fab fa-html5");
    }};

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

    public static String getContentType(String filename) {
        try {
            Path path = Paths.get(filename.toLowerCase());
            // CentOS 7 JDK8 返回值为Null， UBUNTU和Windows环境返回值正常
            String type = Files.probeContentType(path);
            return type == null ? "application/octet-stream" : type;
        } catch (IOException exp) {
            return "application/octet-stream";
        }
    }

    public static String getMimeType(String filename) {
        Tika tika = new Tika();
        String mimeType = tika.detect(filename);
        return mimeType;
    }

    public static String getMimeType(InputStream inputStream, String filename) {
        AutoDetectParser parser = new AutoDetectParser();
        parser.setParsers(new HashMap<MediaType, Parser>());

        Metadata metadata = new Metadata();
        metadata.add(TikaMetadataKeys.RESOURCE_NAME_KEY, filename);

        try {
            parser.parse(inputStream, new DefaultHandler(), metadata, new ParseContext());
            inputStream.close();
        } catch (TikaException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return metadata.get(HttpHeaders.CONTENT_TYPE);
    }

    public static String read(InputStream input) throws IOException {
        return read(input, Charset.defaultCharset());
    }

    public static String read(InputStream input, Charset charset) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input, charset))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }
}
