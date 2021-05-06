package duantn.backend.model.dto.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewspaperOutputDTO {
    private Integer newId;

    private String title;

    private String content;

    private String image;

    private String author;

    private Long updateTime;

    private Boolean deleted;

    private Long elements;
    private Integer pages;
}
