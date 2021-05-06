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
public class Ward implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer wardId;

    @Column(nullable = false)
    private String wardName;

    @ManyToOne
    @JoinColumn(name = "districtId", nullable = false)
    private District district;

    @OneToMany(mappedBy = "ward", fetch = FetchType.LAZY)
    private Set<Article> articles;
}
