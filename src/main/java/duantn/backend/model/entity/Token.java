package duantn.backend.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created with YourComputer.
 * Author: DUC_PRO
 * Date: 13/04/2021
 * Time: 10:39 CH
 */

@Entity
@Getter
@Setter
public class Token implements Serializable {
    @Id
    private String token=UUID.randomUUID().toString();

    @OneToOne
    @JoinColumn(name = "staffId", unique = true)
    private Staff staff;

    @OneToOne
    @JoinColumn(name = "customerId", unique = true)
    private Customer customer;

    @Column(nullable = false)
    private Date expDate;
}
