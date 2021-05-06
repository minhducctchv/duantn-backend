package duantn.backend.model.dto.input;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class ChangePassDTO {
    @NotNull(message = "Mật khẩu cũ không được trống")
    @Size(min = 6, max = 30, message = "Mật khẩu cũ phải có từ 6-30 kí tự")
    String oldPass;

    @NotNull(message = "Mật khẩu mới không được trống")
    @Size(min = 6, max = 30, message = "Mật khẩu mới phải có từ 6-30 kí tự")
    String newPass;
}
