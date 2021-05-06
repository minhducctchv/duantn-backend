package duantn.backend.model.dto.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionOutputDTO {

    private Integer transactionId;

    private Long date;

    private String method;

    private int amount;

    private String description;

    private Long elements;
    private Integer pages;

}
