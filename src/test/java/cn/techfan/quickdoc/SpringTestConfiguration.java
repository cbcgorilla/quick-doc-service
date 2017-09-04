package cn.techfan.quickdoc;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = { "com.neofinance.quickdoc.service" })
@TestConfiguration
public class SpringTestConfiguration {
}
