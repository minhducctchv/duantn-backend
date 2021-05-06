package duantn.backend.model.dto.input;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class StaffPersonUpdateDTO {
    @Size(min = 3, max=50, message = "Tên phải có từ 3 đến 50 kí tự")
    @NotNull(message = "Tên không được null")
    private String name;

    @Size(min = 9, max = 12, message = "Số CMND phải có từ 9-12 kí tự")
    @NotNull(message = "CMND không được null")
    private String cardId;

    @NotNull(message = "Ngày sinh không được trống")
    private long birthday;

    @NotNull(message = "Giới tính không được trống")
    private boolean gender;

    @NotBlank(message = "Địa chỉ không được trống")
    @NotNull(message = "Địa chỉ không được null")
    private String address;

    @Size(min=9,max = 11,message = "SĐT phải có 9-11 kí tự")
    @NotNull(message = "SĐT không được null")
    private String phone;

    @NotBlank(message = "Ảnh không được trống")
    @NotNull(message = "Ảnh không được null")
    private String image;
}
