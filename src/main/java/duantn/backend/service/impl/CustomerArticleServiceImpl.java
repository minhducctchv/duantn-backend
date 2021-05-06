package duantn.backend.service.impl;

import duantn.backend.authentication.CustomException;
import duantn.backend.config.paypal.PaypalService;
import duantn.backend.dao.*;
import duantn.backend.helper.Helper;
import duantn.backend.helper.VariableCommon;
import duantn.backend.model.dto.input.ArticleInsertDTO;
import duantn.backend.model.dto.input.ArticleUpdateDTO;
import duantn.backend.model.dto.input.RoommateDTO;
import duantn.backend.model.dto.output.ArticleOutputDTO;
import duantn.backend.model.dto.output.Message;
import duantn.backend.model.entity.*;
import duantn.backend.service.CustomerArticleService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CustomerArticleServiceImpl implements CustomerArticleService {
    final
    ArticleRepository articleRepository;
    final
    StaffArticleRepository staffArticleRepository;
    final
    CustomerRepository customerRepository;
    final
    WardRepository wardRepository;
    final
    Helper helper;
    final
    TransactionRepository transactionRepository;
    final
    FavoriteArticleRepository favoriteArticleRepository;
    final
    PaypalService paypalService;
    final
    CommentRepository commentRepository;

    @Value("${duantn.day.price}")
    private Integer dayPrice;
    @Value("${duantn.week.price}")
    private Integer weekPrice;
    @Value("${duantn.month.price}")
    private Integer monthPrice;

    @Value("${duantn.vip.day.price}")
    private Integer vipDayPrice;
    @Value("${duantn.vip.week.price}")
    private Integer vipWeekPrice;
    @Value("${duantn.vip.month.price}")
    private Integer vipMonthPrice;

    public CustomerArticleServiceImpl(ArticleRepository articleRepository, StaffArticleRepository staffArticleRepository, CustomerRepository customerRepository, WardRepository wardRepository, Helper helper, TransactionRepository transactionRepository, FavoriteArticleRepository favoriteArticleRepository, PaypalService paypalService, CommentRepository commentRepository) {
        this.articleRepository = articleRepository;
        this.staffArticleRepository = staffArticleRepository;
        this.customerRepository = customerRepository;
        this.wardRepository = wardRepository;
        this.helper = helper;
        this.transactionRepository = transactionRepository;
        this.favoriteArticleRepository = favoriteArticleRepository;
        this.paypalService = paypalService;
        this.commentRepository = commentRepository;
    }

    @Override
    public List<ArticleOutputDTO> listArticle(String email, String sort, Long start, Long end, Integer ward, Integer district, Integer city, Boolean roommate, String status, Boolean vip, String search, Integer minAcreage, Integer maxAcreage, Integer minPrice, Integer maxPrice, Integer page, Integer limit) {
        List<Article> articleList =
                articleRepository.findCustomAndEmail(email, sort, start, end, ward, district, city,
                        roommate, status, vip, search, minAcreage, maxAcreage, minPrice, maxPrice, page, limit);
        Map<String, Long> countMap = articleRepository.findCustomAndEmailCount(
                email, start, end, ward, district, city,
                roommate, status, vip, search, minAcreage, maxAcreage, minPrice, maxPrice, limit
        );
        List<ArticleOutputDTO> articleOutputDTOList = new ArrayList<>();
        if (articleList.size() > 0) {
            for (Article article : articleList) {
                ArticleOutputDTO articleOutputDTO = helper.convertToOutputDTO(article);
                articleOutputDTO.setElements(countMap.get("elements"));
                articleOutputDTO.setPages(countMap.get("pages"));
                articleOutputDTOList.add(articleOutputDTO);
            }
        }
        return articleOutputDTOList;
    }

    @Override
    public ArticleOutputDTO insertArticle(String email, ArticleInsertDTO articleInsertDTO)
            throws CustomException {
        System.out.println("email: " + email);
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) throw new CustomException("Khách hàng đăng bài không hợp lệ");

        //kiểm tra và trừ tiền
        Integer money = null;
        int priceDay = articleInsertDTO.getVip() ? vipDayPrice : dayPrice;
        int priceWeek = articleInsertDTO.getVip() ? vipWeekPrice : weekPrice;
        int priceMonth = articleInsertDTO.getVip() ? vipMonthPrice : monthPrice;
        if (articleInsertDTO.getType().equals("day")) {
            money = articleInsertDTO.getNumber() * priceDay;
        } else if (articleInsertDTO.getType().equals("week")) {
            money = articleInsertDTO.getNumber() * priceWeek;
        } else if (articleInsertDTO.getType().equals("month")) {
            money = articleInsertDTO.getNumber() * priceMonth;
        } else throw new CustomException("Type của thời gian không hợp lệ");
        if (customer.getAccountBalance() < money)
            throw new CustomException("Số tiền trong tài khoản không đủ");
        customer.setAccountBalance(customer.getAccountBalance() - money);

        String description = "Thanh toán đăng bài: " + money + " VNĐ cho bài đăng: " + articleInsertDTO.getTitle();

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
            article.setStatus(VariableCommon.CHUA_DUYET);

            Customer newCustomer = customerRepository.save(customer);
            article.setCustomer(newCustomer);
            creatTransactionPay(money, newCustomer, description);
            return helper.convertToOutputDTO(articleRepository.save(article));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("Đăng bài thất bại");
        }
    }

    public void creatTransactionPay(Integer amount, Customer customer, String description) {
        Transaction transaction = new Transaction();
        transaction.setType(false);
        transaction.setAmount(amount);
        transaction.setTimeCreated(new Date());
        transaction.setCustomer(customer);
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }

    @Override
    public ArticleOutputDTO updateArticle(String email, ArticleUpdateDTO articleUpdateDTO,
                                          Integer id) throws CustomException {
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) throw new CustomException("Không tìm thấy khách hàng");
        try {
//            ModelMapper modelMapper = new ModelMapper();
//            modelMapper.getConfiguration()
//                    .setMatchingStrategy(MatchingStrategies.STRICT);

            Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
            if (article == null) throw new CustomException("Bài đăng id không hợp lệ");
            if (customer != article.getCustomer())
                throw new CustomException("Khách hàng không hợp lệ");

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
                if(roommate==null) roommate=new Roommate();
                roommate.setDescription(roommateDTO.getDescription());
                roommate.setGender(roommateDTO.getGender());
                roommate.setQuantity(roommateDTO.getQuantity());
                article.setRoommate(roommate);
            } else article.setRoommate(null);


            Optional<Ward> wardOptional = wardRepository.findById(articleUpdateDTO.getWardId());
            if (!wardOptional.isPresent()) throw new CustomException("Ward Id không hợp lệ");
            article.setWard(wardOptional.get());

            article.setUpdateTime(new Date());
            article.setPoint(0);
            //xóa toàn bộ comment
            List<Comment> comments = commentRepository.findByArticle(article);
            if(comments.size()>0){
                for (Comment comment : comments) {
                    commentRepository.delete(comment);
                }
            }

            if (article.getStatus().equals(VariableCommon.DANG_DANG) || article.getStatus().equals(VariableCommon.SUA_LAI))
                article.setStatus(VariableCommon.DA_SUA);

            return helper.convertToOutputDTO(articleRepository.save(article));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("Cập nhật bài thất bại");
        }
    }

    @Override
    public Message hiddenArticle(String email, Integer id) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null)
            throw new CustomException("Bài đăng với id: " + id + " không tồn tại");

        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Khách hàng không hợp lệ");
        article.setStatus(VariableCommon.BI_AN);

        articleRepository.save(article);
        return new Message("Ẩn bài đăng id: " + id + " thành công");
    }

    @Override
    public Message deleteArticle(String email, Integer id) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null)
            throw new CustomException("Bài đăng với id: " + id + " không tồn tại");

        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Khách hàng không hợp lệ");

        article.setDeleted(true);
        articleRepository.save(article);
        return new Message("Xóa bài đăng id: " + id + " thành công");
    }

    @Override
    public Message extensionExp(String email, Integer id, Integer days, String type) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null)
            throw new CustomException("Bài đăng với id: " + id + " không tồn tại");
        else if (!article.getStatus().equals(VariableCommon.DANG_DANG))
            throw new CustomException("Gia hạn chỉ áp dụng với bài đăng đã được duyệt");

        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Khách hàng không hợp lệ");

        //kiểm tra và trừ tiền
        Integer money = null;
        int priceDay = article.getVip() ? vipDayPrice : dayPrice;
        int priceWeek = article.getVip() ? vipWeekPrice : weekPrice;
        int priceMonth = article.getVip() ? vipMonthPrice : monthPrice;
        if (type.equals("day")) {
            money = days * priceDay;
        } else if (type.equals("week")) {
            money = days * priceWeek;
        } else if (type.equals("month")) {
            money = days * priceMonth;
        } else throw new CustomException("Type của thời gian không hợp lệ");
        Customer customer = article.getCustomer();
        if (customer.getAccountBalance() < money)
            throw new CustomException("Số tiền trong tài khoản không đủ");
        customer.setAccountBalance(customer.getAccountBalance() - money);

        String description = "Thanh toán gia hạn: " + money + " VNĐ cho bài đăng: " + article.getTitle();

        //tạo thời hạn
        if (article.getType().equals("day")) {
            article.setExpTime(helper.addDayForDate(days, article.getExpTime()));
        } else if (article.getType().equals("week") || article.getType().equals("month")) {
            Integer numberDay = helper.calculateDays(days, type, article.getExpTime());
            article.setExpTime(helper.addDayForDate(numberDay, article.getExpTime()));
        } else throw new CustomException("Type của bài đăng bị sai");

        Customer newCustomer = customerRepository.save(customer);
        creatTransactionPay(money, newCustomer, description);
        articleRepository.save(article);
        return new Message("Gia hạn bài đăng id: " + id + " thành công");
    }

    @Override
    public Message postOldArticle(String email, Integer id, Integer days, String type,
                                  Boolean vip) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null)
            throw new CustomException("Bài đăng với id: " + id + " không tồn tại");
        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Khách hàng không hợp lệ");
        if (article.getStatus().equals(VariableCommon.DANG_DANG) ||
                article.getStatus().equals(VariableCommon.SUA_LAI) ||
                article.getStatus().equals(VariableCommon.CHUA_DUYET))
            throw new CustomException("Đăng lại bài cũ chỉ áp dụng với bài bị ẩn hoặc hết hạn");
        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Khách hàng không hợp lệ");

        article.setVip(vip);

        //kiểm tra và trừ tiền
        Integer money = null;
        int priceDay = article.getVip() ? vipDayPrice : dayPrice;
        int priceWeek = article.getVip() ? vipWeekPrice : weekPrice;
        int priceMonth = article.getVip() ? vipMonthPrice : monthPrice;
        if (type.equals("day")) {
            money = days * priceDay;
        } else if (type.equals("week")) {
            money = days * priceWeek;
        } else if (type.equals("month")) {
            money = days * priceMonth;
        } else throw new CustomException("Type của thời gian không hợp lệ");
        Customer customer = article.getCustomer();
        if (customer.getAccountBalance() < money)
            throw new CustomException("Số tiền trong tài khoản không đủ");
        customer.setAccountBalance(customer.getAccountBalance() - money);

        String description = "Thanh toán đăng lại bài: " + money + " VNĐ cho bài đăng: " + article.getTitle();

        article.setStatus(VariableCommon.CHUA_DUYET);

        article.setNumber(days);
        article.setType(type);

        article.setUpdateTime(new Date());
        article.setPoint(0);
        //xóa toàn bộ comment
        List<Comment> comments = commentRepository.findByArticle(article);
        if(comments.size()>0){
            for (Comment comment : comments) {
                commentRepository.delete(comment);
            }
        }

        Customer newCustomer = customerRepository.save(customer);
        creatTransactionPay(money, newCustomer, description);
        articleRepository.save(article);
        return new Message("Đăng lại bài đăng đã ẩn id: " + id + " thành công");
    }

    @Override
    public ArticleOutputDTO detailArticle(String email, Integer id) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null)
            throw new CustomException("Bài đăng với id: " + id + " không tồn tại");

        System.out.println("email: " + email);
        System.out.println("customer: " + article.getCustomer().getEmail());
        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Khách hàng không hợp lệ");
        return helper.convertToOutputDTO(article);
    }

    @Value("${duantn.point.price}")
    private Integer pricePoint;

    @Value("${duantn.renew.price}")
    private Integer renewPrice;

    @Override
    public Message buffPoint(String email, Integer id, Integer point) throws CustomException {
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) throw new CustomException("Khách hàng không tồn tại");
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null) throw new CustomException("Bài đăng không tồn tại");
        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Bạn không được tăng buff cho bài đăng của người khác");
        if (point < 0) throw new CustomException("Số điểm nhỏ nhất là 0");
        if(!article.getStatus().equals(VariableCommon.DANG_DANG))
            throw new CustomException("Chỉ có thể buff điểm cho bài đang đăng");

        article.setPoint(article.getPoint() + point);
        article.setUpdateTime(new Date());
        article.setTimeGroup(0);

        //tính tiền
        Integer amount = pricePoint * point + renewPrice;
        if (customer.getAccountBalance() < amount) throw new CustomException("Bạn không đủ tiền trong tài khoản");
        customer.setAccountBalance(customer.getAccountBalance() - amount);
        Customer newCustomer = customerRepository.save(customer);

        Transaction transaction = new Transaction();
        transaction.setCustomer(newCustomer);
        transaction.setAmount(amount);
        transaction.setType(false);
        transaction.setDescription("Làm mới và tăng " + point + " điểm cho bài đăng: " + article.getTitle() + " (id: " +
                article.getArticleId() + ")");
        transactionRepository.save(transaction);

        Article newArticle = articleRepository.save(article);

        return new Message("Bạn đã làm mới và tăng điểm thành công, bài đăng hiện có: " + newArticle.getPoint() + " điểm");
    }

    @Override
    public Map<String, Object> showPoint(Integer id) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null) throw new CustomException("Bài đăng không tồn tại");
        Map<String, Object> map = new HashMap<>();
        map.put("point", article.getPoint());
        return map;
    }
}
