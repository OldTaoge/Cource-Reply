package space.oldtaoge.edu.junsen.worker;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import space.oldtaoge.edu.junsen.dto.FileSaveDto;
import space.oldtaoge.edu.junsen.entity.Lesson;
import space.oldtaoge.edu.junsen.mapper.LessonMapper;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static space.oldtaoge.edu.junsen.worker.TranscodeWorker.FFMPEG_WORKER_IP;
import static space.oldtaoge.edu.junsen.worker.TranscodeWorker.REMOTE_EXEC_BEFORE;
import static space.oldtaoge.edu.junsen.worker.TranscodeWorker.SSH_ARGS;
import static space.oldtaoge.edu.junsen.worker.TranscodeWorker.WEB_WORKER_IP;

@Component
public class UploadWorker {
    private final UploadWorkerRegister register = UploadWorkerRegister.getInstance();

    @Value("${service.upload}")
    private boolean isEnable;

    @Resource
    private LessonMapper lessonMapper;

    @Async
    @Scheduled(cron = "0 */1 * * * ?")
    public void upload() {
        if (!register.getUploadQueue().isEmpty() && register.getIsUploading() == 0 && register.getInsReq() == 0 && isEnable)
        {
            try {
                register.setIsUploading(1);
                register.getUploadQueue().forEach(d -> {
                    if (register.getInsReq() == 1)
                    {
                        register.setInsResp(1);
                        while (register.getInsReq() == 1) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        register.setInsResp(0);
                    } else {
                        uploadAction(d);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                register.setIsUploading(0);
            }

        }
    }

    private void uploadAction(FileSaveDto.UploadDto d)
    {
        try {
            TranscodeWorker.StartExec("ssh " + SSH_ARGS + " root@" + WEB_WORKER_IP + " \"" + REMOTE_EXEC_BEFORE + "rsync -v -r -t --delete --password-file=/etc/rsync.password /mnt/webdata/jun-sen/ rsync://admins@nas.oldtaoge.space/bcdn/jun-sen/\"");
            if (d.getLessonId() != 0 && d.getVideoPath() != null)
            {
                Lesson lesson = lessonMapper.selectById(d.getLessonId());
                lesson.setVideo(String.valueOf(d.getVideoPath()));
                lessonMapper.updateById(lesson);
            }
            register.getUploadQueue().remove(d);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public void insUpload(FileSaveDto.UploadDto d)
    {
        if (!isEnable) return;
        register.setInsReq(1);
        if (register.getIsUploading() == 1)
        {
            while (register.getInsResp() == 0)
            {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        uploadAction(d);
        register.setInsReq(0);
    }

    @Data
    public static class UploadWorkerRegister {
        private int isUploading = 0;
        private int insReq = 0;
        private int insResp = 1;


        private List<FileSaveDto.UploadDto> uploadQueue = new ArrayList<>();

        private static UploadWorkerRegister instance = new UploadWorkerRegister();

        public static UploadWorkerRegister getInstance() {
            return instance;
        }
    }
}
