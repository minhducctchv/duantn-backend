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
            throw new CustomException("Email ???? ???????c kh??ch h??ng s??? d???ng");
        if (staffRepository.findByEmail(staffInsertDTO.getEmail()) != null)
            throw new CustomException("Email ???? ???????c nh??n vi??n s??? d???ng");
        String matchNumber = "[0-9]+";
        if (!staffInsertDTO.getCardId().matches(matchNumber))
            throw new CustomException("S??? CMND ph???i l?? s???");
        if (!staffInsertDTO.getPhone().matches(matchNumber))
            throw new CustomException("S??? ??i???n tho???i ph???i l?? s???");
        if (staffInsertDTO.getBirthday() >= System.currentTimeMillis())
            throw new CustomException("Ng??y sinh ph???i trong qu?? kh???");

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
                        "X??c nh???n ?????a ch??? email",
                        "Click v??o ???????ng link sau ????? x??c nh???n email v?? k??ch ho???t t??i kho???n c???a b???n:<br/>" +
                                helper.getHostUrl(request.getRequestURL().toString(), "/super-admin") + "/confirm?token-customer=" + token
                                + "&email=" + staffInsertDTO.getEmail(),
                        "Th???i h???n x??c nh???n email: 10 ph??t k??? t??? khi ????ng k??"
                );
            }catch (Exception e){
                throw new CustomException("L???i, g???i mail th???t b???i");
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

            return new Message("B???n h??y check mail ????? x??c nh???n, th???i h???n 10 ph??t k??? t??? khi ????ng k??");
        } catch (Exception e) {
            //e.printStackTrace();
            throw new CustomException("Th??m m???i nh??n vi??n th???t b???i");
        }
    }

    @Override
    public ResponseEntity<?> updateStaff(StaffUpdateDTO staffUpdateDTO, Integer id) throws CustomException {
        //validate
        String matchNumber = "[0-9]+";
        if (!staffUpdateDTO.getCardId().matches(matchNumber))
            throw new CustomException("S??? CMND ph???i l?? s???");
        if (!staffUpdateDTO.getPhone().matches(matchNumber))
            throw new CustomException("S??? ??i???n tho???i ph???i l?? s???");
        if (staffUpdateDTO.getBirthday() >= System.currentTimeMillis())
            throw new CustomException("Ng??y sinh ph???i trong qu?? kh???");

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
            throw new CustomException("C???p nh???t nh??n vi??n th???t b???i");
        }
    }

    @Override
    public Message blockStaff(Integer id, String email) throws CustomException {
        Staff staff = staffRepository.findByStaffIdAndDeletedFalseAndEnabledTrue(id);
        Staff superStaff = staffRepository.findByEmail(email);
        if (superStaff == null) throw new CustomException("Kh??ng t??m th???y super staff");
        if (staff == null) throw new CustomException("L???i: id " + id + " kh??ng t???n t???i, ho???c ???? b??? block");
        else {

            //g???i mail
            SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            String title="Nh??n vi??n: "+staff.getEmail()+" ???? b??? kh??a t??i kho???n";
            String content="<p>Ch??ng t??i xin tr??n tr???ng th??ng b??o.</p>\n" +
                    "<p>Nh??n vi??n: <strong>"+staff.getName()+"</strong></p>\n" +
                    "<p>T??i kho???n: <strong>"+staff.getEmail()+"</strong></p>\n" +
                    "<p>???? b??? <span style=\"color: rgb(184, 49, 47);\"><strong>kh??a </strong></span>t??i kho???n, b???i:</p>\n" +
                    "<p>Qu???n l??: <strong>"+superStaff.getName()+"</strong></p>\n" +
                    "<p>Email: <strong>"+email+"</strong></p>\n" +
                    "<p>V??o l??c: <strong><em>"+sdf.format(new Date())+"</em></strong></p>";
            String note="N???u c?? th???c m???c ?? ki???n b???n h??y li??n h??? v???i qu???n l?? qua email: "+email;
            try{
                mailSender.send(staff.getEmail(), title, content, note);
            }catch (Exception e){
                throw new CustomException("L???i, g???i mail th???t b???i");
            }

            staff.setDeleted(true);
            staffRepository.save(staff);
            return new Message("Block nh??n vi??n id " + id + " th??nh c??ng");
        }
    }

    @Override
    public Message activeStaff(Integer id, String email) throws CustomException {
        Optional<Staff> optionalStaff = staffRepository.findById(id);
        Staff superStaff=staffRepository.findByEmail(email);
        if (superStaff==null) throw new CustomException("Kh??ng t??m th???y super staff");
        if (!optionalStaff.isPresent()) throw new CustomException("L???i: id " + id + " kh??ng t???n t???i");
        else {
            Staff staff=optionalStaff.get();
            if(staff.getDeleted()==false) throw new CustomException("Ch??? k??ch ho???t l???i khi t??i kho???n b??? kh??a");
            //g???i mail
            SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            String title="Nh??n vi??n: "+staff.getEmail()+" ???? ???????c k??ch ho???t l???i t??i kho???n";
            String content="<p>Ch??ng t??i xin tr??n tr???ng th??ng b??o.</p>\n" +
                    "<p>Nh??n vi??n: <strong>"+staff.getName()+"</strong></p>\n" +
                    "<p>T??i kho???n: <strong>"+staff.getEmail()+"</strong></p>\n" +
                    "<p>???? ???????c <span style=\"color: rgb(184, 49, 47);\"><strong>k??ch ho???t </strong></span>t??i kho???n, b???i:</p>\n" +
                    "<p>Qu???n l??: <strong>"+superStaff.getName()+"</strong></p>\n" +
                    "<p>Email: <strong>"+email+"</strong></p>\n" +
                    "<p>V??o l??c: <strong><em>"+sdf.format(new Date())+"</em></strong></p>";
            String note="N???u c?? th???c m???c ?? ki???n b???n h??y li??n h??? v???i qu???n l?? qua email: "+email;
            try{
                mailSender.send(staff.getEmail(), title, content, note);
            }catch (Exception e){
                throw new CustomException("L???i, g???i mail th???t b???i");
            }

            staff.setDeleted(false);
            staffRepository.save(staff);
            return new Message("K??ch ho???t nh??n vi??n id: " + id + " th??nh c??ng");
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
            return ResponseEntity.badRequest().body(new Message("L???i: nh??n vi??n id " + id + " kh??ng t???n t???i"));
        }
    }

    @Override
    public Message deleteAllStaffs() {
        List<Staff> staffList = staffRepository.findByDeletedTrueAndEnabledTrue();
        for (Staff staff : staffList) {
            staffRepository.delete(staff);
        }
        return new Message("X??a t???t c??? nh??n vi??n b??? x??a m???m th??nh c??ng");
    }

    @Override
    public Message deleteStaffs(Integer id) throws CustomException {
        Staff staff=staffRepository.
                findByEnabledTrueAndStaffId(id);
        if(staff==null) throw new CustomException("Nh??n vi??n id: "+id+" kh??ng t???n t???i");
        staffRepository.delete(staff);
        return new Message("X??a nh??n vi??n "+id+" th??nh c??ng");
    }
}
