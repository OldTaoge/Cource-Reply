package space.oldtaoge.edu.junsen.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author OldTaoge
 * @since 2021-03-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("cms_lesson")
public class Lesson implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 上课日期
     */
    private LocalDate date;

    /**
     * 主要内容
     */
    private String content;

    /**
     * 课程名
     */
    private String subject;

    /**
     * 存储前缀
     */
    private String prefix;

    /**
     * 视频uri
     */
    private String video;

    /**
     * 附件JSON
     */
    private String file;


}
