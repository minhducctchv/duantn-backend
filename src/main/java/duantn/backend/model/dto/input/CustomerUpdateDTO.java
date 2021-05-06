package duantn.backend.model.dto.input;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CustomerUpdateDTO {
    @Size(min = 3, max=50, message = "Tên phải có từ 3 đến 50 kí tự")
    @NotNull(message = "Tên không được trống")
    private String name;

    @NotNull(message = "Giới tính không được trống")
    private boolean gender;

    private String address;

    @Size(min=9,max = 11,message = "SĐT phải có 9-11 kí tự")
    @NotNull(message = "Số điện thoại không được null")
    private String phone;

    private String cardId;

    @NotNull(message = "Ngày sinh không được trống")
    private long birthday;

    @NotBlank(message = "Ảnh không được trống")
    @NotNull(message = "Ảnh không được null")
    private String image;
}
