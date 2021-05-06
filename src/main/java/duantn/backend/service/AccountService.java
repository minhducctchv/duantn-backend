package duantn.backend.service;

import duantn.backend.authentication.CustomException;
import duantn.backend.model.dto.input.*;
import duantn.backend.model.dto.output.CustomerOutputDTO;
import duantn.backend.model.dto.output.Message;
import duantn.backend.model.dto.output.StaffOutputDTO;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface AccountService {
    Message customerSignup(SignupDTO signupDTO, HttpServletRequest request) throws CustomException;

    //sau nay chuyen sang void
    Message confirmEmail(String token, String email) throws CustomException;

    Map<String, String> login(LoginDTO loginDTO) throws Exception;

    Map<String, String> refreshtoken(HttpServletRequest request) throws CustomException;

    Message forgotPassword(String email) throws CustomException;

    Message resetPassword(ResetPasswordDTO resetPasswordDTO) throws CustomException;

    StaffOutputDTO staffDetail(HttpServletRequest request) throws CustomException;

    StaffOutputDTO staffUpdateProfile(StaffPersonUpdateDTO staffPersonUpdateDTO,
                                      HttpServletRequest request) throws CustomException;

    CustomerOutputDTO customerProfile(HttpServletRequest request) throws CustomException;

    CustomerOutputDTO customerUpdateProfile(CustomerUpdateDTO customerUpdateDTO,
                                            HttpServletRequest request) throws CustomException;

    Message changePassword(String oldPass, String newPass,
                           HttpServletRequest request) throws CustomException;

    Message avatar(String avatar, String email) throws CustomException;

    Message avatarStaff(String avatar, String email) throws CustomException;
}
