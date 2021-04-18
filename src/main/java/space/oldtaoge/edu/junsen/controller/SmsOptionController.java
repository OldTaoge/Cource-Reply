package space.oldtaoge.edu.junsen.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import space.oldtaoge.edu.junsen.dto.ResponseResult;
import space.oldtaoge.edu.junsen.entity.SmsOption;
import space.oldtaoge.edu.junsen.service.ISmsOptionService;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author OldTaoge
 * @since 2021-04-14
 */
@RestController
@RequestMapping("/option")
public class SmsOptionController {
    @Resource
    ISmsOptionService smsOptionService;

    @RequestMapping(method = RequestMethod.GET, path = "{option}")
    @CrossOrigin("*")
    public ResponseResult<SmsOption> getOption(@PathVariable("option") String optionName)
    {
        QueryWrapper<SmsOption> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("option_name", optionName);

        SmsOption option = smsOptionService.getOne(queryWrapper);
        if (option != null) {
            return new ResponseResult<>(ResponseResult.CodeStatus.OK, option);
        }
        return new ResponseResult<>(ResponseResult.CodeStatus.FAIL, "The server cannot find the requested resource");
    }

}
