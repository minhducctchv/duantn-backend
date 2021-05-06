package duantn.backend.dao;

import duantn.backend.model.entity.Customer;
import duantn.backend.model.entity.Staff;
import duantn.backend.model.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Created with YourComputer.
 * Author: DUC_PRO
 * Date: 13/04/2021
 * Time: 10:53 CH
 */

public interface TokenRepository extends JpaRepository<Token, String> {
    Token findByStaff(Staff staff);
    Token findByCustomer(Customer customer);
}
