package duantn.backend.model.dto.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffOutputDTO {
    private Integer staffId;

    private String email;

    private String name;

    private String cardId;

    private Long birthday;

    private Boolean gender;

    private Boolean role;

    private Boolean deleted;

    private String address;

    private String phone;

    private String image;

    private Long elements;
    private Integer pages;
}
