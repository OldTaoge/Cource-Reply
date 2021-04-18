package space.oldtaoge.edu.junsen;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import space.oldtaoge.edu.junsen.entity.Lesson;
import space.oldtaoge.edu.junsen.service.ILessonService;
import space.oldtaoge.edu.junsen.worker.TranscodeWorker;

import javax.annotation.Resource;
import java.io.Console;
import java.time.LocalDate;
import java.util.Scanner;

@SpringBootTest
class JunSenApplicationTests {
    @Resource
    TranscodeWorker transcodeWorker;

    @Test
    @Rollback
    void contextLoads() {
//        transcodeWorker.TransInsert(1L, "/root/young.flv");
//        transcodeWorker.DeleteFile("312/gre/1.2　太阳对地球的影响导学案xs.doc");
    }

}
