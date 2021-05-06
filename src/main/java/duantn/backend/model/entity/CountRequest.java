package duantn.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created with YourComputer.
 * User: DUC_PRO
 * Date: 28/04/2021
 * Time: 1:16 SA
 */

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CountRequest implements Serializable {
    @Id
    private Date date=new Date();

    @Column
    private Long count=0L;
}
