package duantn.backend.service.impl;

import duantn.backend.authentication.CustomException;
import duantn.backend.component.MailSender;
import duantn.backend.dao.CustomerRepository;
import duantn.backend.dao.StaffRepository;
import duantn.backend.model.dto.input.CustomerUpdateDTO;
import duantn.backend.model.dto.output.CustomerOutputDTO;
import duantn.backend.model.dto.output.Message;
import duantn.backend.model.entity.Customer;
import duantn.backend.model.entity.Staff;
import duantn.backend.service.CustomerService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {
    final
    CustomerRepository customerRepository;

    final
    PasswordEncoder passwordEncoder;

    final
    StaffRepository staffRepository;
    final
    MailSender mailSender;

    public CustomerServiceImpl(StaffRepository staffRepository, PasswordEncoder passwordEncoder, CustomerRepository customerRepository, MailSender mailSender) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.staffRepository = staffRepository;
        this.mailSender = mailSender;
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public List<CustomerOutputDTO> listCustomer(String search, Boolean deleted, String nameSort,
                                                String balanceSort, Integer page, Integer limit) {
        if (search == null || search.trim().equals("")) search = "";
        String sort = null;
        String sortBy = null;
        if (balanceSort != null && !balanceSort.trim().equals("")) {
            sort = balanceSort;
            sortBy = "accountBalance";
        } else if (nameSort != null && !nameSort.trim().equals("")) {
            sort = nameSort;
            sortBy = "name";
        }
        Page<Customer> customerPage;
        if (sort == null) {
            if (deleted != null) {
                if (deleted)
                    customerPage = customerRepository.
                            findByNameLikeAndEnabledTrueAndDeletedTrueOrPhoneLikeAndEnabledTrueAndDeletedTrueOrEmailLikeAndEnabledTrueAndDeletedTrue(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit)
                            );
                else
                    customerPage = customerRepository.
                            findByNameLikeAndEnabledTrueAndDeletedFalseOrPhoneLikeAndEnabledTrueAndDeletedFalseOrEmailLikeAndEnabledTrueAndDeletedFalse(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit)
                            );
            } else
                customerPage = customerRepository.
                        findByNameLikeAndEnabledTrueOrPhoneLikeAndEnabledTrueOrEmailLikeAndEnabledTrue(
                                "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                PageRequest.of(page, limit)
                        );
        } else {
            if (sort.equalsIgnoreCase("desc")) {
                if (deleted != null) {
                    if (deleted)
                        customerPage = customerRepository.
                                findByNameLikeAndEnabledTrueAndDeletedTrueOrPhoneLikeAndEnabledTrueAndDeletedTrueOrEmailLikeAndEnabledTrueAndDeletedTrue(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by(sortBy).descending())
                                );
                    else
                        customerPage = customerRepository.
                                findByNameLikeAndEnabledTrueAndDeletedFalseOrPhoneLikeAndEnabledTrueAndDeletedFalseOrEmailLikeAndEnabledTrueAndDeletedFalse(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by(sortBy).descending())
                                );
                } else
                    customerPage = customerRepository.
                            findByNameLikeAndEnabledTrueOrPhoneLikeAndEnabledTrueOrEmailLikeAndEnabledTrue(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit, Sort.by(sortBy).descending())
                            );
            } else {
                if (deleted != null) {
                    if (deleted)
                        customerPage = customerRepository.
                                findByNameLikeAndEnabledTrueAndDeletedTrueOrPhoneLikeAndEnabledTrueAndDeletedTrueOrEmailLikeAndEnabledTrueAndDeletedTrue(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by(sortBy).ascending())
                                );
                    else
                        customerPage = customerRepository.
                                findByNameLikeAndEnabledTrueAndDeletedFalseOrPhoneLikeAndEnabledTrueAndDeletedFalseOrEmailLikeAndEnabledTrueAndDeletedFalse(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by(sortBy).ascending())
                                );
                } else
                    customerPage = customerRepository.
                            findByNameLikeAndEnabledTrueOrPhoneLikeAndEnabledTrueOrEmailLikeAndEnabledTrue(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit, Sort.by(sortBy).ascending())
                            );
            }

        }

        List<Customer> customerList = customerPage.toList();

        //convert sang CustomerOutputDTO
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        List<CustomerOutputDTO> customerOutputDTOList = new ArrayList<>();
        for (Customer customer : customerList) {
            CustomerOutputDTO customerOutputDTO = modelMapper.map(customer, CustomerOutputDTO.class);
            if (customer.getDob() != null) customerOutputDTO.setBirthday(customer.getDob().getTime());
            customerOutputDTO.setPages(customerPage.getTotalPages());
            customerOutputDTO.setElements(customerPage.getTotalElements());
            customerOutputDTOList.add(customerOutputDTO);
        }

        return customerOutputDTOList;
    }

    @Override
    public ResponseEntity<?> updateCustomer(CustomerUpdateDTO customerUpdateDTO,
                                            Integer id) throws CustomException {
        //validate
        String matchNumber = "[0-9]+";
        if (customerUpdateDTO.getCardId() != null && !customerUpdateDTO.getCardId().equals("")) {
            if (!customerUpdateDTO.getCardId().matches(matchNumber))
                throw new CustomException("S??? CMND ph???i l?? s???");
            else if (customerUpdateDTO.getCardId().length() < 9 || customerUpdateDTO.getCardId().length() > 12)
                throw new CustomException("S??? CMND ph???i g???m 9-12 s???");
        }
        if (!customerUpdateDTO.getPhone().matches(matchNumber))
            throw new CustomException("S??? ??i???n tho???i ph???i l?? s???");
        if (customerUpdateDTO.getBirthday() >= System.currentTimeMillis())
            throw new CustomException("Ng??y sinh ph???i trong qu?? kh???");

        //update
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Optional<Customer> optionalCustomer = customerRepository.findById(id);
            Customer customer = optionalCustomer.get();
            customer.setName(customerUpdateDTO.getName());
            customer.setGender(customerUpdateDTO.isGender());
            customer.setAddress(customerUpdateDTO.getAddress());
            customer.setPhone(customerUpdateDTO.getPhone());
            customer.setCardId(customerUpdateDTO.getCardId());
            customer.setDob(new Date(customerUpdateDTO.getBirthday()));
            customer.setImage(customerUpdateDTO.getImage());
            Customer newCustomer = customerRepository.save(customer);
            CustomerOutputDTO customerOutputDTO = modelMapper.map(newCustomer, CustomerOutputDTO.class);
            if (customer.getDob() != null) customerOutputDTO.setBirthday(newCustomer.getDob().getTime());
            return ResponseEntity.ok(customerOutputDTO);
        } catch (Exception e) {
            //e.printStackTrace();
            throw new CustomException("C???p nh???t kh??ch h??ng th???t b???i");
        }
    }

    @Override
    public Message blockCustomer(Integer id, String email) throws CustomException {
        Customer customer = customerRepository.findByCustomerIdAndDeletedFalseAndEnabledTrue(id);
        Staff superStaff = staffRepository.findByEmail(email);
        if (superStaff == null) throw new CustomException("Kh??ng t??m th???y super staff");
        if (customer == null) throw new CustomException("L???i: id " + id + " kh??ng t???n t???i, ho???c ???? block r???i");
        else {
            //g???i mail
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            String title = "Kh??ch h??ng: " + customer.getEmail() + " ???? b??? kh??a t??i kho???n";
            String content = "<p>Ch??ng t??i xin tr??n tr???ng th??ng b??o.</p>\n" +
                    "<p>Kh??ch h??ng: <strong>" + customer.getName() + "</strong></p>\n" +
                    "<p>T??i kho???n: <strong>" + customer.getEmail() + "</strong></p>\n" +
                    "<p>???? b??? <span style=\"color: rgb(184, 49, 47);\"><strong>kh??a </strong></span>t??i kho???n, b???i:</p>\n" +
                    "<p>Nh??n vi??n: <strong>" + superStaff.getName() + "</strong></p>\n" +
                    "<p>Email: <strong>" + email + "</strong></p>\n" +
                    "<p>V??o l??c: <strong><em>" + sdf.format(new Date()) + "</em></strong></p>";
            String note = "N???u c?? th???c m???c ?? ki???n b???n h??y li??n h??? v???i nh??n vi??n qua email: " + email;
            try {
                mailSender.send(customer.getEmail(), title, content, note);
            } catch (Exception e) {
                throw new CustomException("L???i, g???i mail th???t b???i");
            }

            customer.setDeleted(true);
            customerRepository.save(customer);
            return new Message("Block kh??ch h??ng id " + id + " th??nh c??ng");
        }
    }

    @Override
    public Message activeCustomer(Integer id, String email) throws CustomException {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        Staff superStaff = staffRepository.findByEmail(email);
        if (superStaff == null) throw new CustomException("Kh??ng t??m th???y super staff");
        if (!optionalCustomer.isPresent()) throw new CustomException("L???i: id " + id + " kh??ng t???n t???i");
        else {
            Customer customer = optionalCustomer.get();
            if (customer.getDeleted() == false) throw new CustomException("Ch??? k??ch ho???t t??i kho???n khi n?? ???? b??? kh??a");
            //g???i mail
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            String title = "Kh??ch h??ng: " + customer.getEmail() + " ???? ???????c k??ch ho???t t??i kho???n";
            String content = "<p>Ch??ng t??i xin tr??n tr???ng th??ng b??o.</p>\n" +
                    "<p>Kh??ch h??ng: <strong>" + customer.getName() + "</strong></p>\n" +
                    "<p>T??i kho???n: <strong>" + customer.getEmail() + "</strong></p>\n" +
                    "<p>???? ???????c <span style=\"color: rgb(184, 49, 47);\"><strong>k??ch ho???t </strong></span>t??i kho???n, b???i:</p>\n" +
                    "<p>Nh??n vi??n: <strong>" + superStaff.getName() + "</strong></p>\n" +
                    "<p>Email: <strong>" + email + "</strong></p>\n" +
                    "<p>V??o l??c: <strong><em>" + sdf.format(new Date()) + "</em></strong></p>";
            String note = "N???u c?? th???c m???c ?? ki???n b???n h??y li??n h??? v???i nh??n vi??n qua email: " + email;
            try {
                mailSender.send(customer.getEmail(), title, content, note);
            } catch (Exception e) {
                throw new CustomException("L???i, g???i mail th???t b???i");
            }

            optionalCustomer.get().setDeleted(false);
            customerRepository.save(optionalCustomer.get());
            return new Message("K??ch ho???t kh??ch h??ng id: " + id + " th??nh c??ng");
        }
    }

    @Override
    public ResponseEntity<?> findOneCustomer(Integer id) {
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Customer customer = customerRepository.findById(id).get();
            CustomerOutputDTO customerOutputDTO = modelMapper.map(customer, CustomerOutputDTO.class);
            if (customer.getDob() != null) customerOutputDTO.setBirthday(customer.getDob().getTime());
            return ResponseEntity.ok(customerOutputDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Message("L???i: kh??ch h??ng id " + id + " kh??ng t???n t???i"));
        }
    }

    @Override
    public Message deleteAllCustomers() {
        List<Customer> customerList = customerRepository.findByDeletedTrueAndEnabledTrue();
        for (Customer customer : customerList) {
            customerRepository.delete(customer);
        }
        return new Message("X??a t???t c??? kh??ch h??ng b??? x??a m???m th??nh c??ng");
    }

    @Override
    public Message deleteCustomers(Integer id) throws CustomException {
        Customer customer = customerRepository.findByCustomerIdAndEnabledTrue(id);
        if (customer == null) throw new CustomException("Kh??ch h??ng v???i id " + id + " kh??ng t???n t???i");
        customerRepository.delete(customer);
        return new Message("X??a h??ch h??ng id " + id + " th??nh c??ng");
    }
}
