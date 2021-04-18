package space.oldtaoge.edu.junsen.dto;

import lombok.Data;

@Data
public class FileSaveDto {
    private String name;
    private String path;
    private Long size;

    @Data
    public static class UploadDto {
        private String path;
        private Long lessonId = 0L;
        private String videoPath;
    }
}
