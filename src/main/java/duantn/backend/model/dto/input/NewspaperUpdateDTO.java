package duantn.backend.model.dto.input;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class NewspaperUpdateDTO {
    @Size(min = 3, max = 225, message = "Tiêu đề phải có 3-225 kí tự")
    @NotNull(message = "Tiêu đề không được null")
    private String title;

    @Size(min = 100, message = "Nội dung phải có ít nhất 100 kí tự")
    @NotNull(message = "Nội dung không được null")
    private String content;

    @NotBlank(message = "Ảnh không được trống")
    @NotNull(message = "Ảnh không được null")
    private String image;

    @NotNull(message = "staff Id đăng bài không được trống")
    private Integer staffId;
}
