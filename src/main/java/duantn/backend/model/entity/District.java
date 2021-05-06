package duantn.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class District implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer districtId;

    @Column(nullable = false)
    private String districtName;


    @ManyToOne
    @JoinColumn(name = "cityId", nullable = false)
    private City city;

    @OneToMany(mappedBy = "district", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Ward> wards;
}
