package duantn.backend.authentication;

import duantn.backend.dao.CustomerRepository;
import duantn.backend.dao.StaffRepository;
import duantn.backend.model.entity.Customer;
import duantn.backend.model.entity.Staff;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    final
    CustomerRepository customerRepository;

    final
    StaffRepository staffRepository;

    public CustomUserDetailsService(CustomerRepository customerRepository, StaffRepository staffRepository) {
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
    }

    //load user by email return user
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<SimpleGrantedAuthority> roles = null;
        Customer customer=null;
        Staff staff=null;
        customer=customerRepository.findByEmail(username);
        if(customer==null) staff=staffRepository.findByEmail(username);
        if(staff!=null)
        {
            if(staff.isRole()==true){
                roles=Arrays.asList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
                return new User(staff.getEmail(),staff.getPass(),roles);
            }else{
                roles=Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
                return new User(staff.getEmail(),staff.getPass(),roles);
            }
        }
        else if(customer !=null)
        {
            roles=Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
            return new User(customer.getEmail(),customer.getPass(),roles);
        }
        throw new UsernameNotFoundException("User not found with the name "+ username);
    }

}