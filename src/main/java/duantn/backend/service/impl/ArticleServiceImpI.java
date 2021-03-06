package duantn.backend.service.impl;

import duantn.backend.authentication.CustomException;
import duantn.backend.authentication.JwtUtil;
import duantn.backend.component.MailSender;
import duantn.backend.dao.*;
import duantn.backend.helper.Helper;
import duantn.backend.helper.VariableCommon;
import duantn.backend.model.dto.input.ArticleInsertDTO;
import duantn.backend.model.dto.input.ArticleUpdateDTO;
import duantn.backend.model.dto.input.ContactCustomerDTO;
import duantn.backend.model.dto.input.RoommateDTO;
import duantn.backend.model.dto.output.ArticleOutputDTO;
import duantn.backend.model.dto.output.Message;
import duantn.backend.model.entity.*;
import duantn.backend.service.ArticleService;
import io.jsonwebtoken.ExpiredJwtException;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ArticleServiceImpI implements ArticleService {
    final
    ArticleRepository articleRepository;

    final
    StaffArticleRepository staffArticleRepository;

    final
    MailSender mailSender;

    final
    JwtUtil jwtUtil;

    final
    StaffRepository staffRepository;

    final
    Helper helper;

    final
    WardRepository wardRepository;

    final
    FavoriteArticleRepository favoriteArticleRepository;

    final
    CommentRepository commentRepository;

    public ArticleServiceImpI(ArticleRepository articleRepository, StaffArticleRepository staffArticleRepository, MailSender mailSender, JwtUtil jwtUtil, StaffRepository staffRepository, Helper helper, WardRepository wardRepository, FavoriteArticleRepository favoriteArticleRepository, CommentRepository commentRepository) {
        this.articleRepository = articleRepository;
        this.staffArticleRepository = staffArticleRepository;
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
        this.staffRepository = staffRepository;
        this.helper = helper;
        this.wardRepository = wardRepository;
        this.favoriteArticleRepository = favoriteArticleRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public List<ArticleOutputDTO> listArticle(String sort, Long start, Long end, Integer ward, Integer district, Integer city, Boolean roommate, String status, Boolean vip, String search, Integer minAcreage, Integer maxAcreage, Integer minPrice, Integer maxPrice, Integer page, Integer limit) {
        List<ArticleOutputDTO> articleOutputDTOList = new ArrayList<>();
        List<Article> articleList =
                articleRepository.findCustom(sort, start, end, ward, district, city,
                        roommate, status, vip, search, minAcreage, maxAcreage, minPrice, maxPrice, page, limit);
        Map<String, Long> countMap = articleRepository.findCustomCount(
                start, end, ward, district, city,
                roommate, status, vip, search, minAcreage, maxAcreage, minPrice, maxPrice, limit
        );
        for (Article article : articleList) {
            ArticleOutputDTO articleOutputDTO = helper.convertToOutputDTO(article);
            articleOutputDTO.setElements(countMap.get("elements"));
            articleOutputDTO.setPages(countMap.get("pages"));
            articleOutputDTOList.add(articleOutputDTO);
        }
        return articleOutputDTOList;
    }

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    @Override
    public Message contactToCustomer(Integer id,
                                     ContactCustomerDTO contactCustomerDTO,
                                     HttpServletRequest request) throws CustomException {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Staff staff;
            try {
                staff = findStaffByJWT(request);
                if (staff == null) throw new Exception();
            } catch (ExpiredJwtException e) {
                throw new CustomException("JWT h???t h???n");
            } catch (Exception e) {
                throw new CustomException("JWT kh??ng h???p l???");
            }

            if (articleOptional.get().getCustomer() == null)
                throw new CustomException("Kh??ng x??c ?????nh ???????c kh??ch h??ng c???n li??n l???c");


            String to = articleOptional.get().getCustomer().getEmail();

            String note = "Nh??n vi??n li??n h???: " + staff.getName() + "<br/>"
                    + "Email: " + staff.getEmail() + "<br/>"
                    + "Th???i gian: " + simpleDateFormat.format(new Date());

            try {
                mailSender.send(to, contactCustomerDTO.getTitle(), contactCustomerDTO.getContent(), note);
            } catch (Exception e) {
                throw new CustomException("L???i, g???i mail th???t b???i");
            }

            //t???o b???n ghi staff article
            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setTime(new Date());
            staffArticle.setStaff(staff);
            staffArticle.setArticle(articleOptional.get());
            staffArticle.setAction("Li??n h??? v???i ng?????i ????ng b??i");
            staffArticleRepository.save(staffArticle);

            return new Message("G???i mail th??nh c??ng");
        } else throw new CustomException("B??i ????ng v???i id: " + id + " kh??ng t???n t???i");
    }

    @Override
    public Message activeArticle(Integer id, HttpServletRequest request) throws CustomException {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Staff staff;
            try {
                staff = findStaffByJWT(request);
                if (staff == null) throw new Exception();
            } catch (ExpiredJwtException e) {
                throw new CustomException("JWT h???t h???n");
            } catch (Exception e) {
                throw new CustomException("JWT kh??ng h???p l???");
            }

            //duy???t b??i
            //chuy???n deleted th??nh true
            Article article = articleOptional.get();
            if (!(article.getStatus().equals(VariableCommon.CHUA_DUYET) || article.getStatus().equals(VariableCommon.DA_SUA)))
                throw new CustomException("Ch??? ???????c duy???t b??i c?? tr???ng th??i l?? ch??a duy???t");

            //n???u l?? b??i ???? s???a
            if (article.getStatus().equals(VariableCommon.DA_SUA)&&(article.getExpTime()!=null)) {
                if (article.getExpTime().after(new Date())) {
                } else {
                    article.setStatus(VariableCommon.HET_HAN);
                    articleRepository.save(article);
                    throw new CustomException("B??i ????ng ???? h???t h???n");
                }
            } else {
                //t???o th???i h???n
                Integer days = null;
                if (article.getType().equals("day")) {
                    days = article.getNumber();
                    article.setExpTime(helper.addDayForDate(days, new Date()));
                } else if (article.getType().equals("week")) {
                    days = helper.calculateDays(article.getNumber(), article.getType(),
                            new Date());
                    article.setExpTime(helper.addDayForDate(days, new Date()));
                } else if (article.getType().equals("month")) {
                    days = helper.calculateDays(article.getNumber(), article.getType(),
                            new Date());
                    article.setExpTime(helper.addDayForDate(days, new Date()));
                } else throw new CustomException("Type c???a b??i ????ng b??? sai");
            }

            //set TimeUpdated
            article.setStatus(VariableCommon.DANG_DANG);
            article.setUpdateTime(new Date());
            article.setTimeGroup(0);


            //x??a to??n b??? comment,  y??u th??ch, ??i???m
            article.setPoint(0);
            List<Comment> comments = commentRepository.findByArticle(article);
            if (comments != null && comments.size() > 0) {
                for (Comment comment : comments) {
                    commentRepository.delete(comment);
                }
            }
            List<FavoriteArticle> favoriteArticles = favoriteArticleRepository.findByArticle(article);
            if (favoriteArticles != null && favoriteArticles.size() > 0) {
                for (FavoriteArticle favoriteArticle : favoriteArticles) {
                    favoriteArticleRepository.delete(favoriteArticle);
                }
            }

            //t???o b???n ghi staffArticle
            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setTime(new Date());
            staffArticle.setStaff(staff);
            staffArticle.setArticle(article);
            staffArticle.setAction("Duy???t b??i");

            //g???i th??
            String note = "Nh??n vi??n duy???t b??i: " + staff.getName() + "<br/>"
                    + "Email: " + staff.getEmail() + "<br/>"
                    + "Th???i gian: " + simpleDateFormat.format(new Date());
            String title = "B??i ????ng s???: " + article.getArticleId() + " ???? ???????c duy???t";
            String content = "<p>B??i ????ng s???: " + article.getArticleId() + "</p>\n" +
                    "\n" +
                    "<p>Ti??u ?????: " + article.getTitle() + "</p>\n" +
                    "\n" +
                    "<p>Ng?????i ????ng: " + article.getCustomer().getName() + "</p>\n" +
                    "\n" +
                    "<p>Email: " + article.getCustomer().getEmail() + "</p>\n" +
                    "\n" +
                    "<p>S??T: " + article.getCustomer().getPhone() + "</p>\n" +
                    "\n" +
                    "<p>Th???i gian ????ng: " + simpleDateFormat.format(article.getTimeCreated()) + "</p>\n" +
                    "\n" +
                    "\n" +
                    "<p>Th???i gian duy???t b??i: " + simpleDateFormat.format(new Date()) + "</p>\n" +
                    "\n" +
                    "<p>Tr???ng th??i: <strong><span style=\"color:#2980b9\">???? ???????c duy???t</span></strong></p>\n" +
                    "\n" +
                    "<p>Th???i gian h???t h???n (?????c t??nh): <span style=\"color:#c0392b\">" + simpleDateFormat.format(article.getExpTime()) + "</span></p>\n" +
                    "\n" +
                    "<p>B??i ????ng c???a b???n ???? ???????c nh??n vi??n <em><strong>" + staff.getName() + " </strong></em>(email: <em><strong>" + staff.getEmail() + "</strong></em>) duy???t v??o l??c <em><strong>" + simpleDateFormat.format(new Date()) + "</strong></em>.</p>\n" +
                    "\n" +
                    "<p>B???n c?? th??? v??o theo ???????ng d???n sau ????? xem b??i vi???t c???a m??nh:</p>\n" +
                    "\n" +
                    "<p> xxxx </p>\n";
            if (article.getCustomer() != null) {
                String to = article.getCustomer().getEmail();
                try {
                    mailSender.send(to, title, content, note);
                } catch (Exception e) {
                    throw new CustomException("L???i, g???i mail th???t b???i");
                }
            }

            //l??u
            staffArticleRepository.save(staffArticle);
            ArticleOutputDTO articleOutputDTO = helper.convertToOutputDTO(articleRepository.save(article));

            return new Message("Duy???t b??i th??nh c??ng");
        } else throw new CustomException("B??i ????ng v???i id: " + id + " kh??ng t???n t???i");
    }

    @Override
    public Message hiddenArticle(Integer id, String reason, HttpServletRequest request) throws CustomException {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Staff staff;
            try {
                staff = findStaffByJWT(request);
                if (staff == null) throw new Exception();
            } catch (ExpiredJwtException e) {
                throw new CustomException("JWT h???t h???n");
            } catch (Exception e) {
                throw new CustomException("JWT kh??ng h???p l???");
            }

            //duy???t b??i
            //chuy???n deleted th??nh true
            Article article = articleOptional.get();
            if (!article.getStatus().equals(VariableCommon.DANG_DANG))
                throw new CustomException("Ch??? c?? th??? ???n khi b??i vi???t ??ang ????ng");

            article.setStatus(VariableCommon.BI_AN);

            //t???o b???n ghi staffArticle
            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setTime(new Date());
            staffArticle.setStaff(staff);
            staffArticle.setArticle(article);
            staffArticle.setAction("???n b??i");

            //g???i th??
            if (article.getCustomer() != null) {
                if (reason == null || reason.trim().equals("")) reason = "kh??ng c?? l?? do c??? th???";
                String to = article.getCustomer().getEmail();
                String note = "Nh??n vi??n ???n b??i: " + staff.getName() + "<br/>"
                        + "Email: " + staff.getEmail() + "<br/>"
                        + "Th???i gian: " + simpleDateFormat.format(new Date());
                String title = "B??i ????ng s???: " + article.getArticleId() + " ???? b??? ???n";
                String content = "<p>B??i ????ng s???: " + article.getArticleId() + "</p>\n" +
                        "\n" +
                        "<p>Ti??u ?????: " + article.getTitle() + "</p>\n" +
                        "\n" +
                        "<p>Ng?????i ????ng: " + article.getCustomer().getName() + "</p>\n" +
                        "\n" +
                        "<p>Email: " + article.getCustomer().getEmail() + "</p>\n" +
                        "\n" +
                        "<p>S??T: " + article.getCustomer().getPhone() + "</p>\n" +
                        "\n" +
                        "<p>Th???i gian ???n b??i: " + simpleDateFormat.format(new Date()) + "</p>\n" +
                        "\n" +
                        "<p>Tr???ng th??i: <strong><span style=\"color:red\">???? b??? ???n</span></strong></p>\n" +
                        "\n" +
                        "<p>L?? do: <strong><span style=\"color:blue\">" + reason + "</span></strong></p>\n" +
                        "\n" +
                        "<p>B??i ????ng c???a b???n ???? b??? nh??n vi??n <em><strong>" + staff.getName() + " </strong></em>(email: <em><strong>" + staff.getEmail() + "</strong></em>) ???n v??o l??c <em><strong>" + simpleDateFormat.format(new Date()) + "</strong></em>.</p>\n" +
                        "\n" +
                        "<p>Ch??ng t??i r???t ti???c v??? ??i???u n??y, b???n vui l??ng xem l???i b??i ????ng c???a m??nh ???? ph?? h???p v???i n???i quy website ch??a. M???i th???c m???c xin li??n h??? theo email nh??n vi??n ???? duy???t b??i.</p>\n";
                try {
                    mailSender.send(to, title, content, note);
                } catch (Exception e) {
                    throw new CustomException("L???i, g???i mail th???t b???i");
                }
            }

            //l??u
            staffArticleRepository.save(staffArticle);
            ArticleOutputDTO articleOutputDTO = helper.convertToOutputDTO(articleRepository.save(article));

            return new Message("???n b??i th??nh c??ng");
        } else throw new CustomException("B??i ????ng v???i id: " + id + " kh??ng t???n t???i");
    }

    @Override
    public Message suggestCorrectingArticle(Integer id, String reason, HttpServletRequest request) throws CustomException {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Staff staff;
            try {
                staff = findStaffByJWT(request);
                if (staff == null) throw new Exception();
            } catch (ExpiredJwtException e) {
                throw new CustomException("JWT h???t h???n");
            } catch (Exception e) {
                throw new CustomException("JWT kh??ng h???p l???");
            }

            //duy???t b??i
            //chuy???n deleted th??nh true
            Article article = articleOptional.get();
            if (!article.getStatus().equals(VariableCommon.CHUA_DUYET) &&
                    !article.getStatus().equals(VariableCommon.DANG_DANG))
                throw new CustomException("Ch??? c?? ??p d???ng y??u c???u s???a l???i v???i b??i ch??a duy???t ho???c ??ang ????ng");

            article.setStatus(VariableCommon.SUA_LAI);

            //t???o b???n ghi staffArticle
            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setTime(new Date());
            staffArticle.setStaff(staff);
            staffArticle.setArticle(article);
            staffArticle.setAction("Y??u c???u s???a l???i b??i");

            //g???i th??
            if (article.getCustomer() != null) {
                if (reason == null || reason.trim().equals("")) reason = "kh??ng c?? l?? do c??? th???";
                String to = article.getCustomer().getEmail();
                String note = "Nh??n vi??n y??u c???u s???a b??i: " + staff.getName() + "<br/>"
                        + "Email: " + staff.getEmail() + "<br/>"
                        + "Th???i gian: " + simpleDateFormat.format(new Date());
                String title = "Y??u c???u s???a l???i b??i ????ng s???: " + article.getArticleId();
                String content = "<p>B??i ????ng s???: " + article.getArticleId() + "</p>\n" +
                        "\n" +
                        "<p>Ti??u ?????: " + article.getTitle() + "</p>\n" +
                        "\n" +
                        "<p>Ng?????i ????ng: " + article.getCustomer().getName() + "</p>\n" +
                        "\n" +
                        "<p>Email: " + article.getCustomer().getEmail() + "</p>\n" +
                        "\n" +
                        "<p>S??T: " + article.getCustomer().getPhone() + "</p>\n" +
                        "\n" +
                        "<p>Th???i gian y??u c???u: " + simpleDateFormat.format(new Date()) + "</p>\n" +
                        "\n" +
                        "<p>Tr???ng th??i: <strong><span style=\"color:red\">B??i vi???t c???n ???????c s???a l???i</span></strong></p>\n" +
                        "\n" +
                        "<p>L?? do: <strong><span style=\"color:blue\">" + reason + "</span></strong></p>\n" +
                        "\n" +
                        "<p>B??i ????ng c???a b???n ???? b??? nh??n vi??n <em><strong>" + staff.getName() + " </strong></em>(email: <em><strong>" + staff.getEmail() + "</strong></em>) y??u c???u s???a l???i, v??o l??c <em><strong>" + simpleDateFormat.format(new Date()) + "</strong></em>.</p>\n" +
                        "\n" +
                        "<p>Ch??ng t??i r???t ti???c v??? ??i???u n??y, b???n vui l??ng xem l???i b??i ????ng c???a m??nh ???? ph?? h???p v???i n???i quy website ch??a. M???i th???c m???c xin li??n h??? theo email nh??n vi??n ???? duy???t b??i.</p>\n";
                try {
                    mailSender.send(to, title, content, note);
                } catch (Exception e) {
                    throw new CustomException("L???i, g???i mail th???t b???i");
                }
            }

            //l??u
            staffArticleRepository.save(staffArticle);
            ArticleOutputDTO articleOutputDTO = helper.convertToOutputDTO(articleRepository.save(article));

            return new Message("Y??u c???u s???a l???i b??i th??nh c??ng");
        } else throw new CustomException("B??i ????ng v???i id: " + id + " kh??ng t???n t???i");
    }

    @Override
    public ArticleOutputDTO detailArticle(Integer id) throws CustomException {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (!articleOptional.isPresent())
            throw new CustomException("B??i ????ng v???i id: " + id + " kh??ng t???n t???i");

        Article article = articleOptional.get();
        return helper.convertToOutputDTO(article);
    }

    @Override
    public ArticleOutputDTO insertArticle(String email, ArticleInsertDTO articleInsertDTO) throws CustomException {
        Staff staff = staffRepository.findByEmail(email);
        if (staff == null) throw new CustomException("Admin ????ng b??i kh??ng h???p l???");

        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Article article = modelMapper.map(articleInsertDTO, Article.class);

            duantn.backend.model.entity.Service service = new duantn.backend.model.entity.Service();
            service.setElectricPrice(articleInsertDTO.getElectricPrice());
            service.setWaterPrice(articleInsertDTO.getWaterPrice());
            service.setWifiPrice(articleInsertDTO.getWifiPrice());
            article.setService(service);

            RoommateDTO roommateDTO = articleInsertDTO.getRoommateDTO();
            Roommate roommate = null;
            if (roommateDTO != null)
                roommate = modelMapper.map(roommateDTO, Roommate.class);
            article.setRoommate(roommate);

            Optional<Ward> wardOptional = wardRepository.findById(articleInsertDTO.getWardId());
            if (!wardOptional.isPresent()) throw new CustomException("Ward Id kh??ng h???p l???");
            article.setWard(wardOptional.get());

            article.setUpdateTime(new Date());
            article.setTimeGroup(0);
            article.setStatus(VariableCommon.DANG_DANG);
            article.setCustomer(null);

            //t???o h???n s??? d???ng
            Integer days = null;
            if (article.getType().equals("day")) {
                days = article.getNumber();
                article.setExpTime(helper.addDayForDate(days, new Date()));
            } else if (article.getType().equals("week")) {
                days = helper.calculateDays(article.getNumber(), article.getType(),
                        new Date());
                article.setExpTime(helper.addDayForDate(days, new Date()));
            } else if (article.getType().equals("month")) {
                days = helper.calculateDays(article.getNumber(), article.getType(),
                        new Date());
                article.setExpTime(helper.addDayForDate(days, new Date()));
            } else throw new CustomException("Type c???a b??i ????ng b??? sai");

            //l??u b??i
            Article newArticle = articleRepository.save(article);

            //nh??n vi??n ????ng b??i
            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setStaff(staff);
            staffArticle.setArticle(newArticle);
            staffArticle.setTime(new Date());
            staffArticle.setAction("????ng b??i");
            staffArticleRepository.save(staffArticle);

            return helper.convertToOutputDTO(newArticle);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("????ng b??i th???t b???i");
        }
    }

    @Override
    public ArticleOutputDTO updateArticle(String email, ArticleUpdateDTO articleUpdateDTO, Integer id) throws CustomException {
        Staff staff = staffRepository.findByEmail(email);
        if (staff == null) throw new CustomException("Kh??ng t??m th???y admin");
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);

            Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
            if (article == null) throw new CustomException("B??i ????ng id kh??ng h???p l???");
            if (article.getCustomer() != null)
                throw new CustomException("Admin ch??? ???????c s???a b??i do admin ????ng");

            article.setTitle(articleUpdateDTO.getTitle());
            article.setImage(articleUpdateDTO.getImage());
            article.setRoomPrice(articleUpdateDTO.getRoomPrice());
            article.setDescription(articleUpdateDTO.getDescription());

            article.setAddress(articleUpdateDTO.getAddress());
            article.setAcreage(articleUpdateDTO.getAcreage());
            article.setVideo(articleUpdateDTO.getVideo());

            duantn.backend.model.entity.Service service = article.getService();
            service.setElectricPrice(articleUpdateDTO.getElectricPrice());
            service.setWaterPrice(articleUpdateDTO.getWaterPrice());
            service.setWifiPrice(articleUpdateDTO.getWifiPrice());
            article.setService(service);

            RoommateDTO roommateDTO = articleUpdateDTO.getRoommateDTO();
            if (roommateDTO != null) {
                Roommate roommate = article.getRoommate();
                roommate.setDescription(roommateDTO.getDescription());
                roommate.setGender(roommateDTO.getGender());
                roommate.setQuantity(roommateDTO.getQuantity());
                article.setRoommate(roommate);
            } else article.setRoommate(null);

            Optional<Ward> wardOptional = wardRepository.findById(articleUpdateDTO.getWardId());
            if (!wardOptional.isPresent()) throw new CustomException("Ward Id kh??ng h???p l???");
            article.setWard(wardOptional.get());

            article.setUpdateTime(new Date());
            article.setTimeGroup(0);

            //x??a to??n b??? comment,  y??u th??ch, ??i???m
            article.setPoint(0);
            List<Comment> comments = commentRepository.findByArticle(article);
            if (comments != null && comments.size() > 0) {
                for (Comment comment : comments) {
                    commentRepository.delete(comment);
                }
            }
            List<FavoriteArticle> favoriteArticles = favoriteArticleRepository.findByArticle(article);
            if (favoriteArticles != null && favoriteArticles.size() > 0) {
                for (FavoriteArticle favoriteArticle : favoriteArticles) {
                    favoriteArticleRepository.delete(favoriteArticle);
                }
            }

            //l??u b??i
            Article newArticle = articleRepository.save(article);

            //t???o b???n ghi staffArticle
            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setStaff(staff);
            staffArticle.setArticle(newArticle);
            staffArticle.setTime(new Date());
            staffArticle.setAction("S???a b??i");
            staffArticleRepository.save(staffArticle);

            return helper.convertToOutputDTO(newArticle);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("C???p nh???t b??i th???t b???i");
        }
    }

    @Override
    public Message extensionExp(String email, Integer id, Integer date, String type) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null)
            throw new CustomException("B??i ????ng v???i id: " + id + " kh??ng t???n t???i");
        else if (!article.getStatus().equals(VariableCommon.DANG_DANG))
            throw new CustomException("Gia h???n ch??? ??p d???ng v???i b??i ????ng ???? ???????c duy???t");

        Staff staff = staffRepository.findByEmail(email);
        if (article.getCustomer() != null)
            throw new CustomException("Admin ch??? ???????c gia h???n b??i vi???t b??i vi???t do admin ????ng");

        //t???o th???i h???n
        if (article.getType().equals("day")) {
            article.setExpTime(helper.addDayForDate(date, article.getExpTime()));
        } else if (article.getType().equals("week") || article.getType().equals("month")) {
            Integer numberDay = helper.calculateDays(date, type, article.getExpTime());
            article.setExpTime(helper.addDayForDate(numberDay, article.getExpTime()));
        } else throw new CustomException("Type c???a b??i ????ng b??? sai");

        Article newArticle = articleRepository.save(article);

        //t???o b???n ghi staffArticle
        StaffArticle staffArticle = new StaffArticle();
        staffArticle.setArticle(newArticle);
        staffArticle.setStaff(staff);
        staffArticle.setTime(new Date());
        staffArticle.setAction("Gia h???n b??i ????ng th??m " + date + " " + type);
        staffArticleRepository.save(staffArticle);

        return new Message("Gia h???n b??i ????ng id: " + id + " th??nh c??ng");
    }

    @Override
    public Message postOldArticle(String email, Integer id, Integer date, String type, Boolean vip) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        Staff staff = staffRepository.findByEmail(email);
        if (staff == null)
            throw new CustomException("Nh??n vi??n kh??ng t???n t???i");
        if (article == null)
            throw new CustomException("B??i ????ng v???i id: " + id + " kh??ng t???n t???i, ho???c ??ang kh??ng b??? ???n");
        if (article.getCustomer() != null)
            throw new CustomException("Admin ch??? ???????c ????ng l???i b??i c?? do admin ????ng");
        if (article.getStatus().equals(VariableCommon.DANG_DANG) ||
                article.getStatus().equals(VariableCommon.CHUA_DUYET) ||
                article.getStatus().equals(VariableCommon.SUA_LAI))
            throw new CustomException("Ch??? ???????c ????ng l???i b??i ???? ???n ho???c h???t h???n");

        article.setVip(vip);

        article.setStatus(VariableCommon.DANG_DANG);

        article.setNumber(date);
        article.setType(type);

        article.setUpdateTime(new Date());
        article.setTimeGroup(0);

        //x??a to??n b??? comment,  y??u th??ch, ??i???m
        article.setPoint(0);
        List<Comment> comments = commentRepository.findByArticle(article);
        if (comments != null && comments.size() > 0) {
            for (Comment comment : comments) {
                commentRepository.delete(comment);
            }
        }
        List<FavoriteArticle> favoriteArticles = favoriteArticleRepository.findByArticle(article);
        if (favoriteArticles != null && favoriteArticles.size() > 0) {
            for (FavoriteArticle favoriteArticle : favoriteArticles) {
                favoriteArticleRepository.delete(favoriteArticle);
            }
        }

        //t???o th???i h???n
        Integer days = null;
        if (type.equals("day")) {
            article.setExpTime(helper.addDayForDate(date, new Date()));
        } else if (type.equals("week")) {
            days = helper.calculateDays(date, article.getType(),
                    new Date());
            article.setExpTime(helper.addDayForDate(days, new Date()));
        } else if (type.equals("month")) {
            days = helper.calculateDays(date, article.getType(),
                    new Date());
            article.setExpTime(helper.addDayForDate(days, new Date()));
        } else throw new CustomException("Type c???a b??i ????ng b??? sai");
        article.setNumber(date);
        article.setType(type);

        Article newArticle = articleRepository.save(article);

        //t???o b???n ghi staffArticle
        StaffArticle staffArticle = new StaffArticle();
        staffArticle.setStaff(staff);
        staffArticle.setArticle(newArticle);
        staffArticle.setTime(new Date());
        staffArticle.setAction("????ng l???i b??ng ????ng");
        staffArticleRepository.save(staffArticle);

        return new Message("????ng l???i b??i ????ng ???? ???n id: " + id + " th??nh c??ng");
    }

    @Override
    public Message buffPoint(String email, Integer id, Integer point) throws CustomException {
        Staff staff = staffRepository.findByEmail(email);
        if (staff == null) throw new CustomException("Nh??n vi??n kh??ng t???n t???i");
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null) throw new CustomException("B??i ????ng kh??ng t???n t???i");
        if (point < 0) throw new CustomException("S??? ??i???m nh??? nh???t l?? 0");

        article.setPoint(article.getPoint() + point);
        article.setUpdateTime(new Date());
        article.setTimeGroup(0);

        Article newArticle = articleRepository.save(article);

        //t???o staffArticle
        StaffArticle staffArticle = new StaffArticle();
        staffArticle.setArticle(newArticle);
        staffArticle.setStaff(staff);
        staffArticle.setTime(new Date());
        staffArticle.setAction("L??m m???i v?? t??ng cho b??i ????ng " + point + " ??i???m");
        staffArticleRepository.save(staffArticle);

        return new Message("B???n ???? l??m m???i v?? t??ng ??i???m th??nh c??ng, b??i ????ng hi???n c??: " + newArticle.getPoint() + " ??i???m");
    }

    private Staff findStaffByJWT(HttpServletRequest request) throws Exception {
        String jwt = extractJwtFromRequest(request);
        if (jwt == null || jwt.trim().equals("")) throw new CustomException("Kh??ng c?? JWT");
        String email = jwtUtil.getUsernameFromToken(jwt);
        if (email == null || email.trim().equals("")) throw new CustomException("JWT kh??ng h???p l???");
        Staff staff = staffRepository.findByEmail(email);
        if (staff == null) throw new CustomException("JWT kh??ng h???p l???");
        return staff;
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }

}
