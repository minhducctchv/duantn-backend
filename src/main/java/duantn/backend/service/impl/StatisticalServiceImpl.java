package duantn.backend.service.impl;

import duantn.backend.authentication.CustomException;
import duantn.backend.dao.*;
import duantn.backend.helper.DateHelper;
import duantn.backend.model.dto.output.StaffArticleOutputDTO;
import duantn.backend.model.entity.Article;
import duantn.backend.model.entity.Staff;
import duantn.backend.model.entity.StaffArticle;
import duantn.backend.service.StatisticalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with YourComputer.
 * User: DUC_PRO
 * Date: 27/04/2021
 * Time: 9:41 CH
 */

@Service
public class StatisticalServiceImpl implements StatisticalService {
    final
    ArticleRepository articleRepository;
    final
    TransactionRepository transactionRepository;
    final
    CustomerRepository customerRepository;
    final
    CountRequestRepository countRequestRepository;
    final
    StaffArticleRepository staffArticleRepository;
    final
    DateHelper dateHelper;

    public StatisticalServiceImpl(ArticleRepository articleRepository, TransactionRepository transactionRepository, CustomerRepository customerRepository, CountRequestRepository countRequestRepository, StaffArticleRepository staffArticleRepository, DateHelper dateHelper) {
        this.articleRepository = articleRepository;
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
        this.countRequestRepository = countRequestRepository;
        this.staffArticleRepository = staffArticleRepository;
        this.dateHelper = dateHelper;
    }

    @Override
    public Map<String, Object> statisticArticle(Integer month, Integer year,
                                                Integer page, Integer limit) throws CustomException {
        if (month != null && year == null) {
            throw new CustomException("Lỗi có tháng thì phải có năm");
        } else if (month != null && year != null) {
            Calendar start = Calendar.getInstance();
            start.set(year, month - 1, 1, 0, 0, 0);
            Calendar end = Calendar.getInstance();
            end.set(year, month, 1, 0, 0, 0);
            Page<Object[]> objectsPage = articleRepository.countArticleOfDate(start.getTime(), end.getTime(), PageRequest.of(page, limit));
            List<Object[]> objectsList = objectsPage.toList();
            List<Map<String, Object>> returnList = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            for (Object[] object : objectsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", sdf.format((Date) object[0]));
                map.put("number", object[1]);
                returnList.add(map);
            }
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("pages", objectsPage.getTotalPages());
            returnMap.put("elements", objectsPage.getTotalElements());
            returnMap.put("data", returnList);
            return returnMap;
        } else if (month == null && year != null) {
            Calendar start = Calendar.getInstance();
            start.set(year, 0, 1, 0, 0, 0);
            Calendar end = Calendar.getInstance();
            end.set(year + 1, 0, 1, 0, 0, 0);
            Page<Object[]> objectsPage = articleRepository.countArticleOfMonth(start.getTime(), end.getTime(), PageRequest.of(page, limit));
            List<Object[]> objectsList = objectsPage.toList();
            List<Map<String, Object>> returnList = new ArrayList<>();
            for (Object[] object : objectsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("month", object[0]);
                map.put("number", object[1]);
                returnList.add(map);
            }
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("pages", objectsPage.getTotalPages());
            returnMap.put("elements", objectsPage.getTotalElements());
            returnMap.put("data", returnList);
            return returnMap;
        } else {
            Page<Object[]> objectsPage = articleRepository.countArticleOfYear(PageRequest.of(page, limit));
            List<Object[]> objectsList = objectsPage.toList();
            List<Map<String, Object>> returnList = new ArrayList<>();
            for (Object[] object : objectsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("year", object[0]);
                map.put("number", object[1]);
                returnList.add(map);
            }
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("pages", objectsPage.getTotalPages());
            returnMap.put("elements", objectsPage.getTotalElements());
            returnMap.put("data", returnList);
            return returnMap;
        }
    }

    @Override
    public Map<String, Object> statisticRevenue(Integer month, Integer year, Integer page, Integer limit) throws CustomException {
        if (month != null && year == null) {
            throw new CustomException("Lỗi có tháng thì phải có năm");
        } else if (month != null && year != null) {
            Calendar start = Calendar.getInstance();
            start.set(year, month - 1, 1, 0, 0, 0);
            Calendar end = Calendar.getInstance();
            end.set(year, month, 1, 0, 0, 0);
            Page<Object[]> objectsPage = transactionRepository.sumRevenueByDate(start.getTime(), end.getTime(), PageRequest.of(page, limit));
            List<Object[]> objectsList = objectsPage.toList();
            List<Map<String, Object>> returnList = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            for (Object[] object : objectsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", sdf.format((Date) object[0]));
                map.put("sum", object[1]);
                returnList.add(map);
            }
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("pages", objectsPage.getTotalPages());
            returnMap.put("elements", objectsPage.getTotalElements());
            returnMap.put("data", returnList);
            return returnMap;
        } else if (month == null && year != null) {
            Calendar start = Calendar.getInstance();
            start.set(year, 0, 1, 0, 0, 0);
            Calendar end = Calendar.getInstance();
            end.set(year + 1, 0, 1, 0, 0, 0);
            Page<Object[]> objectsPage = transactionRepository.sumRevenueByMonth(start.getTime(), end.getTime(), PageRequest.of(page, limit));
            List<Object[]> objectsList = objectsPage.toList();
            List<Map<String, Object>> returnList = new ArrayList<>();
            for (Object[] object : objectsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("month", object[0]);
                map.put("sum", object[1]);
                returnList.add(map);
            }
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("pages", objectsPage.getTotalPages());
            returnMap.put("elements", objectsPage.getTotalElements());
            returnMap.put("data", returnList);
            return returnMap;
        } else {
            Page<Object[]> objectsPage = transactionRepository.sumRevenueByYear(PageRequest.of(page, limit));
            List<Object[]> objectsList = objectsPage.toList();
            List<Map<String, Object>> returnList = new ArrayList<>();
            for (Object[] object : objectsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("year", object[0]);
                map.put("sum", object[1]);
                returnList.add(map);
            }
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("pages", objectsPage.getTotalPages());
            returnMap.put("elements", objectsPage.getTotalElements());
            returnMap.put("data", returnList);
            return returnMap;
        }
    }

    @Override
    public Map<String, Object> statisticCustomer(Integer month, Integer year, Integer page, Integer limit) throws CustomException {
        if (month != null && year == null) {
            throw new CustomException("Lỗi có tháng thì phải có năm");
        } else if (month != null && year != null) {
            Calendar start = Calendar.getInstance();
            start.set(year, month - 1, 1, 0, 0, 0);
            Calendar end = Calendar.getInstance();
            end.set(year, month, 1, 0, 0, 0);
            Page<Object[]> objectsPage = customerRepository.countCustomerOfDate(start.getTime(), end.getTime(), PageRequest.of(page, limit));
            List<Object[]> objectsList = objectsPage.toList();
            List<Map<String, Object>> returnList = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            for (Object[] object : objectsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", sdf.format((Date) object[0]));
                map.put("number", object[1]);
                returnList.add(map);
            }
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("pages", objectsPage.getTotalPages());
            returnMap.put("elements", objectsPage.getTotalElements());
            returnMap.put("data", returnList);
            return returnMap;
        } else if (month == null && year != null) {
            Calendar start = Calendar.getInstance();
            start.set(year, 0, 1, 0, 0, 0);
            Calendar end = Calendar.getInstance();
            end.set(year + 1, 0, 1, 0, 0, 0);
            Page<Object[]> objectsPage = customerRepository.countCustomerOfMonth(start.getTime(), end.getTime(), PageRequest.of(page, limit));
            List<Object[]> objectsList = objectsPage.toList();
            List<Map<String, Object>> returnList = new ArrayList<>();
            for (Object[] object : objectsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("month", object[0]);
                map.put("number", object[1]);
                returnList.add(map);
            }
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("pages", objectsPage.getTotalPages());
            returnMap.put("elements", objectsPage.getTotalElements());
            returnMap.put("data", returnList);
            return returnMap;
        } else {
            Page<Object[]> objectsPage = customerRepository.countCustomerOfYear(PageRequest.of(page, limit));
            List<Object[]> objectsList = objectsPage.toList();
            List<Map<String, Object>> returnList = new ArrayList<>();
            for (Object[] object : objectsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("year", object[0]);
                map.put("number", object[1]);
                returnList.add(map);
            }
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("pages", objectsPage.getTotalPages());
            returnMap.put("elements", objectsPage.getTotalElements());
            returnMap.put("data", returnList);
            return returnMap;
        }
    }

    @Override
    public Map<String, Object> statisticRequest(Integer month, Integer year, Integer page, Integer limit) throws CustomException {
        if (month != null && year == null) {
            throw new CustomException("Lỗi có tháng thì phải có năm");
        } else if (month != null && year != null) {
            Calendar start = Calendar.getInstance();
            start.set(year, month - 1, 1, 0, 0, 0);
            Calendar end = Calendar.getInstance();
            end.set(year, month, 1, 0, 0, 0);
            Page<Object[]> objectsPage = countRequestRepository.countCountRequestOfDate(start.getTime(), end.getTime(), PageRequest.of(page, limit));
            List<Object[]> objectsList = objectsPage.toList();
            List<Map<String, Object>> returnList = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            for (Object[] object : objectsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", sdf.format((Date) object[0]));
                map.put("number", object[1]);
                returnList.add(map);
            }
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("pages", objectsPage.getTotalPages());
            returnMap.put("elements", objectsPage.getTotalElements());
            returnMap.put("data", returnList);
            return returnMap;
        } else if (month == null && year != null) {
            Calendar start = Calendar.getInstance();
            start.set(year, 0, 1, 0, 0, 0);
            Calendar end = Calendar.getInstance();
            end.set(year + 1, 0, 1, 0, 0, 0);
            Page<Object[]> objectsPage = countRequestRepository.countCountRequestOfMonth(start.getTime(), end.getTime(), PageRequest.of(page, limit));
            List<Object[]> objectsList = objectsPage.toList();
            List<Map<String, Object>> returnList = new ArrayList<>();
            for (Object[] object : objectsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("month", object[0]);
                map.put("number", object[1]);
                returnList.add(map);
            }
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("pages", objectsPage.getTotalPages());
            returnMap.put("elements", objectsPage.getTotalElements());
            returnMap.put("data", returnList);
            return returnMap;
        } else {
            Page<Object[]> objectsPage = countRequestRepository.countCountRequestOfYear(PageRequest.of(page, limit));
            List<Object[]> objectsList = objectsPage.toList();
            List<Map<String, Object>> returnList = new ArrayList<>();
            for (Object[] object : objectsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("year", object[0]);
                map.put("number", object[1]);
                returnList.add(map);
            }
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("pages", objectsPage.getTotalPages());
            returnMap.put("elements", objectsPage.getTotalElements());
            returnMap.put("data", returnList);
            return returnMap;
        }
    }

    @Override
    public Map<String, Object> statisticStaffAction(String search, Long date, Integer month, Integer year, Integer page, Integer limit) throws CustomException {
        if (date == null && month == null && year == null) {
            date = new Date().getTime();
        }
        if (date != null) {
            Date start = dateHelper.changeTime(new Date(date), "00:00:00");
            Date end = dateHelper.changeTime(new Date(date + 24 * 3600 * 1000), "00:00:00");

            List<StaffArticle> staffArticles = staffArticleRepository.listStaffArticle(start, end, search, page, limit);
            List<StaffArticleOutputDTO> staffArticleOutputDTOS = new ArrayList<>();
            for (StaffArticle staffArticle : staffArticles) {
                staffArticleOutputDTOS.add(convertToOutputDTO(staffArticle));
            }

            Long elements = staffArticleRepository.countStaffArticle(start, end, search);
            Long pages = elements / limit;
            if (pages * limit < elements) pages++;

            Map<String, Object> map = new HashMap<>();
            map.put("data", staffArticleOutputDTOS);
            map.put("elements", elements);
            map.put("pages", pages);
            return map;
        } else if (month != null && year != null) {
            Calendar start = Calendar.getInstance();
            start.set(year, month - 1, 1, 0, 0, 0);
            Calendar end = Calendar.getInstance();
            end.set(year, month, 1, 0, 0, 0);

            List<StaffArticle> staffArticles = staffArticleRepository.listStaffArticle(start.getTime(), end.getTime(), search, page, limit);
            List<StaffArticleOutputDTO> staffArticleOutputDTOS = new ArrayList<>();
            for (StaffArticle staffArticle : staffArticles) {
                staffArticleOutputDTOS.add(convertToOutputDTO(staffArticle));
            }

            Long elements = staffArticleRepository.countStaffArticle(start.getTime(), end.getTime(), search);
            Long pages = elements / limit;
            if (pages * limit < elements) pages++;

            Map<String, Object> map = new HashMap<>();
            map.put("data", staffArticleOutputDTOS);
            map.put("elements", elements);
            map.put("pages", pages);
            return map;
        } else {
            throw new CustomException("Thống kê theo tháng phải nhập đủ cả năm và tháng");
        }
    }

    private StaffArticleOutputDTO convertToOutputDTO(StaffArticle staffArticle) {
        StaffArticleOutputDTO staffArticleOutputDTO = new StaffArticleOutputDTO();
        staffArticleOutputDTO.setId(staffArticle.getStaffArticleId());
        staffArticleOutputDTO.setAction(staffArticle.getAction());
        staffArticleOutputDTO.setTime(staffArticle.getTime().getTime());

        Staff staff = staffArticle.getStaff();
        Map<String, Object> staffMap = new HashMap<>();
        staffMap.put("staffId", staff.getStaffId());
        staffMap.put("name", staff.getName());
        staffMap.put("email", staff.getEmail());
        staffMap.put("phone", staff.getPhone());
        staffMap.put("image", staff.getImage());
        staffArticleOutputDTO.setStaff(staffMap);

        Article article = staffArticle.getArticle();
        Map<String, Object> articleMap = new HashMap<>();
        articleMap.put("articleId", article.getArticleId());
        articleMap.put("title", article.getTitle());
        articleMap.put("price", article.getRoomPrice());
        articleMap.put("timeUpdate", article.getUpdateTime().getTime());
        articleMap.put("image", article.getImage());
        articleMap.put("customerName", article.getCustomer().getName());
        staffArticleOutputDTO.setArticle(articleMap);

        return staffArticleOutputDTO;
    }
}
