package cn.mxleader.quickdoc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class QuickDocApplication {

    /**
     * 设置SLF4J日志输出级别
     *
     * @param level 日志级别
     */
    public static void setLoggingLevel(Level level) {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }

    public static void main(String[] args) {
        SpringApplication.run(QuickDocApplication.class, args);
    }
}
