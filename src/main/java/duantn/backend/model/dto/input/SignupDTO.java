package duantn.backend.model.dto.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupDTO {
    @Size(min = 3, max=50, message = "Tên phải có 3-50 kí tự")
    @NotNull(message = "Tên không được null")
    private String name;

    @NotNull(message = "Giới tính không được trống")
    private Boolean gender;

    @Email(message = "Email không đúng định dạng")
    @NotBlank(message = "Email không được trống")
    @NotNull(message = "Email không được null")
    private String email;

    @Size(min = 6, max = 30, message = "Mật khẩu phải có 6-30 kí tự")
    @NotNull(message = "Mật khẩu không được null")
    private String pass;

    @Size(min = 9, max = 11, message = "SĐT phải có 9-11 số")
    @NotNull(message = "SĐT không được null")
    private String phone;
}
