package duantn.backend.service.impl;

import duantn.backend.authentication.CustomException;
import duantn.backend.component.MailSender;
import duantn.backend.dao.CustomerRepository;
import duantn.backend.dao.StaffRepository;
import duantn.backend.helper.Helper;
import duantn.backend.model.dto.input.StaffInsertDTO;
import duantn.backend.model.dto.input.StaffUpdateDTO;
import duantn.backend.model.dto.output.Message;
import duantn.backend.model.dto.output.StaffOutputDTO;
import duantn.backend.model.entity.Staff;
import duantn.backend.service.StaffService;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class StaffServiceImpl implements StaffService {
    final
    StaffRepository staffRepository;

    final
    PasswordEncoder passwordEncoder;

    final
    CustomerRepository customerRepository;

    final
    MailSender mailSender;

    final
    Helper helper;

    public StaffServiceImpl(StaffRepository staffRepository, PasswordEncoder passwordEncoder, CustomerRepository customerRepository, MailSender mailSender, Helper helper) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.mailSender = mailSender;
        this.helper = helper;
    }

    @Override
    public List<StaffOutputDTO> listStaff(String search, Boolean status, String sort,
                         Integer page, Integer limit) {
        if (search == null || search.trim().equals("")) search = "";
        Page<Staff> staffPage;
        if (sort == null || sort.equals("")) {
            if (status != null) {
                if (status)
                    staffPage = staffRepository.
                            findByNameLikeAndDeletedTrueAndEnabledTrueOrEmailLikeAndDeletedTrueAndEnabledTrueOrPhoneLikeAndDeletedTrueAndEnabledTrue(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit)
                            );
                else
                    staffPage = staffRepository.
                            findByNameLikeAndDeletedFalseAndEnabledTrueOrEmailLikeAndDeletedFalseAndEnabledTrueOrPhoneLikeAndDeletedFalseAndEnabledTrue(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit)
                            );
            } else
                staffPage = staffRepository.
                        findByNameLikeAndEnabledTrueOrEmailLikeAndEnabledTrueOrPhoneLikeAndEnabledTrue(
                                "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                PageRequest.of(page, limit)
                        );
        } else {
            if (sort.equalsIgnoreCase("desc")) {
                if (status != null) {
                    if (status)
                        staffPage = staffRepository.
                                findByNameLikeAndDeletedTrueAndEnabledTrueOrEmailLikeAndDeletedTrueAndEnabledTrueOrPhoneLikeAndDeletedTrueAndEnabledTrue(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by("name").descending())
                                );
                    else
                        staffPage = staffRepository.
                                findByNameLikeAndDeletedFalseAndEnabledTrueOrEmailLikeAndDeletedFalseAndEnabledTrueOrPhoneLikeAndDeletedFalseAndEnabledTrue(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by("name").descending())
                                );
                } else
                    staffPage = staffRepository.
                            findByNameLikeAndEnabledTrueOrEmailLikeAndEnabledTrueOrPhoneLikeAndEnabledTrue(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit, Sort.by("name").descending())
                            );
            } else {
                if (status != null) {
                    if (status)
                        staffPage = staffRepository.
                                findByNameLikeAndDeletedTrueAndEnabledTrueOrEmailLikeAndDeletedTrueAndEnabledTrueOrPhoneLikeAndDeletedTrueAndEnabledTrue(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by("name").ascending())
                                );
                    else
                        staffPage = staffRepository.
                                findByNameLikeAndDeletedFalseAndEnabledTrueOrEmailLikeAndDeletedFalseAndEnabledTrueOrPhoneLikeAndDeletedFalseAndEnabledTrue(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by("name").ascending())
                                );
                } else
                    staffPage = staffRepository.
                            findByNameLikeAndEnabledTrueOrEmailLikeAndEnabledTrueOrPhoneLikeAndEnabledTrue(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit, Sort.by("name").ascending())
                            );
            }

        }

        List<Staff> staffList = staffPage.toList();

        //convert sang StaffOutputDTO
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        List<StaffOutputDTO> staffOutputDTOList = new ArrayList<>();
        for (Staff staff : staffList) {
            StaffOutputDTO staffOutputDTO = modelMapper.map(staff, StaffOutputDTO.class);
            staffOutputDTO.setBirthday(staff.getDob().getTime());
            staffOutputDTO.setPages(staffPage.getTotalPages());
            staffOutputDTO.setElements(staffPage.getTotalElements());
            staffOutputDTOList.add(staffOutputDTO);
        }

        return staffOutputDTOList;
    }

    @Override
    public Message insertStaff(StaffInsertDTO staffInsertDTO, HttpServletRequest request) throws Exception {
        //validation
        if (customerRepository.findByEmail(staffInsertDTO.getEmail()) != null)
            throw new CustomException("Email đã được khách hàng sử dụng");
        if (staffRepository.findByEmail(staffInsertDTO.getEmail()) != null)
            throw new CustomException("Email đã được nhân viên sử dụng");
        String matchNumber = "[0-9]+";
        if (!staffInsertDTO.getCardId().matches(matchNumber))
            throw new CustomException("Số CMND phải là số");
        if (!staffInsertDTO.getPhone().matches(matchNumber))
            throw new CustomException("Số điện thoại phải là số");
        if (staffInsertDTO.getBirthday() >= System.currentTimeMillis())
            throw new CustomException("Ngày sinh phải trong quá khứ");

        //create token
        String token = helper.createToken(30);

        //insert
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Staff staff = modelMapper.map(staffInsertDTO, Staff.class);
            staff.setDob(new Date((staffInsertDTO.getBirthday())));
            staff.setPass(passwordEncoder.encode(staffInsertDTO.getPass()));
            staff.setToken(token);

            //StaffOutputDTO staffOutputDTO = modelMapper.map(newStaff, StaffOutputDTO.class);
            //staffOutputDTO.setBirthday(newStaff.getDob().getTime());

            //send mail
            try{
                mailSender.send(
                        staffInsertDTO.getEmail(),
                        "Xác nhận địa chỉ email",
                        "Click vào đường link sau để xác nhận email và kích hoạt tài khoản của bạn:<br/>" +
                                helper.getHostUrl(request.getRequestURL().toString(), "/super-admin") + "/confirm?token-customer=" + token
                                + "&email=" + staffInsertDTO.getEmail(),
                        "Thời hạn xác nhận email: 10 phút kể từ khi đăng kí"
                );
            }catch (Exception e){
                throw new CustomException("Lỗi, gửi mail thất bại");
            }

            Staff newStaff = staffRepository.save(staff);

            Thread deleteDisabledStaff = new Thread() {
                @SneakyThrows
                @Override
                public void run() {
                    Thread.sleep(10*60*1000);
                    Optional<Staff> optionalStaff=
                            staffRepository.findByStaffIdAndEnabledFalse(newStaff.getStaffId());
                    if(optionalStaff.isPresent())
                        staffRepository.delete(optionalStaff.get());
                }
            };
            deleteDisabledStaff.start();

            return new Message("Bạn hãy check mail để xác nhận, thời hạn 10 phút kể từ khi đăng kí");
        } catch (Exception e) {
            //e.printStackTrace();
            throw new CustomException("Thêm mới nhân viên thất bại");
        }
    }

    @Override
    public ResponseEntity<?> updateStaff(StaffUpdateDTO staffUpdateDTO, Integer id) throws CustomException {
        //validate
        String matchNumber = "[0-9]+";
        if (!staffUpdateDTO.getCardId().matches(matchNumber))
            throw new CustomException("Số CMND phải là số");
        if (!staffUpdateDTO.getPhone().matches(matchNumber))
            throw new CustomException("Số điện thoại phải là số");
        if (staffUpdateDTO.getBirthday() >= System.currentTimeMillis())
            throw new CustomException("Ngày sinh phải trong quá khứ");

        //update
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Optional<Staff> optionalStaff = staffRepository.findById(id);
            Staff staff = optionalStaff.get();
            staff.setName(staffUpdateDTO.getName());
            staff.setCardId(staffUpdateDTO.getCardId());
            staff.setDob(new Date(staffUpdateDTO.getBirthday()));
            staff.setGender(staffUpdateDTO.isGender());
            staff.setRole(staffUpdateDTO.isRole());
            staff.setAddress(staffUpdateDTO.getAddress());
            staff.setPhone(staffUpdateDTO.getPhone());
            staff.setImage(staffUpdateDTO.getImage());
            Staff newStaff = staffRepository.save(staff);
            StaffOutputDTO staffOutputDTO = modelMapper.map(newStaff, StaffOutputDTO.class);
            staffOutputDTO.setBirthday(newStaff.getDob().getTime());
            return ResponseEntity.ok(staffOutputDTO);
        } catch (Exception e) {
            //e.printStackTrace();
            throw new CustomException("Cập nhật nhân viên thất bại");
        }
    }

    @Override
    public Message blockStaff(Integer id, String email) throws CustomException {
        Staff staff = staffRepository.findByStaffIdAndDeletedFalseAndEnabledTrue(id);
        Staff superStaff = staffRepository.findByEmail(email);
        if (superStaff == null) throw new CustomException("Không tìm thấy super staff");
        if (staff == null) throw new CustomException("Lỗi: id " + id + " không tồn tại, hoặc đã bị block");
        else {

            //gửi mail
            SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            String title="Nhân viên: "+staff.getEmail()+" đã bị khóa tài khoản";
            String content="<p>Chúng tôi xin trân trọng thông báo.</p>\n" +
                    "<p>Nhân viên: <strong>"+staff.getName()+"</strong></p>\n" +
                    "<p>Tài khoản: <strong>"+staff.getEmail()+"</strong></p>\n" +
                    "<p>Đã bị <span style=\"color: rgb(184, 49, 47);\"><strong>khóa </strong></span>tài khoản, bởi:</p>\n" +
                    "<p>Quản lý: <strong>"+superStaff.getName()+"</strong></p>\n" +
                    "<p>Email: <strong>"+email+"</strong></p>\n" +
                    "<p>Vào lúc: <strong><em>"+sdf.format(new Date())+"</em></strong></p>";
            String note="Nếu có thắc mắc ý kiến bạn hãy liên hệ với quản lý qua email: "+email;
            try{
                mailSender.send(staff.getEmail(), title, content, note);
            }catch (Exception e){
                throw new CustomException("Lỗi, gửi mail thất bại");
            }

            staff.setDeleted(true);
            staffRepository.save(staff);
            return new Message("Block nhân viên id " + id + " thành công");
        }
    }

    @Override
    public Message activeStaff(Integer id, String email) throws CustomException {
        Optional<Staff> optionalStaff = staffRepository.findById(id);
        Staff superStaff=staffRepository.findByEmail(email);
        if (superStaff==null) throw new CustomException("Không tìm thấy super staff");
        if (!optionalStaff.isPresent()) throw new CustomException("Lỗi: id " + id + " không tồn tại");
        else {
            Staff staff=optionalStaff.get();
            if(staff.getDeleted()==false) throw new CustomException("Chỉ kích hoạt lại khi tài khoản bị khóa");
            //gửi mail
            SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            String title="Nhân viên: "+staff.getEmail()+" đã được kích hoạt lại tài khoản";
            String content="<p>Chúng tôi xin trân trọng thông báo.</p>\n" +
                    "<p>Nhân viên: <strong>"+staff.getName()+"</strong></p>\n" +
                    "<p>Tài khoản: <strong>"+staff.getEmail()+"</strong></p>\n" +
                    "<p>Đã được <span style=\"color: rgb(184, 49, 47);\"><strong>kích hoạt </strong></span>tài khoản, bởi:</p>\n" +
                    "<p>Quản lý: <strong>"+superStaff.getName()+"</strong></p>\n" +
                    "<p>Email: <strong>"+email+"</strong></p>\n" +
                    "<p>Vào lúc: <strong><em>"+sdf.format(new Date())+"</em></strong></p>";
            String note="Nếu có thắc mắc ý kiến bạn hãy liên hệ với quản lý qua email: "+email;
            try{
                mailSender.send(staff.getEmail(), title, content, note);
            }catch (Exception e){
                throw new CustomException("Lỗi, gửi mail thất bại");
            }

            staff.setDeleted(false);
            staffRepository.save(staff);
            return new Message("Kích hoạt nhân viên id: " + id + " thành công");
        }
    }

    @Override
    public ResponseEntity<?> findOneStaff(Integer id) {
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Staff newStaff = staffRepository.findById(id).get();
            StaffOutputDTO staffOutputDTO = modelMapper.map(newStaff, StaffOutputDTO.class);
            staffOutputDTO.setBirthday(newStaff.getDob().getTime());
            return ResponseEntity.ok(staffOutputDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Message("Lỗi: nhân viên id " + id + " không tồn tại"));
        }
    }

    @Override
    public Message deleteAllStaffs() {
        List<Staff> staffList = staffRepository.findByDeletedTrueAndEnabledTrue();
        for (Staff staff : staffList) {
            staffRepository.delete(staff);
        }
        return new Message("Xóa tất cả nhân viên bị xóa mềm thành công");
    }

    @Override
    public Message deleteStaffs(Integer id) throws CustomException {
        Staff staff=staffRepository.
                findByEnabledTrueAndStaffId(id);
        if(staff==null) throw new CustomException("Nhân viên id: "+id+" không tồn tại");
        staffRepository.delete(staff);
        return new Message("Xóa nhân viên "+id+" thành công");
    }
}
