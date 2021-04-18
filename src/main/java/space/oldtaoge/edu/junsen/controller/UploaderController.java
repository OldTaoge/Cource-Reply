package space.oldtaoge.edu.junsen.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import space.oldtaoge.edu.junsen.common.JsonUtils;
import space.oldtaoge.edu.junsen.dto.FileSaveDto;
import space.oldtaoge.edu.junsen.dto.TranscodeDto;
import space.oldtaoge.edu.junsen.entity.Lesson;
import space.oldtaoge.edu.junsen.service.ILessonService;
import space.oldtaoge.edu.junsen.worker.TranscodeWorker;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin("*")
@RequestMapping("/upload")
public class UploaderController {
    private TranscodeWorker.TranscodeWorkerRegister transcodeReg = TranscodeWorker.TranscodeWorkerRegister.getInstance();

    @Resource
    private ILessonService lessonService;

    @Resource
    private TranscodeWorker transcodeWorker;

    
    @RequestMapping(method = RequestMethod.POST, path = "media")
    public String MediaUpload(@RequestParam("file")MultipartFile files)
    {
        if (files.isEmpty()) {
            return "请上传文件";
        }
        String filename = files.getOriginalFilename();

        try {
            File tempFile = File.createTempFile("JavaAPP-Upload", filename.substring(filename.lastIndexOf(".")));
            files.transferTo(tempFile);
            String uuid = UUID.randomUUID().toString();

            transcodeReg.getTempFileMap().put(uuid, tempFile.getAbsolutePath());
            return "{\"status\": \"success\", \"flag\": \"" + uuid + "\"}";

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, path = "form")
    public String FormConfirm(
            @RequestParam("date")String date,
            @RequestParam("subject")String subject,
            @RequestParam("content")String content,
            @RequestParam("id")Long id,
            @RequestParam("mediaFlag")String mediaFlag,
            HttpServletResponse response
            )
    {
        Lesson lesson;
        if (id != 0L)
        {
            //修改
            lesson = lessonService.getById(id);
            lesson.setDate(LocalDate.parse(date));
            lesson.setSubject(subject);
            lesson.setContent(content);
        }
        else
        {
            //新建
            lesson = new Lesson();
            lesson.setFile("[]");
            lesson.setDate(LocalDate.parse(date));
            lesson.setVideo("none");
            lesson.setSubject(subject);
            lesson.setContent(content);
            lesson.setPrefix(UUID.randomUUID().toString());
        }
        lessonService.saveOrUpdate(lesson);
        if (!mediaFlag.equals("notModify"))
        {
            //修改视频
            lesson.setVideo("transcoding");
            TranscodeDto transcodeDto = new TranscodeDto();
            transcodeDto.setId(lesson.getId());
            transcodeDto.setVideoFile(transcodeReg.getTempFileMap().get(mediaFlag));
            transcodeReg.getTranscodeQueue().add(transcodeDto);
            transcodeWorker.GoWork();
            lessonService.updateById(lesson);
        }

        return "{\"status\": \"success\"}";
    }

    
    @RequestMapping(path = "file/{id}", method = RequestMethod.POST)
    synchronized public String FileUpload(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file,
            HttpServletResponse response
    )
    {
        if (file.isEmpty()) {
            return "请上传文件";
        }
        Lesson lesson = lessonService.getById(id);
//        String savePathPrefix = String.valueOf(lesson.getDate().getMonthValue()) + lesson.getDate().getDayOfMonth() ;
        String filename = file.getOriginalFilename().replace(" ", "");
        try {
            List<FileSaveDto> files = JsonUtils.json2list(lesson.getFile(), FileSaveDto.class);
            final int[] sameName = {0};
            files.forEach(t -> {
                if (t.getName().equals(filename))
                {
                    sameName[0] = 1;
                }
            });
            if (sameName[0] == 1)
            {
                return "{\"status\": \"success\"}";
            }
            File tempFile = File.createTempFile("JavaAPP-Upload", filename.substring(filename.lastIndexOf(".")));
            file.transferTo(tempFile);
            FileSaveDto fileDto = new FileSaveDto();
            fileDto.setName(filename);
            fileDto.setSize(file.getSize());
            fileDto.setPath(String.valueOf(lesson.getDate().getMonthValue())+lesson.getDate().getDayOfMonth() + "/" + lesson.getPrefix() + "/" + filename);

            transcodeWorker.MoveFile(tempFile.getAbsolutePath(), fileDto.getPath(), id, fileDto);

            return "{\"status\": \"success\"}";

        } catch (Exception e) {
            e.printStackTrace();
        }
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return "{\"status\": \"failure\"}";
    }


    @RequestMapping(path = "lessons/{id}", method = RequestMethod.DELETE)
    public String DeleteLesson(@PathVariable(name = "id")Long id)
    {
        Lesson lesson = lessonService.getById(id);
        try {
            transcodeWorker.DeleteLesson(lesson.getPrefix(), lesson.getDate());
            lessonService.removeById(lesson.getId());
            return "{\"status\": \"success\"}";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{\"status\": \"failure\"}";
    }

    /*@CrossOrigin("*")
    @RequestMapping(path = "lessons/{id}}", method = RequestMethod.DELETE)
    public String deleteLesson(@PathVariable("id") Long id)
    {

    }*/

    
    @RequestMapping(path = "file/{lessonId}/{fileId}", method = RequestMethod.DELETE)
    synchronized public String DeleteFile(
            @PathVariable("lessonId") Long lessonId,
            @PathVariable("fileId") int fileId)
    {
        Lesson lesson = lessonService.getById(lessonId);
        try {
            List<FileSaveDto> files = JsonUtils.json2list(lesson.getFile(), FileSaveDto.class);
            FileSaveDto removed = files.remove(fileId);
            transcodeWorker.DeleteFile(removed.getPath());
            lesson.setFile(JsonUtils.obj2json(files));
            lessonService.updateById(lesson);
            return "{\"status\": \"success\"}";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{\"status\": \"failure\"}";
    }

    @RequestMapping("test")
    public String test()
    {
        return "test";
    }

}
