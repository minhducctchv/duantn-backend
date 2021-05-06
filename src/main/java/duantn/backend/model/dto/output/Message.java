package duantn.backend.model.dto.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    String mess;
    public Message(String mess){
        this.mess=mess;
    }
}
