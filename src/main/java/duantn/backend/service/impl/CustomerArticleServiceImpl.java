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
        if (customer == null) throw new CustomException("Kh??ch h??ng ????ng b??i kh??ng h???p l???");

        //ki???m tra v?? tr??? ti???n
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
        } else throw new CustomException("Type c???a th???i gian kh??ng h???p l???");
        if (customer.getAccountBalance() < money)
            throw new CustomException("S??? ti???n trong t??i kho???n kh??ng ?????");
        customer.setAccountBalance(customer.getAccountBalance() - money);

        String description = "Thanh to??n ????ng b??i: " + money + " VN?? cho b??i ????ng: " + articleInsertDTO.getTitle();

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
            article.setStatus(VariableCommon.CHUA_DUYET);

            Customer newCustomer = customerRepository.save(customer);
            article.setCustomer(newCustomer);
            creatTransactionPay(money, newCustomer, description);
            return helper.convertToOutputDTO(articleRepository.save(article));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("????ng b??i th???t b???i");
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
        if (customer == null) throw new CustomException("Kh??ng t??m th???y kh??ch h??ng");
        try {
//            ModelMapper modelMapper = new ModelMapper();
//            modelMapper.getConfiguration()
//                    .setMatchingStrategy(MatchingStrategies.STRICT);

            Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
            if (article == null) throw new CustomException("B??i ????ng id kh??ng h???p l???");
            if (customer != article.getCustomer())
                throw new CustomException("Kh??ch h??ng kh??ng h???p l???");

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
            if (!wardOptional.isPresent()) throw new CustomException("Ward Id kh??ng h???p l???");
            article.setWard(wardOptional.get());

            article.setUpdateTime(new Date());
            article.setPoint(0);
            //x??a to??n b??? comment
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
            throw new CustomException("C???p nh???t b??i th???t b???i");
        }
    }

    @Override
    public Message hiddenArticle(String email, Integer id) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null)
            throw new CustomException("B??i ????ng v???i id: " + id + " kh??ng t???n t???i");

        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Kh??ch h??ng kh??ng h???p l???");
        article.setStatus(VariableCommon.BI_AN);

        articleRepository.save(article);
        return new Message("???n b??i ????ng id: " + id + " th??nh c??ng");
    }

    @Override
    public Message deleteArticle(String email, Integer id) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null)
            throw new CustomException("B??i ????ng v???i id: " + id + " kh??ng t???n t???i");

        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Kh??ch h??ng kh??ng h???p l???");

        article.setDeleted(true);
        articleRepository.save(article);
        return new Message("X??a b??i ????ng id: " + id + " th??nh c??ng");
    }

    @Override
    public Message extensionExp(String email, Integer id, Integer days, String type) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null)
            throw new CustomException("B??i ????ng v???i id: " + id + " kh??ng t???n t???i");
        else if (!article.getStatus().equals(VariableCommon.DANG_DANG))
            throw new CustomException("Gia h???n ch??? ??p d???ng v???i b??i ????ng ???? ???????c duy???t");

        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Kh??ch h??ng kh??ng h???p l???");

        //ki???m tra v?? tr??? ti???n
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
        } else throw new CustomException("Type c???a th???i gian kh??ng h???p l???");
        Customer customer = article.getCustomer();
        if (customer.getAccountBalance() < money)
            throw new CustomException("S??? ti???n trong t??i kho???n kh??ng ?????");
        customer.setAccountBalance(customer.getAccountBalance() - money);

        String description = "Thanh to??n gia h???n: " + money + " VN?? cho b??i ????ng: " + article.getTitle();

        //t???o th???i h???n
        if (article.getType().equals("day")) {
            article.setExpTime(helper.addDayForDate(days, article.getExpTime()));
        } else if (article.getType().equals("week") || article.getType().equals("month")) {
            Integer numberDay = helper.calculateDays(days, type, article.getExpTime());
            article.setExpTime(helper.addDayForDate(numberDay, article.getExpTime()));
        } else throw new CustomException("Type c???a b??i ????ng b??? sai");

        Customer newCustomer = customerRepository.save(customer);
        creatTransactionPay(money, newCustomer, description);
        articleRepository.save(article);
        return new Message("Gia h???n b??i ????ng id: " + id + " th??nh c??ng");
    }

    @Override
    public Message postOldArticle(String email, Integer id, Integer days, String type,
                                  Boolean vip) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null)
            throw new CustomException("B??i ????ng v???i id: " + id + " kh??ng t???n t???i");
        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Kh??ch h??ng kh??ng h???p l???");
        if (article.getStatus().equals(VariableCommon.DANG_DANG) ||
                article.getStatus().equals(VariableCommon.SUA_LAI) ||
                article.getStatus().equals(VariableCommon.CHUA_DUYET))
            throw new CustomException("????ng l???i b??i c?? ch??? ??p d???ng v???i b??i b??? ???n ho???c h???t h???n");
        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Kh??ch h??ng kh??ng h???p l???");

        article.setVip(vip);

        //ki???m tra v?? tr??? ti???n
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
        } else throw new CustomException("Type c???a th???i gian kh??ng h???p l???");
        Customer customer = article.getCustomer();
        if (customer.getAccountBalance() < money)
            throw new CustomException("S??? ti???n trong t??i kho???n kh??ng ?????");
        customer.setAccountBalance(customer.getAccountBalance() - money);

        String description = "Thanh to??n ????ng l???i b??i: " + money + " VN?? cho b??i ????ng: " + article.getTitle();

        article.setStatus(VariableCommon.CHUA_DUYET);

        article.setNumber(days);
        article.setType(type);

        article.setUpdateTime(new Date());
        article.setPoint(0);
        //x??a to??n b??? comment
        List<Comment> comments = commentRepository.findByArticle(article);
        if(comments.size()>0){
            for (Comment comment : comments) {
                commentRepository.delete(comment);
            }
        }

        Customer newCustomer = customerRepository.save(customer);
        creatTransactionPay(money, newCustomer, description);
        articleRepository.save(article);
        return new Message("????ng l???i b??i ????ng ???? ???n id: " + id + " th??nh c??ng");
    }

    @Override
    public ArticleOutputDTO detailArticle(String email, Integer id) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null)
            throw new CustomException("B??i ????ng v???i id: " + id + " kh??ng t???n t???i");

        System.out.println("email: " + email);
        System.out.println("customer: " + article.getCustomer().getEmail());
        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Kh??ch h??ng kh??ng h???p l???");
        return helper.convertToOutputDTO(article);
    }

    @Value("${duantn.point.price}")
    private Integer pricePoint;

    @Value("${duantn.renew.price}")
    private Integer renewPrice;

    @Override
    public Message buffPoint(String email, Integer id, Integer point) throws CustomException {
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) throw new CustomException("Kh??ch h??ng kh??ng t???n t???i");
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null) throw new CustomException("B??i ????ng kh??ng t???n t???i");
        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("B???n kh??ng ???????c t??ng buff cho b??i ????ng c???a ng?????i kh??c");
        if (point < 0) throw new CustomException("S??? ??i???m nh??? nh???t l?? 0");
        if(!article.getStatus().equals(VariableCommon.DANG_DANG))
            throw new CustomException("Ch??? c?? th??? buff ??i???m cho b??i ??ang ????ng");

        article.setPoint(article.getPoint() + point);
        article.setUpdateTime(new Date());
        article.setTimeGroup(0);

        //t??nh ti???n
        Integer amount = pricePoint * point + renewPrice;
        if (customer.getAccountBalance() < amount) throw new CustomException("B???n kh??ng ????? ti???n trong t??i kho???n");
        customer.setAccountBalance(customer.getAccountBalance() - amount);
        Customer newCustomer = customerRepository.save(customer);

        Transaction transaction = new Transaction();
        transaction.setCustomer(newCustomer);
        transaction.setAmount(amount);
        transaction.setType(false);
        transaction.setDescription("L??m m???i v?? t??ng " + point + " ??i???m cho b??i ????ng: " + article.getTitle() + " (id: " +
                article.getArticleId() + ")");
        transactionRepository.save(transaction);

        Article newArticle = articleRepository.save(article);

        return new Message("B???n ???? l??m m???i v?? t??ng ??i???m th??nh c??ng, b??i ????ng hi???n c??: " + newArticle.getPoint() + " ??i???m");
    }

    @Override
    public Map<String, Object> showPoint(Integer id) throws CustomException {
        Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
        if (article == null) throw new CustomException("B??i ????ng kh??ng t???n t???i");
        Map<String, Object> map = new HashMap<>();
        map.put("point", article.getPoint());
        return map;
    }
}
