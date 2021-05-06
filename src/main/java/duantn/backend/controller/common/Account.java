package duantn.backend.controller.common;

import duantn.backend.authentication.CustomException;
import duantn.backend.authentication.CustomUserDetailsService;
import duantn.backend.authentication.JwtUtil;
import duantn.backend.dao.StaffRepository;
import duantn.backend.dao.TokenRepository;
import duantn.backend.model.dto.input.*;
import duantn.backend.model.dto.output.CustomerOutputDTO;
import duantn.backend.model.dto.output.Message;
import duantn.backend.model.dto.output.StaffOutputDTO;
import duantn.backend.model.entity.Token;
import duantn.backend.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
public class Account {
    final
    AccountService accountService;

    private final AuthenticationManager authenticationManager;

    private final CustomUserDetailsService userDetailsService;

    private final JwtUtil jwtTokenUtil;

    final
    StaffRepository staffRepository;

    final
    TokenRepository tokenRepository;

    public Account(AccountService accountService, AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService, JwtUtil jwtTokenUtil, StaffRepository staffRepository, TokenRepository tokenRepository) {
        this.accountService = accountService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.staffRepository = staffRepository;
        this.tokenRepository = tokenRepository;
    }


    @PostMapping("/sign-up")
    public Message customerSignup(@Valid @RequestBody SignupDTO signupDTO, HttpServletRequest request) throws CustomException {
        return accountService.customerSignup(signupDTO, request);
    }

    @GetMapping("/confirm")
    public Message confirmEmail(@RequestParam(value = "token-customer") String token,
                                @RequestParam String email) throws CustomException {
        return accountService.confirmEmail(token, email);
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginDTO loginDTO) throws Exception {
        //login danh cho khach hang ->role: customer
        //login danh cho nhan vien -> role: admin, super admin
        return accountService.login(loginDTO);
    }

    //Brear Token
    //isRefreshToken = true (Header)
    @PostMapping("/refreshtoken")
    public Map<String, String> refreshToken(@Valid @RequestBody String refreshtoken,
                                            HttpServletRequest request) throws CustomException {
        System.out.println(refreshtoken);
        Optional<Token> optionalToken=tokenRepository.findById(refreshtoken.trim());
        if(!optionalToken.isPresent())
            throw new CustomException("Refreshtoken không chính xác");
        if(optionalToken.get().getExpDate().before(new Date()))
            throw new CustomException("Refreshtoken đã hết hạn");
        request.setAttribute("userToken",
                optionalToken.get().getCustomer()==null? optionalToken.get().getStaff().getEmail():
                optionalToken.get().getCustomer().getEmail());
        return accountService.refreshtoken(request);
    }

    @GetMapping("/forgot")
    public Message forgotPassword(@RequestParam String email) throws CustomException {
        return accountService.forgotPassword(email);
    }

    @PostMapping("/reset-password")
    public Message resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) throws CustomException {
        return accountService.resetPassword(resetPasswordDTO);
    }

    @GetMapping("/admin/profile")
    public StaffOutputDTO staffProfile(HttpServletRequest request)
            throws CustomException {
        return accountService.staffDetail(request);
    }

    @PostMapping("/admin/update-profile")
    public StaffOutputDTO staffUpdateProfile(@Valid @RequestBody StaffPersonUpdateDTO staffPersonUpdateDTO,
                                             HttpServletRequest request)
            throws CustomException {
        return accountService.staffUpdateProfile(staffPersonUpdateDTO, request);
    }

    @GetMapping("/customer/profile")
    public CustomerOutputDTO customerProfile(HttpServletRequest request)
            throws CustomException {
        return accountService.customerProfile(request);
    }

    @PostMapping("/customer/update-profile")
    public CustomerOutputDTO customerUpdateProfile(@Valid @RequestBody CustomerUpdateDTO customerUpdateDTO,
                                                   HttpServletRequest request)
            throws CustomException {
        return accountService.customerUpdateProfile(customerUpdateDTO, request);
    }

    @PostMapping("/change-password")
    public Message changePassword(@Valid @RequestBody ChangePassDTO changePassDTO,
                                  HttpServletRequest request)
            throws CustomException {
        return accountService.changePassword(changePassDTO.getOldPass(),
                changePassDTO.getNewPass(), request);
    }

    @PostMapping("/customer/avatar")
    public Message avatar(@RequestBody String avatar, HttpServletRequest request)
         throws CustomException{
        String email= (String) request.getAttribute("email");
        return accountService.avatar(avatar, email);
    }

    @PostMapping("/admin/avatar")
    public Message avatarAdmin(@RequestBody String avatar, HttpServletRequest request)
            throws CustomException{
        String email= (String) request.getAttribute("email");
        return accountService.avatarStaff(avatar, email);
    }
}
