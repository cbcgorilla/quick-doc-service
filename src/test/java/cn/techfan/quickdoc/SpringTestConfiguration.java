package cn.techfan.quickdoc;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = { "cn.techfan.quickdoc.service" })
@TestConfiguration
public class SpringTestConfiguration {
}
