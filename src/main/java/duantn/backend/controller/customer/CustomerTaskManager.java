package duantn.backend.controller.customer;

import duantn.backend.authentication.CustomException;
import duantn.backend.model.dto.output.TransactionOutputDTO;
import duantn.backend.service.CommonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer")
public class CustomerTaskManager {
    final
    CommonService commonService;

    public CustomerTaskManager(CommonService commonService) {
        this.commonService = commonService;
    }

    //Lịch sử nạp tiền
    @GetMapping("/transaction")
    List<TransactionOutputDTO> listAllTransaction(HttpServletRequest request,
                                           @RequestParam Integer page,
                                           @RequestParam Integer limit,
                                           @RequestParam(required = false) Boolean type) throws CustomException {
        String email = (String) request.getAttribute("email");
        return commonService.listAllTransaction(email, page, limit, type);
    }
}
