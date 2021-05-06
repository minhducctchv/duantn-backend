package duantn.backend.helper;

import duantn.backend.authentication.CustomException;
import duantn.backend.authentication.CustomJwtAuthenticationFilter;
import duantn.backend.authentication.JwtUtil;
import duantn.backend.dao.CustomerRepository;
import duantn.backend.dao.FavoriteArticleRepository;
import duantn.backend.dao.StaffArticleRepository;
import duantn.backend.dao.StaffRepository;
import duantn.backend.model.dto.output.ArticleOutputDTO;
import duantn.backend.model.entity.Article;
import duantn.backend.model.entity.StaffArticle;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class Helper {
    final
    CustomerRepository customerRepository;
    final
    StaffRepository staffRepository;
    final
    JwtUtil jwtUtil;
    final
    CustomJwtAuthenticationFilter customJwtAuthenticationFilter;
    final
    StaffArticleRepository staffArticleRepository;
    final
    FavoriteArticleRepository favoriteArticleRepository;

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

    public Helper(CustomerRepository customerRepository, StaffRepository staffRepository, JwtUtil jwtUtil, CustomJwtAuthenticationFilter customJwtAuthenticationFilter, StaffArticleRepository staffArticleRepository, FavoriteArticleRepository favoriteArticleRepository) {
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
        this.jwtUtil = jwtUtil;
        this.customJwtAuthenticationFilter = customJwtAuthenticationFilter;
        this.staffArticleRepository = staffArticleRepository;
        this.favoriteArticleRepository = favoriteArticleRepository;
    }

    public String getHostUrl(String url, String substring) {
        int index = url.indexOf(substring);
        String hostUrl = url.substring(0, index);
        return hostUrl;
    }

    public String getBaseURL(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        StringBuffer url =  new StringBuffer();
        url.append(scheme).append("://").append(serverName);
        if ((serverPort != 80) && (serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        url.append(contextPath);
        if(url.toString().endsWith("/")){
            url.append("/");
        }
        return url.toString();
    }

    public String getEmailFromRequest(HttpServletRequest request) throws CustomException {
        try{
            String token= customJwtAuthenticationFilter.extractJwtFromRequest(request);
            String email=jwtUtil.getUsernameFromToken(token);
            return email;
        }catch (Exception e){
            throw new CustomException("Token bị thiếu, hoặc không hợp lệ");
        }
    }

    public String getEmailOrNullFromRequest(HttpServletRequest request) throws CustomException {
        try{
            String token= customJwtAuthenticationFilter.extractJwtFromRequest(request);
            String email=jwtUtil.getUsernameFromToken(token);
            return email;
        }catch (Exception e){
            return null;
        }
    }

    public String createToken(int sl) {
        //create token
        String token;
        while (true) {
            token = randomAlphaNumeric(sl);
            if (customerRepository.findByToken(token) == null
                    && staffRepository.findByToken(token) == null) break;
        }
        return token;
    }

    private final String alpha = "abcdefghijklmnopqrstuvwxyz"; // a-z
    private final String alphaUpperCase = alpha.toUpperCase(); // A-Z
    private final String digits = "0123456789"; // 0-9
    private final String ALPHA_NUMERIC = alpha + alphaUpperCase + digits;

    private Random generator = new Random();

    /**
     * Random string with a-zA-Z0-9, not included special characters
     */
    public String randomAlphaNumeric(int numberOfCharactor) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfCharactor; i++) {
            int number = randomNumber(0, ALPHA_NUMERIC.length() - 1);
            char ch = ALPHA_NUMERIC.charAt(number);
            sb.append(ch);
        }
        return sb.toString();
    }

    public int randomNumber(int min, int max) {
        return generator.nextInt((max - min) + 1) + min;
    }

    public String listToText(List<String> list){
        String string="";
        for (String s:list){
            string=string+s+"___";
        }
        return string;
    }

    public List<String> textToList(String text){
        String[] mang=text.split("___");
        return Arrays.asList(mang);
    }

    public Integer calculateDays(int number, String type, Date starTime){
        Calendar start=Calendar.getInstance();
        start.setTime(starTime);
        long millisecond;
        if(type.trim().equals("week")){
            Calendar end=Calendar.getInstance();
            end.setTime(starTime);
            end.add(Calendar.WEEK_OF_YEAR, number);
            millisecond=end.getTime().getTime()-start.getTime().getTime();
        }else if(type.trim().equals("month")){
            Calendar end=Calendar.getInstance();
            end.setTime(starTime);
            end.add(Calendar.MONTH, number);
            millisecond=end.getTime().getTime()-start.getTime().getTime();
        } else return null;
        int days= (int) (millisecond/(24*3600*1000));
        return days;
    }

    public Date addDayForDate(Integer days, Date date){
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    public ArticleOutputDTO convertToOutputDTO(Article article) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        ArticleOutputDTO articleOutputDTO = modelMapper.map(article, ArticleOutputDTO.class);
        articleOutputDTO.setCreateTime(article.getTimeCreated().getTime());
        articleOutputDTO.setLastUpdateTime(article.getUpdateTime().getTime());

        Map<String, String> customer = new HashMap<>();
        customer.put("customerId", article.getCustomer().getCustomerId() + "");
        customer.put("name", article.getCustomer().getName());
        customer.put("email", article.getCustomer().getEmail());
        customer.put("phone", article.getCustomer().getPhone());
        customer.put("image", article.getCustomer().getImage());
        customer.put("balance", article.getCustomer().getAccountBalance()+"");
        articleOutputDTO.setCustomer(customer);

        if (article.getExpTime()!=null) {
            articleOutputDTO.
                    setExpDate(article.getExpTime().getTime());
        }

        Map<String, String> location = new HashMap<>();
        location.put("wardId", article.getWard().getWardId() + "");
        location.put("wardName", article.getWard().getWardName());
        location.put("districtId", article.getWard().getDistrict().getDistrictId() + "");
        location.put("districtName", article.getWard().getDistrict().getDistrictName());
        location.put("cityId", article.getWard().getDistrict().getCity().getCityId() + "");
        location.put("cityName", article.getWard().getDistrict().getCity().getCityName());
        articleOutputDTO.setLocation(location);

        //set price va tinh tien
        if(article.getVip()){
            switch (article.getType()){
                case "day": {
                    articleOutputDTO.setPrice(vipDayPrice);
                    articleOutputDTO.setAmount(article.getNumber()*vipDayPrice);
                    break;
                }
                case "week": {
                    articleOutputDTO.setPrice(vipWeekPrice);
                    articleOutputDTO.setAmount(article.getNumber()*vipWeekPrice);
                    break;
                }
                case "month": {
                    articleOutputDTO.setPrice(vipMonthPrice);
                    articleOutputDTO.setAmount(article.getNumber()*vipMonthPrice);
                    break;
                }
            }
        }else{
            switch (article.getType()){
                case "day": {
                    articleOutputDTO.setPrice(dayPrice);
                    articleOutputDTO.setAmount(article.getNumber()*dayPrice);
                    break;
                }
                case "week": {
                    articleOutputDTO.setPrice(weekPrice);
                    articleOutputDTO.setAmount(article.getNumber()*weekPrice);
                    break;
                }
                case "month": {
                    articleOutputDTO.setPrice(monthPrice);
                    articleOutputDTO.setAmount(article.getNumber()*monthPrice);
                    break;
                }
            }
        }

        articleOutputDTO.setCountLike(favoriteArticleRepository.countByArticle(article));

        return articleOutputDTO;
    }
}
