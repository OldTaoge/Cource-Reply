package space.oldtaoge.edu.junsen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * @since 2021-04-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SmsOption implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String optionName;

    private String optionValue;


}
