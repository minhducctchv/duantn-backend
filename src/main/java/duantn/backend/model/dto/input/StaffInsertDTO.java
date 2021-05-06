package duantn.backend.model.dto.input;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class StaffInsertDTO {

    @Email(message = "Email không đúng định dạng")
    @NotBlank(message = "Email không được trống")
    private String email;

    @Size(min = 6, max = 30, message = "Mật khẩu phải có 6-30 kí tự")
    @NotNull(message = "Mật khẩu không được null")
    private String pass;

    @Size(min = 3, max=50, message = "Tên phải có 3-50 kí tự")
    @NotNull(message = "Tên không được null")
    private String name;

    @Size(min = 9, max = 12, message = "Số CMND phải có 9-12 kí tự")
    @NotNull(message = "CMND không được null")
    private String cardId;

    @NotNull(message = "Ngày sinh không được trống")
    private long birthday;

    @NotNull(message = "Giới tính không được trống")
    private boolean gender;

    @NotNull(message = "Vai trò không được trống")
    private boolean role;

    @NotBlank(message = "Địa chỉ không được trống")
    @NotNull(message = "Địa chỉ không được null")
    private String address;

    @Size(min = 9, max = 11, message = "Số điện thoại phải có 9-11 kí tự")
    @NotNull(message = "SĐT không được null")
    private String phone;

    @NotNull(message = "Ảnh không được null")
    @NotBlank(message = "Ảnh không được trống")
    private String image;
}
