package duantn.backend.model.dto.output;

import duantn.backend.model.entity.Roommate;
import duantn.backend.model.entity.Service;
import duantn.backend.model.entity.Ward;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ArticleOutputDTO {
    private Integer articleId;

    private String title;

    private String image;

    private Integer roomPrice;

    private String description;

    private Long createTime;

    private Long lastUpdateTime;

    private Long expDate;

    private Boolean vip;

    private String status;

    private Service service;

    private Roommate roommate;

    private Integer acreage;

    private String address;

    private String video;

    private Map<String, String> customer;

    private Map<String, String> location;
    //private Ward ward;

    private Integer number;
    private String type;
    private Integer price;
    private Integer amount;

    private Long elements;
    private Long pages;

    private Long countLike;
    private Boolean liked=false;

    private Integer point;
}
