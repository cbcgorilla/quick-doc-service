package cn.techfan.quickdoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QuickDocApplication {

    private static void test(){
        System.out.println("ddddd");
    }
    public static void main(String[] args) {
        SpringApplication.run(QuickDocApplication.class, args);
    }
}
