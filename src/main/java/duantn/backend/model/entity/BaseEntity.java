package duantn.backend.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;
import java.util.Date;

@MappedSuperclass
@Getter
@Setter
public class BaseEntity {

    private Boolean deleted = false;

    private Date timeCreated=new Date();

}
