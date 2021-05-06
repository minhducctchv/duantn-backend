package duantn.backend.model.dto.input;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class RoommateDTO {
    @NotNull(message = "Giới tính không được null")
    private Boolean gender;

    @NotNull(message = "Số lượng người ở chung không được null")
    @Min(value = 1, message = "Số lượng người ở chung nhỏ nhất là 1")
    private Integer quantity;

    private String description;
}
