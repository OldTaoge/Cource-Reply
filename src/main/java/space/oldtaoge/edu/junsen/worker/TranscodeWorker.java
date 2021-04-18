package space.oldtaoge.edu.junsen.worker;

import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import space.oldtaoge.edu.junsen.common.JsonUtils;
import space.oldtaoge.edu.junsen.dto.FileSaveDto;
import space.oldtaoge.edu.junsen.dto.TranscodeDto;
import space.oldtaoge.edu.junsen.entity.Lesson;
import space.oldtaoge.edu.junsen.mapper.LessonMapper;
import space.oldtaoge.edu.junsen.service.ILessonService;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class TranscodeWorker {
    public static final String FFMPEG_WORKER_IP = "172.17.0.12";
    public static final String WEB_WORKER_IP = "172.17.0.2";
    public static final String WEB_BASE_PATH = "/home/wwwroot/jun-sen/";
    public static final String COMMAND_EXEC_SERVER = "127.0.0.1";
    public static final int COMMAND_EXEC_PORT = 8081;
    public static final String REMOTE_EXEC_BEFORE = "";
    public static final String SSH_ARGS = "";

    @Resource
    private LessonMapper lessonMapper;

    @Resource
    private ILessonService lessonService;

    @Resource
    private UploadWorker uploadWorker;

    private final TranscodeWorkerRegister register = TranscodeWorkerRegister.getInstance();

    private final UploadWorker.UploadWorkerRegister uploadWorkerRegister = UploadWorker.UploadWorkerRegister.getInstance();



    @Async
    @Scheduled(cron = "0 */1 * * * ?")
    public void GoWork()
    {

        if (register.isWorking == 0)
        {
            register.setIsWorking(1);
            List<TranscodeDto> transcodeQueue = register.getTranscodeQueue();
            while (!transcodeQueue.isEmpty()) {
                TranscodeDto transcodeDto = transcodeQueue.remove(0);
                try {
                    TransInsert(transcodeDto.getId(), transcodeDto.getVideoFile());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new File(transcodeDto.getVideoFile()).delete();
            }
            register.setIsWorking(0);
        }
    }

    public int TransInsert(Long id, String videoFile)
    {
        videoFile = videoFile.replace(" ", "");
        Lesson lesson = lessonMapper.selectById(id);
        LocalDate date = lesson.getDate();
        String prefix = lesson.getPrefix();

        String dirName = UUID.randomUUID().toString();
        String fileName = videoFile.substring(videoFile.replace("\\", "/").lastIndexOf("/") + 1);
        String datePath = String.valueOf(date.getMonthValue()) + date.getDayOfMonth();
        String webPath = WEB_BASE_PATH + datePath + "/" + prefix + "/";
        int status = 1;
        try {
            String command = "ssh " + SSH_ARGS + " root@" + FFMPEG_WORKER_IP + " \"" + REMOTE_EXEC_BEFORE + "mkdir /tmp/" + dirName + "\"";
            status += StartExec(command);

            Thread.sleep(15000);


            command = "scp -B -q " + videoFile + " root@" + FFMPEG_WORKER_IP + ":/tmp/" + dirName + "/" + fileName;
            status += StartExec(command);

            Thread.sleep(5000);

            command = "ssh " + SSH_ARGS + " root@" + FFMPEG_WORKER_IP + " \"" + REMOTE_EXEC_BEFORE + "cd /tmp/" + dirName + "&&" +
                    "ffmpeg -i '" + fileName + "' -c:v h264 -b:v 256k -bufsize 256k -maxrate 512k -c:a aac -f segment -segment_list rec.m3u8 -segment_time 5 rec%03d.ts 2>&1 > /dev/null&&python3 /root/AddParam.py /tmp/" + dirName + "/rec.m3u8&&zip a.zip rec* > /dev/null\"";
            status += StartExec(command);


            command = "ssh " + SSH_ARGS + " root@" + WEB_WORKER_IP + " \"" + REMOTE_EXEC_BEFORE + "mkdir -p " + webPath + "\"";
            status += StartExec(command);

            command = "ssh " + SSH_ARGS + " root@" + WEB_WORKER_IP + " \"" + REMOTE_EXEC_BEFORE + "rm -f " + webPath + "rec*\"";
            status += StartExec(command);


            command = "scp -B -q root@" + FFMPEG_WORKER_IP + ":/tmp/" + dirName + "/a.zip root@" + WEB_WORKER_IP + ":" + webPath + "a.zip";
            status += StartExec(command);


            command = "ssh " + SSH_ARGS + " root@" + WEB_WORKER_IP + " \"" + REMOTE_EXEC_BEFORE + "cd " + webPath + "&&unzip -o -qq a.zip 2>&1 > /dev/null&&rm a.zip\"";
            status += StartExec(command);


            command = "ssh " + SSH_ARGS + " root@" + FFMPEG_WORKER_IP + " \"" + REMOTE_EXEC_BEFORE + "rm -rf /tmp/" + dirName + "\"";
            status += StartExec(command);


            FileSaveDto.UploadDto uploadDto = new FileSaveDto.UploadDto();
            uploadDto.setLessonId(id);
            uploadDto.setPath(datePath + "/" + prefix);
            uploadWorkerRegister.getUploadQueue().add(uploadDto);


            lesson.setVideo(String.valueOf(date.getMonthValue())+date.getDayOfMonth() + "/" + prefix + "/" + "rec.m3u8");
            lessonMapper.updateById(lesson);
            if (status == 0) {
                return status;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Async
    public void MoveFile(String localFrom, String remoteTo, Long id, FileSaveDto fileSaveDto)
    {
        try {
            String command = "ssh " + SSH_ARGS + " root@" + WEB_WORKER_IP + " \"" + REMOTE_EXEC_BEFORE + "mkdir -p " + WEB_BASE_PATH + remoteTo.substring(0, remoteTo.lastIndexOf("/")) + "\"";
            int status = StartExec(command);

            command = "scp -B -q '" + localFrom + "' root@" + WEB_WORKER_IP + ":'" + WEB_BASE_PATH + remoteTo + "'" ;
            status += StartExec(command);


            FileSaveDto.UploadDto uploadDto = new FileSaveDto.UploadDto();
            uploadDto.setPath(remoteTo.substring(0, remoteTo.lastIndexOf("/")));
            uploadWorker.insUpload(uploadDto);
            Lesson lesson = lessonService.getById(id);
            List<FileSaveDto> files = JsonUtils.json2list(lesson.getFile(), FileSaveDto.class);
            files.add(fileSaveDto);
            lesson.setFile(JsonUtils.obj2json(files));
            lessonService.updateById(lesson);
            new File(localFrom).delete();
            if (status == 0)
            {
                return ;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void DeleteFile(String remoteTo)
    {
        try {
            String command = "ssh " + SSH_ARGS + " root@" + WEB_WORKER_IP + " \"" + REMOTE_EXEC_BEFORE + "rm -f " + WEB_BASE_PATH + remoteTo + "\"";
            int status = StartExec(command);


            FileSaveDto.UploadDto uploadDto = new FileSaveDto.UploadDto();
            uploadDto.setPath(remoteTo.substring(0, remoteTo.lastIndexOf("/")));
            uploadWorkerRegister.getUploadQueue().add(uploadDto);

            if (status == 0)
            {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void DeleteLesson(String prefix, LocalDate date)
    {
        try {
            String datePath = String.valueOf(date.getMonthValue()) + date.getDayOfMonth();
            String command = "ssh " + SSH_ARGS + " root@" + WEB_WORKER_IP + " \"" + REMOTE_EXEC_BEFORE + "rm -rf " + WEB_BASE_PATH + datePath + "/" + prefix + "\"";
            int status = StartExec(command);

            FileSaveDto.UploadDto uploadDto = new FileSaveDto.UploadDto();
            uploadDto.setPath(datePath);
            uploadWorkerRegister.getUploadQueue().add(uploadDto);

            if (status == 0)
            {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int StartExec(String command) throws IOException {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            System.out.println(command);
            Socket socket = new Socket(COMMAND_EXEC_SERVER, COMMAND_EXEC_PORT);
            socket.setSoTimeout(3600000);

            OutputStreamWriter os = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            PrintWriter pw = new PrintWriter(os);
            pw.write(command);
            pw.flush();


            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String reply = br.readLine();
            br.close();
            is.close();
            os.close();
            pw.close();
            socket.close();
            return reply.contains("success") ? 0 : 1;
        } catch (SocketException e) {
            e.printStackTrace();
            return 1;
        }
    }

    @Data
    public static class TranscodeWorkerRegister {

        private List<TranscodeDto> transcodeQueue = new ArrayList<>();
        private Map<String, String> tempFileMap = new HashMap<>();

        private int isWorking = 0;

        private static TranscodeWorkerRegister instance = new TranscodeWorkerRegister();

        public static TranscodeWorkerRegister getInstance()
        {
            return instance;
        }
    }
}


