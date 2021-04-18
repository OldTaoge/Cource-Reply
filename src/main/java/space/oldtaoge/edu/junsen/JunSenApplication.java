package space.oldtaoge.edu.junsen;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = "space.oldtaoge.edu.junsen.mapper")
public class JunSenApplication {

    public static void main(String[] args) {
        SpringApplication.run(JunSenApplication.class, args);
    }

}
