package duantn.backend.model.dto.output;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Created with YourComputer.
 * User: DUC_PRO
 * Date: 28/04/2021
 * Time: 4:55 CH
 */

@Getter
@Setter
public class CommentOutputDTO {
    private Integer commentId;
    private String comment;
    private Integer start;
    private Long time;
    private Map<String, Object> article;
    private Map<String, Object> customer;
}
