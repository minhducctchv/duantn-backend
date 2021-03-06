package duantn.backend.service.impl;

import duantn.backend.authentication.CustomException;
import duantn.backend.authentication.CustomUserDetailsService;
import duantn.backend.authentication.JwtUtil;
import duantn.backend.component.MailSender;
import duantn.backend.dao.CustomerRepository;
import duantn.backend.dao.StaffRepository;
import duantn.backend.dao.TokenRepository;
import duantn.backend.helper.Helper;
import duantn.backend.model.dto.input.*;
import duantn.backend.model.dto.output.CustomerOutputDTO;
import duantn.backend.model.dto.output.Message;
import duantn.backend.model.dto.output.StaffOutputDTO;
import duantn.backend.model.entity.Customer;
import duantn.backend.model.entity.Staff;
import duantn.backend.model.entity.Token;
import duantn.backend.service.AccountService;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.Map.Entry;

@Service
public class AccountServiceImpl implements AccountService {
    final
    PasswordEncoder passwordEncoder;

    final
    CustomerRepository customerRepository;

    final
    MailSender mailSender;

    final
    StaffRepository staffRepository;

    final
    AuthenticationManager authenticationManager;
    final
    CustomUserDetailsService userDetailsService;
    final
    JwtUtil jwtTokenUtil;

    final
    Helper helper;

    final
    TokenRepository tokenRepository;

    public AccountServiceImpl(PasswordEncoder passwordEncoder, CustomerRepository customerRepository, MailSender mailSender, StaffRepository staffRepository, AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService, JwtUtil jwtTokenUtil, Helper helper, TokenRepository tokenRepository) {
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.mailSender = mailSender;
        this.staffRepository = staffRepository;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.helper = helper;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public Message customerSignup(SignupDTO signupDTO, HttpServletRequest request) throws CustomException {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        //validate
        String numberMatcher = "[0-9]+";
        if (!signupDTO.getPhone().matches(numberMatcher))
            throw new CustomException("S??? ??i???n tho???i ph???i l?? s???");
        if (customerRepository.findByEmail(signupDTO.getEmail()) != null
                || staffRepository.findByEmail(signupDTO.getEmail()) != null)
            throw new CustomException("Email ???? ???????c s??? d???ng");

        try {
            //create token
            String token = helper.createToken(30);

            //create customer
            Customer customer = modelMapper.map(signupDTO, Customer.class);
            customer.setAccountBalance(10000);
            customer.setPass(passwordEncoder.encode(signupDTO.getPass()));
            customer.setToken(token);

            //send mail
            try {
                mailSender.send(
                        signupDTO.getEmail(),
                        "X??c nh???n ?????a ch??? email",
                        "Click v??o ???????ng link sau ????? x??c nh???n email v?? k??ch ho???t t??i kho???n c???a b???n:<br/>" +
                                helper.getHostUrl(request.getRequestURL().toString(), "/sign-up") + "/confirm?token-customer=" + token
                                + "&email=" + signupDTO.getEmail(),
                        "Th???i h???n x??c nh???n, 10 ph??t k??? t??? khi ????ng k??"
                );
            } catch (Exception e) {
                throw new CustomException("L???i, g???i mail th???t b???i");
            }

            Customer newCustomer = customerRepository.save(customer);

            Thread deleteDisabledCustomer = new Thread() {
                @SneakyThrows
                @Override
                public void run() {
                    Thread.sleep(10 * 60 * 1000);
                    Optional<Customer> optionalCustomer =
                            customerRepository.findByCustomerIdAndEnabledFalse(newCustomer.getCustomerId());
                    if (optionalCustomer.isPresent())
                        customerRepository.delete(optionalCustomer.get());
                }
            };
            deleteDisabledCustomer.start();

            return new Message("B???n h??y check mail ????? x??c nh???n, trong v??ng 10 ph??t");
        } catch (Exception e) {
            throw new CustomException("????ng k?? th???t b???i");
        }
    }

    //v??? sau chuy???n th??nh void, return redirect
    @Override
    public Message confirmEmail(String token, String email) throws CustomException {
        Staff staff = null;
        Customer customer = customerRepository.findByToken(token);
        if (customer == null) staff = staffRepository.findByToken(token);
        if (staff != null) {
            if (!staff.getEmail().equals(email)) throw new CustomException("Email kh??ng ch??nh x??c");
            staff.setEnabled(true);
            staff.setToken(null);
            staffRepository.save(staff);

            //nen lam redirect
            return new Message("X??c nh???n email th??nh c??ng");
        } else if (customer != null) {
            if (!customer.getEmail().equals(email)) throw new CustomException("Email kh??ng ch??nh x??c");
            customer.setEnabled(true);
            customer.setToken(null);
            customerRepository.save(customer);

            //nen lam redirect
            return new Message("X??c nh???n email th??nh c??ng");
        } else throw new CustomException("X??c nh???n email th???t b???i");
    }

    @Override
    public Map<String, String> login(LoginDTO loginDTO) throws Exception {
        Map<String, String> returnMap = new HashMap<>();
        //validate
        if (customerRepository.findByEmail(loginDTO.getEmail()) == null) {
            if (staffRepository.findByEmail(loginDTO.getEmail()) == null) {
                throw new CustomException("Email kh??ng t???n t???i");
            } else if (!staffRepository.findByEmail(loginDTO.getEmail()).getEnabled())
                throw new CustomException("Email ch??a k??ch ho???t");
            else if (staffRepository.findByEmail(loginDTO.getEmail()).getDeleted())
                throw new CustomException("Nh??n vi??n ??ang b??? kh??a");
        } else if (!customerRepository.findByEmail(loginDTO.getEmail()).getEnabled())
            throw new CustomException("Email ch??a ???????c k??ch ho???t");
        else if (customerRepository.findByEmail(loginDTO.getEmail()).getDeleted())
            throw new CustomException("Kh??ch h??ng ??ang b??? kh??a");

        //login
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginDTO.getEmail(), loginDTO.getPass()));
        } catch (DisabledException e) {
            throw new Exception("Ng?????i d??ng v?? hi???u", e);
        } catch (BadCredentialsException e) {
            //throw new Exception("Bad credentials", e);
            throw new CustomException("M???t kh???u kh??ng ????ng");
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getEmail());

        final String token = jwtTokenUtil.generateToken(userDetails);

        //t???o refreshToken
        Staff staff = null;
        Customer customer = customerRepository.findByEmail(loginDTO.getEmail());
        if (customer == null)
            staff = staffRepository.findByEmail(loginDTO.getEmail());
        Token oldRefreshToken = null;
        if (staff != null) {
            oldRefreshToken = tokenRepository.findByStaff(staff);
        } else if (customer != null) {
            oldRefreshToken = tokenRepository.findByCustomer(customer);
        }
        if (oldRefreshToken != null) tokenRepository.delete(oldRefreshToken);
        Token refreshToken = new Token();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 10);
        refreshToken.setExpDate(calendar.getTime());

        String role;
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
            role = "SUPER_ADMIN";
        else if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
            role = "ADMIN";
        else role = "CUSTOMER";

        if (role.equalsIgnoreCase("SUPER_ADMIN") ||
                role.equalsIgnoreCase("ADMIN")) {

            refreshToken.setStaff(staff);

            returnMap.put("id", staff.getStaffId().toString());
            returnMap.put("name", staff.getName());
            returnMap.put("email", staff.getEmail());
            returnMap.put("image", staff.getImage());
        } else if (role.equalsIgnoreCase("CUSTOMER")) {

            refreshToken.setCustomer(customer);

            returnMap.put("id", customer.getCustomerId().toString());
            returnMap.put("name", customer.getName());
            returnMap.put("email", customer.getEmail());
            returnMap.put("image", customer.getImage());
            returnMap.put("balance", customer.getAccountBalance() + "");
        }

        returnMap.put("role", role);
        returnMap.put("token", token);

        Token newRefreshToken = tokenRepository.save(refreshToken);
        returnMap.put("refreshToken", newRefreshToken.getToken());

        return returnMap;
    }

    @Override
    public Map<String, String> refreshtoken(HttpServletRequest request) throws CustomException {
        try {
            // From the HttpRequest get the claims
            DefaultClaims claims = (io.jsonwebtoken.impl.DefaultClaims) request.getAttribute("claims");

            Map<String, Object> expectedMap = getMapFromIoJsonwebtokenClaims(claims);

            String userToken = (String) request.getAttribute("userToken");
            if (!expectedMap.get("sub").toString().equals(userToken))
                throw new CustomException("Refreshtoken kh??ng h???p l???");

            String token = jwtTokenUtil.doGenerateToken(expectedMap, expectedMap.get("sub").toString());
            Map<String, String> returnMap = new HashMap<>();
            returnMap.put("token", token);
            return returnMap;
        } catch (Exception e) {
            //e.printStackTrace();
            throw new CustomException("Refresh token th???t b???i");
        }
    }

    @Override
    public Message forgotPassword(String email) throws CustomException {
        Staff staff = null;
        staff = staffRepository.findByEmail(email);
        Customer customer = null;
        if (staff == null) customer = customerRepository.findByEmail(email);

        String token;

        if (staff != null) {
            if (!staff.getEnabled()) throw new CustomException("Email ch??a ???????c x??c nh???n");
            if (staff.getToken() != null)
                throw new CustomException("Email ?????i m???t kh???u ???? ???????c g???i, b???n h??y check l???i mail");
            token = helper.createToken(31);
            staff.setToken(token);

            try {
                senMail(email, token);
            } catch (Exception e) {
                throw new CustomException("L???i g???i mail th???t b???i");
            }

            staffRepository.save(staff);
        } else if (customer != null) {
            if (!customer.getEnabled()) throw new CustomException("Email ch??a ???????c x??c nh???n");
            if (customer.getToken() != null)
                throw new CustomException("Email ?????i m???t kh???u ???? ???????c g???i, b???n h??y check l???i mail");
            token = helper.createToken(31);
            customer.setToken(token);

            try {
                senMail(email, token);
            } catch (Exception e) {
                throw new CustomException("L???i g???i mail th???t b???i");
            }

            customerRepository.save(customer);
        } else {
            throw new CustomException("Email kh??ng t???n t???i");
        }

        return new Message("Th??nh c??ng, b???n h??y check mail ????? ti???p t???c");
    }

    private void senMail(String email, String token) {
        //send mail
        mailSender.send(
                email,
                "Qu??n m???t kh???u",
                "Click v??o ???????ng link sau ????? t???o m???i m???t kh???u c???a b???n:<br/>" +
                        "dia chi frontend" + "/renew-password?token=" + token
                        + "&email=" + email,
                "Ch??c b???n th??nh c??ng"
        );
    }

    @Override
    public Message resetPassword(ResetPasswordDTO resetPasswordDTO) throws CustomException {
        Staff staff = null;
        Customer customer = null;
        staff = staffRepository.findByToken(resetPasswordDTO.getToken());
        if (staff == null) customer = customerRepository.findByToken(resetPasswordDTO.getToken());
        if (staff != null) {
            if (!staff.getEmail().equals(resetPasswordDTO.getEmail()))
                throw new CustomException("Email kh??ng ch??nh x??c");
            staff.setPass(passwordEncoder.encode(resetPasswordDTO.getPassword()));
            staff.setToken(null);
            staffRepository.save(staff);
            return new Message("L??m m???i m???t kh???u th??nh c??ng");
        } else if (customer != null) {
            if (!customer.getEmail().equals(resetPasswordDTO.getEmail()))
                throw new CustomException("Email kh??ng ch??nh x??c");
            customer.setPass(passwordEncoder.encode(resetPasswordDTO.getPassword()));
            customer.setToken(null);
            customerRepository.save(customer);
            return new Message("L??m m???i m???t kh???u th??nh c???ng");
        } else throw new CustomException("L??m m???i m???t kh???u th???t b???i");
    }

    @Override
    public StaffOutputDTO staffDetail(HttpServletRequest request) throws CustomException {
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);

            String jwt = extractJwtFromRequest(request);
            String email = jwtTokenUtil.getUsernameFromToken(jwt);
            Staff newStaff = staffRepository.findByEmail(email);

            if (newStaff == null)
                throw new CustomException("Token kh??ng h???p l???");

            StaffOutputDTO staffOutputDTO = modelMapper.map(newStaff, StaffOutputDTO.class);
            staffOutputDTO.setBirthday(newStaff.getDob().getTime());
            return staffOutputDTO;
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            throw new CustomException("L???i: ng?????i d??ng kh??ng h???p l??? ho???c kh??ng t???n t???i");
        }
    }

    @Override
    public StaffOutputDTO staffUpdateProfile(StaffPersonUpdateDTO staffPersonUpdateDTO,
                                             HttpServletRequest request) throws CustomException {
        //validate
        String matchNumber = "[0-9]+";
        if (!staffPersonUpdateDTO.getCardId().matches(matchNumber))
            throw new CustomException("S??? CMND ph???i l?? s???");
        if (!staffPersonUpdateDTO.getPhone().matches(matchNumber))
            throw new CustomException("S??? ??i???n tho???i ph???i l?? s???");
        if (staffPersonUpdateDTO.getBirthday() >= System.currentTimeMillis())
            throw new CustomException("Ng??y sinh ph???i trong qu?? kh???");

        //update
        try {
            String jwt = extractJwtFromRequest(request);
            String email = jwtTokenUtil.getUsernameFromToken(jwt);
            Staff staff = staffRepository.findByEmail(email);
            if (staff == null) throw new CustomException("Token kh??ng h???p l???");

            staff.setName(staffPersonUpdateDTO.getName());
            staff.setCardId(staffPersonUpdateDTO.getCardId());
            staff.setDob(new Date(staffPersonUpdateDTO.getBirthday()));
            staff.setGender(staffPersonUpdateDTO.isGender());
            staff.setAddress(staffPersonUpdateDTO.getAddress());
            staff.setPhone(staffPersonUpdateDTO.getPhone());
            staff.setImage(staffPersonUpdateDTO.getImage());
            Staff newStaff = staffRepository.save(staff);

            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            StaffOutputDTO staffOutputDTO = modelMapper.map(newStaff, StaffOutputDTO.class);
            staffOutputDTO.setBirthday(newStaff.getDob().getTime());
            return staffOutputDTO;
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            //e.printStackTrace();
            throw new CustomException("C???p nh???t th??ng tin c?? nh??n th???t b???i");
        }
    }

    @Override
    public CustomerOutputDTO customerProfile(HttpServletRequest request) throws CustomException {
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);

            String jwt = extractJwtFromRequest(request);
            String email = jwtTokenUtil.getUsernameFromToken(jwt);
            Customer customer = customerRepository.findByEmail(email);


            if (customer == null)
                throw new CustomException("Token kh??ng h???p l???");

            CustomerOutputDTO customerOutputDTO = modelMapper.map(customer, CustomerOutputDTO.class);
            if (customer.getDob() != null) customerOutputDTO.setBirthday(customer.getDob().getTime());
            return customerOutputDTO;
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            throw new CustomException("L???i: ng?????i d??ng kh??ng h???p l??? ho???c kh??ng t???n t???i");
        }
    }

    @Override
    public CustomerOutputDTO customerUpdateProfile(CustomerUpdateDTO customerUpdateDTO, HttpServletRequest request) throws CustomException {
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
            String jwt = extractJwtFromRequest(request);
            String email = jwtTokenUtil.getUsernameFromToken(jwt);
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Customer customer = customerRepository.findByEmail(email);
            if (customer == null) throw new CustomException("Token kh??ng h???p l???");

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
            return customerOutputDTO;
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            //e.printStackTrace();
            throw new CustomException("C???p nh???t th??ng tin c?? nh??n th???t b???i");
        }
    }

    @Override
    public Message changePassword(String oldPass, String newPass,
                                  HttpServletRequest request) throws CustomException {
        try {
            String token = extractJwtFromRequest(request);
            String email = jwtTokenUtil.getUsernameFromToken(token);
            if (email == null || email.trim().equals(""))
                throw new CustomException("Token kh??ng h???p l???");
            Staff staff = null;
            Customer customer = customerRepository.findByEmail(email);
            if (customer == null) staff = staffRepository.findByEmail(email);
            if (staff != null) {
                if (staff.getPass() != oldPass) throw new CustomException("M???t kh???u c?? kh??ng ch??nh x??c");
                staff.setPass(newPass);
                staffRepository.save(staff);
                return new Message("?????i m???t kh???u cho nh??n vi??n: " + staff.getEmail() + " th??nh c??ng");
            } else if (customer != null) {
                if (customer.getPass() != oldPass)
                    throw new CustomException("M???t kh???u c?? kh??ng ch??nh x??c");
                customer.setPass(newPass);
                customerRepository.save(customer);
                return new Message("?????i m???t kh???u kh??ch h??ng: " + customer.getEmail() + "th??nh c??ng");
            } else throw new CustomException("Kh??ng t??m th???y ng?????i d??ng h???p l???");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            throw new CustomException("?????i m???t kh???u th???t b???i");
        }
    }

    @Override
    public Message avatar(String avatar, String email) throws CustomException {
        if (avatar == null || avatar.trim().equals("")) throw new CustomException("Link avatar kh??ng ???????c tr???ng");
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) throw new CustomException("Kh??ng t??m th???y kh??ch h??ng");
        customer.setImage(avatar);
        customerRepository.save(customer);
        return new Message("?????i avatar th??nh c??ng");
    }

    @Override
    public Message avatarStaff(String avatar, String email) throws CustomException {
        if (avatar == null || avatar.trim().equals("")) throw new CustomException("Link avatar kh??ng ???????c tr???ng");
        Staff staff = staffRepository.findByEmail(email);
        if (staff == null) throw new CustomException("Kh??ng t??m th???y nh??n vi??n");
        staff.setImage(avatar);
        staffRepository.save(staff);
        return new Message("?????i avatar th??nh c??ng");
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }

    public Map<String, Object> getMapFromIoJsonwebtokenClaims(DefaultClaims claims) {
        Map<String, Object> expectedMap = new HashMap<String, Object>();
        for (Entry<String, Object> entry : claims.entrySet()) {
            expectedMap.put(entry.getKey(), entry.getValue());
        }
        return expectedMap;
    }
}