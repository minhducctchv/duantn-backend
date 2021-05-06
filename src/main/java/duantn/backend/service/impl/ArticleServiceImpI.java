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
                throw new CustomException("JWT hết hạn");
            } catch (Exception e) {
                throw new CustomException("JWT không hợp lệ");
            }

            if (articleOptional.get().getCustomer() == null)
                throw new CustomException("Không xác định được khách hàng cần liên lạc");


            String to = articleOptional.get().getCustomer().getEmail();

            String note = "Nhân viên liên hệ: " + staff.getName() + "<br/>"
                    + "Email: " + staff.getEmail() + "<br/>"
                    + "Thời gian: " + simpleDateFormat.format(new Date());

            try {
                mailSender.send(to, contactCustomerDTO.getTitle(), contactCustomerDTO.getContent(), note);
            } catch (Exception e) {
                throw new CustomException("Lỗi, gửi mail thất bại");
            }

            //tạo bản ghi staff article
            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setTime(new Date());
            staffArticle.setStaff(staff);
            staffArticle.setArticle(articleOptional.get());
            staffArticle.setAction("Liên hệ với người đăng bài");
            staffArticleRepository.save(staffArticle);

            return new Message("Gửi mail thành công");
        } else throw new CustomException("Bài đăng với id: " + id + " không tồn tại");
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
                throw new CustomException("JWT hết hạn");
            } catch (Exception e) {
                throw new CustomException("JWT không hợp lệ");
            }

            //duyệt bài
            //chuyển deleted thành true
            Article article = articleOptional.get();
            if (!(article.getStatus().equals(VariableCommon.CHUA_DUYET) || article.getStatus().equals(VariableCommon.DA_SUA)))
                throw new CustomException("Chỉ được duyệt bài có trạng thái là chưa duyệt");

            //nếu là bài đã sửa
            if (article.getStatus().equals(VariableCommon.DA_SUA)&&(article.getExpTime()!=null)) {
                if (article.getExpTime().after(new Date())) {
                } else {
                    article.setStatus(VariableCommon.HET_HAN);
                    articleRepository.save(article);
                    throw new CustomException("Bài đăng đã hết hạn");
                }
            } else {
                //tạo thời hạn
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
                } else throw new CustomException("Type của bài đăng bị sai");
            }

            //set TimeUpdated
            article.setStatus(VariableCommon.DANG_DANG);
            article.setUpdateTime(new Date());
            article.setTimeGroup(0);


            //xóa toàn bộ comment,  yêu thích, điểm
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

            //tạo bản ghi staffArticle
            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setTime(new Date());
            staffArticle.setStaff(staff);
            staffArticle.setArticle(article);
            staffArticle.setAction("Duyệt bài");

            //gửi thư
            String note = "Nhân viên duyệt bài: " + staff.getName() + "<br/>"
                    + "Email: " + staff.getEmail() + "<br/>"
                    + "Thời gian: " + simpleDateFormat.format(new Date());
            String title = "Bài đăng số: " + article.getArticleId() + " đã được duyệt";
            String content = "<p>Bài đăng số: " + article.getArticleId() + "</p>\n" +
                    "\n" +
                    "<p>Tiêu đề: " + article.getTitle() + "</p>\n" +
                    "\n" +
                    "<p>Người đăng: " + article.getCustomer().getName() + "</p>\n" +
                    "\n" +
                    "<p>Email: " + article.getCustomer().getEmail() + "</p>\n" +
                    "\n" +
                    "<p>SĐT: " + article.getCustomer().getPhone() + "</p>\n" +
                    "\n" +
                    "<p>Thời gian đăng: " + simpleDateFormat.format(article.getTimeCreated()) + "</p>\n" +
                    "\n" +
                    "\n" +
                    "<p>Thời gian duyệt bài: " + simpleDateFormat.format(new Date()) + "</p>\n" +
                    "\n" +
                    "<p>Trạng thái: <strong><span style=\"color:#2980b9\">đã được duyệt</span></strong></p>\n" +
                    "\n" +
                    "<p>Thời gian hết hạn (ước tính): <span style=\"color:#c0392b\">" + simpleDateFormat.format(article.getExpTime()) + "</span></p>\n" +
                    "\n" +
                    "<p>Bài đăng của bạn đã được nhân viên <em><strong>" + staff.getName() + " </strong></em>(email: <em><strong>" + staff.getEmail() + "</strong></em>) duyệt vào lúc <em><strong>" + simpleDateFormat.format(new Date()) + "</strong></em>.</p>\n" +
                    "\n" +
                    "<p>Bạn có thể vào theo đường dẫn sau để xem bài viết của mình:</p>\n" +
                    "\n" +
                    "<p> xxxx </p>\n";
            if (article.getCustomer() != null) {
                String to = article.getCustomer().getEmail();
                try {
                    mailSender.send(to, title, content, note);
                } catch (Exception e) {
                    throw new CustomException("Lỗi, gửi mail thất bại");
                }
            }

            //lưu
            staffArticleRepository.save(staffArticle);
            ArticleOutputDTO articleOutputDTO = helper.convertToOutputDTO(articleRepository.save(article));

            return new Message("Duyệt bài thành công");
        } else throw new CustomException("Bài đăng với id: " + id + " không tồn tại");
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
                throw new CustomException("JWT hết hạn");
            } catch (Exception e) {
                throw new CustomException("JWT không hợp lệ");
            }

            //duyệt bài
            //chuyển deleted thành true
            Article article = articleOptional.get();
            if (!article.getStatus().equals(VariableCommon.DANG_DANG))
                throw new CustomException("Chỉ có thể ẩn khi bài viết đang đăng");

            article.setStatus(VariableCommon.BI_AN);

            //tạo bản ghi staffArticle
            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setTime(new Date());
            staffArticle.setStaff(staff);
            staffArticle.setArticle(article);
            staffArticle.setAction("Ẩn bài");

            //gửi thư
            if (article.getCustomer() != null) {
                if (reason == null || reason.trim().equals("")) reason = "không có lý do cụ thể";
                String to = article.getCustomer().getEmail();
                String note = "Nhân viên ẩn bài: " + staff.getName() + "<br/>"
                        + "Email: " + staff.getEmail() + "<br/>"
                        + "Thời gian: " + simpleDateFormat.format(new Date());
                String title = "Bài đăng số: " + article.getArticleId() + " đã bị ẩn";
                String content = "<p>Bài đăng số: " + article.getArticleId() + "</p>\n" +
                        "\n" +
                        "<p>Tiêu đề: " + article.getTitle() + "</p>\n" +
                        "\n" +
                        "<p>Người đăng: " + article.getCustomer().getName() + "</p>\n" +
                        "\n" +
                        "<p>Email: " + article.getCustomer().getEmail() + "</p>\n" +
                        "\n" +
                        "<p>SĐT: " + article.getCustomer().getPhone() + "</p>\n" +
                        "\n" +
                        "<p>Thời gian ẩn bài: " + simpleDateFormat.format(new Date()) + "</p>\n" +
                        "\n" +
                        "<p>Trạng thái: <strong><span style=\"color:red\">Đã bị ẩn</span></strong></p>\n" +
                        "\n" +
                        "<p>Lý do: <strong><span style=\"color:blue\">" + reason + "</span></strong></p>\n" +
                        "\n" +
                        "<p>Bài đăng của bạn đã bị nhân viên <em><strong>" + staff.getName() + " </strong></em>(email: <em><strong>" + staff.getEmail() + "</strong></em>) ẩn vào lúc <em><strong>" + simpleDateFormat.format(new Date()) + "</strong></em>.</p>\n" +
                        "\n" +
                        "<p>Chúng tôi rất tiếc về điều này, bạn vui lòng xem lại bài đăng của mình đã phù hợp với nội quy website chưa. Mọi thắc mắc xin liên hệ theo email nhân viên đã duyệt bài.</p>\n";
                try {
                    mailSender.send(to, title, content, note);
                } catch (Exception e) {
                    throw new CustomException("Lỗi, gửi mail thất bại");
                }
            }

            //lưu
            staffArticleRepository.save(staffArticle);
            ArticleOutputDTO articleOutputDTO = helper.convertToOutputDTO(articleRepository.save(article));

            return new Message("Ẩn bài thành công");
        } else throw new CustomException("Bài đăng với id: " + id + " không tồn tại");
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
                throw new CustomException("JWT hết hạn");
            } catch (Exception e) {
                throw new CustomException("JWT không hợp lệ");
            }

            //duyệt bài
            //chuyển deleted thành true
            Article article = articleOptional.get();
            if (!article.getStatus().equals(VariableCommon.CHUA_DUYET) &&
                    !article.getStatus().equals(VariableCommon.DANG_DANG))
                throw new CustomException("Chỉ có áp dụng yêu cầu sửa lại với bài chưa duyệt hoặc đang đăng");

            article.setStatus(VariableCommon.SUA_LAI);

            //tạo bản ghi staffArticle
            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setTime(new Date());
            staffArticle.setStaff(staff);
            staffArticle.setArticle(article);
            staffArticle.setAction("Yêu cầu sửa lại bài");

            //gửi thư
            if (article.getCustomer() != null) {
                if (reason == null || reason.trim().equals("")) reason = "không có lý do cụ thể";
                String to = article.getCustomer().getEmail();
                String note = "Nhân viên yêu cầu sửa bài: " + staff.getName() + "<br/>"
                        + "Email: " + staff.getEmail() + "<br/>"
                        + "Thời gian: " + simpleDateFormat.format(new Date());
                String title = "Yêu cầu sửa lại bài đăng số: " + article.getArticleId();
                String content = "<p>Bài đăng số: " + article.getArticleId() + "</p>\n" +
                        "\n" +
                        "<p>Tiêu đề: " + article.getTitle() + "</p>\n" +
                        "\n" +
                        "<p>Người đăng: " + article.getCustomer().getName() + "</p>\n" +
                        "\n" +
                        "<p>Email: " + article.getCustomer().getEmail() + "</p>\n" +
                        "\n" +
                        "<p>SĐT: " + article.getCustomer().getPhone() + "</p>\n" +
                        "\n" +
                        "<p>Thời gian yêu cầu: " + simpleDateFormat.format(new Date()) + "</p>\n" +
                        "\n" +
                        "<p>Trạng thái: <strong><span style=\"color:red\">Bài viết cần được sửa lại</span></strong></p>\n" +
                        "\n" +
                        "<p>Lý do: <strong><span style=\"color:blue\">" + reason + "</span></strong></p>\n" +
                        "\n" +
                        "<p>Bài đăng của bạn đã bị nhân viên <em><strong>" + staff.getName() + " </strong></em>(email: <em><strong>" + staff.getEmail() + "</strong></em>) yêu cầu sửa lại, vào lúc <em><strong>" + simpleDateFormat.format(new Date()) + "</strong></em>.</p>\n" +
                        "\n" +
                        "<p>Chúng tôi rất tiếc về điều này, bạn vui lòng xem lại bài đăng của mình đã phù hợp với nội quy website chưa. Mọi thắc mắc xin liên hệ theo email nhân viên đã duyệt bài.</p>\n";
                try {
                    mailSender.send(to, title, content, note);
                } catch (Exception e) {
                    throw new CustomException("Lỗi, gửi mail thất bại");
                }
            }

            //lưu
            staffArticleRepository.save(staffArticle);
            ArticleOutputDTO articleOutputDTO = helper.convertToOutputDTO(articleRepository.save(article));

            return new Message("Yêu cầu sửa lại bài thành công");
        } else throw new CustomException("Bài đăng với id: " + id + " không tồn tại");
    }

    @Override
    public ArticleOutputDTO detailArticle(Integer id) throws CustomException {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (!articleOptional.isPresent())
            throw new CustomException("Bài đăng với id: " + id + " không tồn tại");

        Article article = articleOptional.get();
        return helper.convertToOutputDTO(article);
    }

    @Override
    public ArticleOutputDTO insertArticle(String email, ArticleInsertDTO articleInsertDTO) throws CustomException {
        Staff staff = staffRepository.findByEmail(email);
        if (staff == null) throw new CustomException("Admin đăng bài không hợp lệ");

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
            if (!wardOptional.isPresent()) throw new CustomException("Ward Id không hợp lệ");
            article.setWard(wardOptional.get());

            article.setUpdateTime(new Date());
            article.setTimeGroup(0);
            article.setStatus(VariableCommon.DANG_DANG);
            article.setCustomer(null);

            //tạo hạn sử dụng
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
            } else throw new CustomException("Type của bài đăng bị sai");

            //lưu bài
            Article newArticle = articleRepository.save(article);

            //nhân viên đăng bài
            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setStaff(staff);
            staffArticle.setArticle(newArticle);
            staffArticle.setTime(new Date());
            staffArticle.setAction("Đăng bài");
            staffArticleRepository.save(staffArticle);

            return helper.convertToOutputDTO(newArticle);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("Đăng bài thất bại");
        }
    }

    @Override
    public ArticleOutputDTO updateArticle(String email, ArticleUpdateDTO articleUpdateDTO, Integer id) throws CustomException {
        Staff staff = staffRepository.findByEmail(email);
        if (staff == null) throw new CustomException("Không tìm thấy admin");
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);

            Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
            if (article == null) throw new CustomException("Bài đăng id không hợp lệ");
            if (article.getCustomer() != null)
                throw new CustomException("Admin chỉ được sửa bài do admin đăng");

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
            if (!wardOptional.isPresent()) throw new CustomException("Ward Id không hợp lệ");
            article.setWard(wardOptional.get());

            article.setUpdateTime(new Date());
            article.setTimeGroup(0);

            //xóa toàn bộ comment,  yêu thích, điểm
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

            //lưu bài
            Article newArticle = articleRepository.save(article);

            //tạo bản ghi staffArticle
            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setStaff(staff);
            staffArticle.setArticle(newArticle);
            staffArticle.setTime(new Date());
            staffArticle.setAction("Sửa bài");
            staffArticleRepository.save(staffArticle);

            return helper.convertToOutputDTO(newArticle);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("Cập nhật bài thất bại");
        }
    }

    @Override
    public Message extensionExp(String email, Integer id, Integer date, String type) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null)
            throw new CustomException("Bài đăng với id: " + id + " không tồn tại");
        else if (!article.getStatus().equals(VariableCommon.DANG_DANG))
            throw new CustomException("Gia hạn chỉ áp dụng với bài đăng đã được duyệt");

        Staff staff = staffRepository.findByEmail(email);
        if (article.getCustomer() != null)
            throw new CustomException("Admin chỉ được gia hạn bài viết bài viết do admin đăng");

        //tạo thời hạn
        if (article.getType().equals("day")) {
            article.setExpTime(helper.addDayForDate(date, article.getExpTime()));
        } else if (article.getType().equals("week") || article.getType().equals("month")) {
            Integer numberDay = helper.calculateDays(date, type, article.getExpTime());
            article.setExpTime(helper.addDayForDate(numberDay, article.getExpTime()));
        } else throw new CustomException("Type của bài đăng bị sai");

        Article newArticle = articleRepository.save(article);

        //tạo bản ghi staffArticle
        StaffArticle staffArticle = new StaffArticle();
        staffArticle.setArticle(newArticle);
        staffArticle.setStaff(staff);
        staffArticle.setTime(new Date());
        staffArticle.setAction("Gia hạn bài đăng thêm " + date + " " + type);
        staffArticleRepository.save(staffArticle);

        return new Message("Gia hạn bài đăng id: " + id + " thành công");
    }

    @Override
    public Message postOldArticle(String email, Integer id, Integer date, String type, Boolean vip) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        Staff staff = staffRepository.findByEmail(email);
        if (staff == null)
            throw new CustomException("Nhân viên không tồn tại");
        if (article == null)
            throw new CustomException("Bài đăng với id: " + id + " không tồn tại, hoặc đang không bị ẩn");
        if (article.getCustomer() != null)
            throw new CustomException("Admin chỉ được đăng lại bài cũ do admin đăng");
        if (article.getStatus().equals(VariableCommon.DANG_DANG) ||
                article.getStatus().equals(VariableCommon.CHUA_DUYET) ||
                article.getStatus().equals(VariableCommon.SUA_LAI))
            throw new CustomException("Chỉ được đăng lại bài đã ẩn hoặc hết hạn");

        article.setVip(vip);

        article.setStatus(VariableCommon.DANG_DANG);

        article.setNumber(date);
        article.setType(type);

        article.setUpdateTime(new Date());
        article.setTimeGroup(0);

        //xóa toàn bộ comment,  yêu thích, điểm
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

        //tạo thời hạn
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
        } else throw new CustomException("Type của bài đăng bị sai");
        article.setNumber(date);
        article.setType(type);

        Article newArticle = articleRepository.save(article);

        //tạo bản ghi staffArticle
        StaffArticle staffArticle = new StaffArticle();
        staffArticle.setStaff(staff);
        staffArticle.setArticle(newArticle);
        staffArticle.setTime(new Date());
        staffArticle.setAction("Đăng lại bàng đăng");
        staffArticleRepository.save(staffArticle);

        return new Message("Đăng lại bài đăng đã ẩn id: " + id + " thành công");
    }

    @Override
    public Message buffPoint(String email, Integer id, Integer point) throws CustomException {
        Staff staff = staffRepository.findByEmail(email);
        if (staff == null) throw new CustomException("Nhân viên không tồn tại");
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null) throw new CustomException("Bài đăng không tồn tại");
        if (point < 0) throw new CustomException("Số điểm nhỏ nhất là 0");

        article.setPoint(article.getPoint() + point);
        article.setUpdateTime(new Date());
        article.setTimeGroup(0);

        Article newArticle = articleRepository.save(article);

        //tạo staffArticle
        StaffArticle staffArticle = new StaffArticle();
        staffArticle.setArticle(newArticle);
        staffArticle.setStaff(staff);
        staffArticle.setTime(new Date());
        staffArticle.setAction("Làm mới và tăng cho bài đăng " + point + " điểm");
        staffArticleRepository.save(staffArticle);

        return new Message("Bạn đã làm mới và tăng điểm thành công, bài đăng hiện có: " + newArticle.getPoint() + " điểm");
    }

    private Staff findStaffByJWT(HttpServletRequest request) throws Exception {
        String jwt = extractJwtFromRequest(request);
        if (jwt == null || jwt.trim().equals("")) throw new CustomException("Không có JWT");
        String email = jwtUtil.getUsernameFromToken(jwt);
        if (email == null || email.trim().equals("")) throw new CustomException("JWT không hợp lệ");
        Staff staff = staffRepository.findByEmail(email);
        if (staff == null) throw new CustomException("JWT không hợp lệ");
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
