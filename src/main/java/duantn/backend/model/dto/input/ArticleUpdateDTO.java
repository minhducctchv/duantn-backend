package duantn.backend.model.dto.input;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class ArticleUpdateDTO {
    @Size(min = 3, max = 220, message = "Tiêu đề phải có từ 3-220 kí tự")
    @NotNull(message = "Tiêu đề không được trống")
    private String title;

    @NotBlank(message = "Ảnh không được trống")
    private String image;

    @Min(value = 1000, message = "Giá phòng nhỏ nhất là 1000 đồng")
    @NotNull(message = "Giá phòng không được trống")
    private Integer roomPrice;

    private String description;

    //numberDate

    @Min(value = 1000, message = "Giá nước nhỏ nhất là 1000 đồng")
    private Integer waterPrice;

    @Min(value = 1000, message = "Giá điện nhỏ nhất là 1000 đồng")
    private Integer electricPrice;

    @Min(value = 1000, message = "Giá wifi nhỏ nhất là 1000 đồng")
    private Integer wifiPrice;

    @NotNull(message = "Diện tích không được null")
    @Min(value = 5, message = "Diện tích nhỏ nhất là 5 m2")
    private Integer acreage;

    @NotNull(message = "Địa chỉ không được null")
    @Size(min = 3, message = "Địa chỉ phải có ít nhất 3 kí tự")
    private String address;

    private String video;

    //lưu ý
    private RoommateDTO roommateDTO;

    //token => customer (hoac cho nhap truc tiep)

    @NotNull(message = "Phường không được trống")
    private Integer wardId;
}
