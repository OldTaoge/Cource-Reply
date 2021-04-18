package space.oldtaoge.edu.junsen.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import space.oldtaoge.edu.junsen.common.JsonUtils;
import space.oldtaoge.edu.junsen.dto.DataTableDto;
import space.oldtaoge.edu.junsen.dto.FileSaveDto;
import space.oldtaoge.edu.junsen.dto.ResponseResult;
import space.oldtaoge.edu.junsen.entity.Lesson;
import space.oldtaoge.edu.junsen.mapper.LessonMapper;
import space.oldtaoge.edu.junsen.service.ILessonService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author OldTaoge
 * @since 2021-03-12
 */
@RestController
@RequestMapping("/lessons")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LessonController {

    @Resource
    private ILessonService lessonService;

    @RequestMapping(path = "/dtp", method = RequestMethod.GET)
    public DataTableDto getLessonsPager(
            @RequestParam(name = "draw") long draw,
            @RequestParam(name = "start") long start,
            @RequestParam(name = "length") long length)
    {
        Page<Lesson> page = new Page<>(start/length + 1, length);
        page.setDesc("id");
        page = lessonService.page(page);
        List<Lesson> records = page.getRecords();
//        records.forEach(e -> {
//            e.setVideo("<a href=\"play.html?id=" + e.getId() + "&path=" + e.getVideo() + "\">回放链接</a>");
//            e.setFile("<a href=\"file.html?id=" + e.getId() + "\">文件链接</a>");
//        });
        return new DataTableDto(draw, page.getTotal(), page.getTotal(),records, null);
    }

    @RequestMapping(path = "{id}", method = RequestMethod.GET)
    public ResponseResult<Lesson> getByID(
            @PathVariable("id") long id
    )
    {
        return new ResponseResult<>(ResponseResult.CodeStatus.OK, lessonService.getById(id));
    }

    @RequestMapping(method = RequestMethod.GET, path = "files/{id}")
    public DataTableDto getFilesByLesson(
            @PathVariable("id")Long id,
            @RequestParam(name = "draw") long draw
    )
    {
        Lesson lesson = lessonService.getById(id);

        try {
            List<FileSaveDto> files = JsonUtils.json2list(lesson.getFile(), FileSaveDto.class);
            return new DataTableDto(draw, files.size(), files.size(), files, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
