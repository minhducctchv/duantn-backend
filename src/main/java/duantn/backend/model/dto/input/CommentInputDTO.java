package duantn.backend.model.dto.input;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created with YourComputer.
 * User: DUC_PRO
 * Date: 28/04/2021
 * Time: 4:52 CH
 */

@Getter
@Setter
public class CommentInputDTO {
    private String comment;

    @NotNull(message = "Số sao không được null")
    @Min(value = 1, message = "Tối thiểu là 1 sao")
    @Max(value = 5, message = "Tối đa là 5 sao")
    private Integer start;

    @NotNull(message = "Article Id không được null")
    private Integer articleId;
}
