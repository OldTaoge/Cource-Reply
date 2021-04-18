package space.oldtaoge.edu.junsen.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import space.oldtaoge.edu.junsen.entity.Lesson;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataTableDto {
    private long draw;
    private long recordsTotal;
    private long recordsFiltered;
    private List data;
    private String error;
}
