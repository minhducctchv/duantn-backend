package duantn.backend.model.dto.output;

import duantn.backend.model.entity.Article;
import duantn.backend.model.entity.Staff;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

/**
 * Created with YourComputer.
 * User: DUC_PRO
 * Date: 28/04/2021
 * Time: 1:50 CH
 */

@Getter
@Setter
public class StaffArticleOutputDTO {
    private Integer id;

    private Map<String, Object> staff;

    private Map<String, Object> article;

    private Long time;

    private String action;
}
