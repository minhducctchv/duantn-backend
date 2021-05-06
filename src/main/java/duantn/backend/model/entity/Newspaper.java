package duantn.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Newspaper extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer newId;

    @Column(nullable = false)
    private String title;

    @Column(length = 65535, columnDefinition = "text", nullable = false)
    private String content;

    @Column(columnDefinition = "text", nullable = false)
    private String image;

    @ManyToOne
    @JoinColumn(name = "staffId", nullable = false)
    private Staff staff;
}
