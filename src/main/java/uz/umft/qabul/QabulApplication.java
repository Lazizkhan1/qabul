package uz.umft.qabul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QabulApplication {

    public static void main(String[] args) {
        SpringApplication.run(QabulApplication.class, args);
    }

}
