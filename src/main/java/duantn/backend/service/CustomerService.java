package duantn.backend.service;

import duantn.backend.authentication.CustomException;
import duantn.backend.model.dto.input.CustomerUpdateDTO;
import duantn.backend.model.dto.output.CustomerOutputDTO;
import duantn.backend.model.dto.output.Message;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CustomerService {
    //Tìm kiếm khách hàng = email, phone, name
    //sắp xếp theo name, balance =asc/desc (chỉ truyền 1 trong 2)
    //phân trang
    //nếu không truyền vào tham số thì trả về all list
    List<CustomerOutputDTO> listCustomer(String search, Boolean deleted, String nameSort,
                                         String balanceSort, Integer page, Integer limit);

    //    cập nhật thông tin khách hàng	Put/super-admin/customers
    ResponseEntity<?> updateCustomer(CustomerUpdateDTO customerUpdateDTO,
                                     Integer id) throws CustomException;

    //    block khách hàng	DELETE/super-admin/customers/{id}
    Message blockCustomer(Integer id, String email) throws CustomException;

    //    active khách hàng
    Message activeCustomer(Integer id, String email) throws CustomException;

    //    xem thông tin khách hàng	GET/super-admin/customers/{id}
    ResponseEntity<?> findOneCustomer(Integer id);

    //xóa cứng tất cả customer bị xóa mềm
    Message deleteAllCustomers();

    //xóa cứng 1 list (mảng Integer Id) khách hàng bị xóa mềm
    Message deleteCustomers(Integer id) throws CustomException;
}
