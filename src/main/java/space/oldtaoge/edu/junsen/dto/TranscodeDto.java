package space.oldtaoge.edu.junsen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscodeDto {
    private Long id;
    private String videoFile;
}
