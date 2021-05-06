package duantn.backend.dao.impl;

import duantn.backend.dao.CustomArticleRepository;
import duantn.backend.helper.VariableCommon;
import duantn.backend.model.entity.Article;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CustomArticleRepositoryImpl implements CustomArticleRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Article> findCustom(String sort, Long start, Long end,
                                    Integer ward, Integer district, Integer city,
                                    Boolean roommate,
                                    String status, Boolean vip, String search,
                                    Integer minAcreage, Integer maxAcreage,
                                    Integer minPrice, Integer maxPrice,
                                    Integer page, Integer limit) {
        if (search == null || search.trim().equals("")) search = "";

        //tạo builder
        CriteriaBuilder builder = em.getCriteriaBuilder();

        //tạo query
        CriteriaQuery<Article> query = builder.createQuery(Article.class);

        //xác định chủ thể cần truy vấn (=FROM Article)
        Root<Article> root = query.from(Article.class);

        //xác định cột trả về
        query.select(root);

        Predicate integrated = findIntegrated(root, builder,
                start, end, ward, district, city, roommate, status, vip, search, minAcreage, maxAcreage, minPrice, maxPrice);

        query.where(integrated);

        if (sort != null && !sort.trim().equals("")) {
            if (sort.equals("desc")) query.orderBy(builder.desc(root.get("updateTime")));
            else query.orderBy(builder.asc(root.get("updateTime")));
        }

        return em.createQuery(query).setFirstResult(page * limit).setMaxResults(limit).getResultList();
    }

    @Override
    public Map<String, Long> findCustomCount(Long start, Long end, Integer ward, Integer district, Integer city, Boolean roommate, String status, Boolean vip, String search, Integer minAcreage, Integer maxAcreage,Integer minPrice, Integer maxPrice, Integer limit) {
        if (search == null || search.trim().equals("")) search = "";
        CriteriaBuilder builder2 = em.getCriteriaBuilder();
        CriteriaQuery<Long> query2 = builder2.createQuery(Long.class);
        Root root2 = query2.from(Article.class);
        query2.select(builder2.count(root2));
        Predicate integrated2 = findIntegrated(root2, builder2,
                start, end, ward, district, city, roommate, status, vip, search, minAcreage, maxAcreage, minPrice, maxPrice);
        query2.where(integrated2);
        Long elements = em.createQuery(query2).getSingleResult();
        Long pages = elements / limit;
        if (pages * limit < elements) pages++;
        Map<String, Long> countMap = new HashMap<>();
        countMap.put("elements", elements);
        countMap.put("pages", pages);
        return countMap;
    }

    @Override
    public List<Article> findCustomAndEmail(String email, String sort, Long start, Long end, Integer ward, Integer district, Integer city, Boolean roommate, String status, Boolean vip, String search, Integer minAcreage, Integer maxAcreage, Integer minPrice, Integer maxPrice,Integer page, Integer limit) {
        if (search == null || search.trim().equals("")) search = "";

        //tạo builder
        CriteriaBuilder builder = em.getCriteriaBuilder();

        //tạo query
        CriteriaQuery<Article> query = builder.createQuery(Article.class);

        //xác định chủ thể cần truy vấn (=FROM Article)
        Root<Article> root = query.from(Article.class);

        //xác định cột trả về
        query.select(root);

        Predicate integrated = findCustomIntegrated(builder, root,
                email, start, end, ward, district, city, roommate, status, vip, search,
                minAcreage, maxAcreage, minPrice, maxPrice);

        query.where(integrated);

        if (sort != null && !sort.trim().equals("")) {
            if (sort.equals("desc")) query.orderBy(builder.desc(root.get("updateTime")));
            else query.orderBy(builder.asc(root.get("updateTime")));
        }

        return em.createQuery(query).setFirstResult(page * limit).setMaxResults(limit).getResultList();
    }

    @Override
    public Map<String, Long> findCustomAndEmailCount(String email, Long start, Long end, Integer ward, Integer district, Integer city, Boolean roommate, String status, Boolean vip, String search, Integer minAcreage, Integer maxAcreage, Integer minPrice, Integer maxPrice,Integer limit) {
        if (search == null || search.trim().equals("")) search = "";
        CriteriaBuilder builder2 = em.getCriteriaBuilder();
        CriteriaQuery<Long> query2 = builder2.createQuery(Long.class);
        Root root2 = query2.from(Article.class);
        query2.select(builder2.count(root2));
        Predicate integrated2 = findCustomIntegrated(builder2, root2,
                email, start, end, ward, district, city, roommate, status, vip, search,
                minAcreage, maxAcreage, minPrice, maxPrice);
        query2.where(integrated2);
        Long elements = em.createQuery(query2).getSingleResult();
        Long pages = elements / limit;
        if (pages * limit < elements) pages++;
        Map<String, Long> countMap = new HashMap<>();
        countMap.put("elements", elements);
        countMap.put("pages", pages);
        return countMap;
    }

    @Override
    public List<Article> findCustomShow(Boolean vip, Long start, Long end,
                                        Integer ward, Integer district, Integer city,
                                        Boolean roommate,
                                        String status, String search,
                                        Integer minAcreage, Integer maxAcreage,
                                        Integer minPrice, Integer maxPrice,
                                        Boolean sort,
                                        Integer page, Integer limit) {
        if (search == null || search.trim().equals("")) search = "";

        //tạo builder
        CriteriaBuilder builder = em.getCriteriaBuilder();

        //tạo query
        CriteriaQuery<Article> query = builder.createQuery(Article.class);

        //xác định chủ thể cần truy vấn (=FROM Article)
        Root<Article> root = query.from(Article.class);

        //xác định cột trả về
        query.select(root);

        Predicate integrated = findShowIntegrated(builder, root,
                vip, start, end, ward, district, city, roommate, status, search, minAcreage, maxAcreage, minPrice, maxPrice);

        query.where(integrated);

        if(sort){
            Order vipDesc = builder.desc(root.get("vip"));
            Order timeGroupAsc = builder.asc(root.get("timeGroup"));
            Order pointDesc=builder.desc(root.get("point"));
            if(vip==null) query.orderBy(timeGroupAsc, vipDesc, pointDesc);
            else query.orderBy(timeGroupAsc, pointDesc);
        }else {
            Order vipDesc = builder.desc(root.get("vip"));
            Order timeDesc = builder.desc(root.get("updateTime"));
            if(vip==null) query.orderBy(vipDesc, timeDesc);
            else query.orderBy(timeDesc);
        }

        return em.createQuery(query).setFirstResult(page * limit).setMaxResults(limit).getResultList();
    }

    @Override
    public Map<String, Long> findCustomShowCount(Boolean vip, Long start, Long end, Integer ward, Integer district, Integer city, Boolean roommate, String status, String search, Integer minAcreage, Integer maxAcreage, Integer minPrice, Integer maxPrice, Integer limit) {
        if (search == null || search.trim().equals("")) search = "";
        CriteriaBuilder builder2 = em.getCriteriaBuilder();
        CriteriaQuery<Long> query2 = builder2.createQuery(Long.class);
        Root root2 = query2.from(Article.class);
        query2.select(builder2.count(root2));
        Predicate integrated2 = findShowIntegrated(builder2, root2,
                vip, start, end, ward, district, city, roommate, status, search, minAcreage, maxAcreage, minPrice, maxPrice);
        query2.where(integrated2);
        Long elements = em.createQuery(query2).getSingleResult();
        Long pages = elements / limit;
        if (pages * limit < elements) pages++;
        Map<String, Long> countMap = new HashMap<>();
        countMap.put("elements", elements);
        countMap.put("pages", pages);
        return countMap;
    }

    private Predicate findIntegrated(Root root, CriteriaBuilder builder, Long start, Long end,
                                     Integer ward, Integer district, Integer city,
                                     Boolean roommate,
                                     String status, Boolean vip, String search,
                                     Integer minAcreage, Integer maxAcreage,
                                     Integer minPrice, Integer maxPrice) {
        //search
        Predicate searchByName = builder.like(root.get("customer").get("name"), "%" + search + "%");
        Predicate searchByPhone = builder.like(root.get("customer").get("phone"), "%" + search + "%");
        Predicate searchByEmail = builder.like(root.get("customer").get("email"), "%" + search + "%");
        Predicate searchByTitle = builder.like(root.get("title"), "%" + search + "%");
        searchByTitle = builder.or(searchByTitle, searchByEmail, searchByPhone, searchByName);

        //tìm khoảng thời gian
        if (start != null) {
            Predicate findByGreaterTime = builder.greaterThanOrEqualTo(root.<Date>get("updateTime"), new Date(start));
            searchByTitle = builder.and(searchByTitle, findByGreaterTime);
        }
        if (end != null) {
            Predicate findByLessTime = builder.lessThanOrEqualTo(root.<Date>get("updateTime"), new Date(end));
            searchByTitle = builder.and(searchByTitle, findByLessTime);
        }

        //lọc theo diện tích
        if (minAcreage != null) {
            Predicate findByGreaterAcreage = builder.greaterThanOrEqualTo(root.get("acreage"), minAcreage);
            searchByTitle = builder.and(searchByTitle, findByGreaterAcreage);
        }
        if (maxAcreage != null) {
            Predicate findByLessAcreage = builder.lessThanOrEqualTo(root.get("acreage"), maxAcreage);
            searchByTitle = builder.and(searchByTitle, findByLessAcreage);
        }

        //tìm theo xã, huyện, tỉnh
        if (ward != null) {
            Predicate findByWard = builder.equal(root.get("ward").get("wardId"), ward);
            searchByTitle = builder.and(searchByTitle, findByWard);
        } else if (district != null) {
            Predicate findByDistrict = builder.equal(root.get("ward").get("district").get("districtId"), district);
            searchByTitle = builder.and(searchByTitle, findByDistrict);
        } else if (city != null) {
            Predicate findByCity = builder.equal(root.get("ward").get("district").get("city").get("cityId"), city);
            searchByTitle = builder.and(searchByTitle, findByCity);
        }

        //tìm theo roommate
        if (roommate != null) {
            if (roommate) {
                Predicate findByRoommateNotNull = builder.isNotNull(root.get("roommate"));
                searchByTitle = builder.and(searchByTitle, findByRoommateNotNull);
            } else {
                Predicate findByRoommateNull = builder.isNull(root.get("roommate"));
                searchByTitle = builder.and(searchByTitle, findByRoommateNull);
            }
        }

        //tìm theo status
        if (status != null && !status.trim().equals("")) {
            if (status.equals("uncheck")) {
                Predicate findByStatusNull = builder.like(root.get("status"), VariableCommon.CHUA_DUYET);
                Predicate findByStatusNull1 = builder.like(root.get("status"), VariableCommon.DA_SUA);
                Predicate findByABC=builder.or(findByStatusNull, findByStatusNull1);
                searchByTitle = builder.and(searchByTitle, findByABC);
            } else if (status.equals("active")) {
                Predicate findByStatusTrue = builder.like(root.get("status"), VariableCommon.DANG_DANG);
                searchByTitle = builder.and(searchByTitle, findByStatusTrue);
            } else if (status.equals("hidden")) {
                Predicate findByStatusFalse = builder.like(root.get("status"), VariableCommon.BI_AN);
                searchByTitle = builder.and(searchByTitle, findByStatusFalse);
            } else if (status.equals("expired")) {
                Predicate findByStatusFalse = builder.like(root.get("status"), VariableCommon.HET_HAN);
                searchByTitle = builder.and(searchByTitle, findByStatusFalse);
            } else if (status.equals("suggest-fix")) {
                Predicate findByStatusFalse = builder.like(root.get("status"), VariableCommon.SUA_LAI);
                searchByTitle = builder.and(searchByTitle, findByStatusFalse);
            }
        }

        //tìm theo vip
        if (vip != null) {
            Predicate findByVip = builder.equal(root.get("vip"), vip);
            searchByTitle = builder.and(searchByTitle, findByVip);
        }

        //tìm theo price
        if (minPrice != null) {
            Predicate findByGreaterPrice = builder.greaterThanOrEqualTo(root.get("roomPrice"), minPrice);
            searchByTitle = builder.and(searchByTitle, findByGreaterPrice);
        }
        if (maxPrice != null) {
            Predicate findByLessPrice = builder.lessThanOrEqualTo(root.get("roomPrice"), maxPrice);
            searchByTitle = builder.and(searchByTitle, findByLessPrice);
        }

        //tìm theo deleted
        Predicate findByDeleted=builder.isFalse(root.get("deleted"));
        searchByTitle=builder.and(searchByTitle, findByDeleted);

        return searchByTitle;
    }

    private Predicate findCustomIntegrated(CriteriaBuilder builder, Root root,
                                           String email, Long start, Long end, Integer ward,
                                           Integer district, Integer city, Boolean roommate,
                                           String status, Boolean vip, String search,
                                           Integer minAcreage, Integer maxAcreage,
                                           Integer minPrice, Integer maxPrice) {
        //search
        Predicate searchByTitle = builder.like(root.get("title"), "%" + search + "%");

        //phải đúng email
        Predicate findByEmail = builder.like(root.get("customer").get("email"), email);
        searchByTitle = builder.and(searchByTitle, findByEmail);


        //tìm khoảng thời gian
        if (start != null) {
            Predicate findByGreaterTime = builder.greaterThanOrEqualTo(root.<Date>get("updateTime"), new Date(start));
            searchByTitle = builder.and(searchByTitle, findByGreaterTime);
        }
        if (end != null) {
            Predicate findByLessTime = builder.lessThanOrEqualTo(root.<Date>get("updateTime"), new Date(end));
            searchByTitle = builder.and(searchByTitle, findByLessTime);
        }

        //lọc theo diện tích
        if (minAcreage != null) {
            Predicate findByGreaterAcreage = builder.greaterThanOrEqualTo(root.get("acreage"), minAcreage);
            searchByTitle = builder.and(searchByTitle, findByGreaterAcreage);
        }
        if (maxAcreage != null) {
            Predicate findByLessAcreage = builder.lessThanOrEqualTo(root.get("acreage"), maxAcreage);
            searchByTitle = builder.and(searchByTitle, findByLessAcreage);
        }

        //tìm theo xã, huyện, tỉnh
        if (ward != null) {
            Predicate findByWard = builder.equal(root.get("ward").get("wardId"), ward);
            searchByTitle = builder.and(searchByTitle, findByWard);
        } else if (district != null) {
            Predicate findByDistrict = builder.equal(root.get("ward").get("district").get("districtId"), district);
            searchByTitle = builder.and(searchByTitle, findByDistrict);
        } else if (city != null) {
            Predicate findByCity = builder.equal(root.get("ward").get("district").get("city").get("cityId"), city);
            searchByTitle = builder.and(searchByTitle, findByCity);
        }

        //tìm theo roommate
        if (roommate != null) {
            if (roommate) {
                Predicate findByRoommateNotNull = builder.isNotNull(root.get("roommate"));
                searchByTitle = builder.and(searchByTitle, findByRoommateNotNull);
            } else {
                Predicate findByRoommateNull = builder.isNull(root.get("roommate"));
                searchByTitle = builder.and(searchByTitle, findByRoommateNull);
            }
        }

        //tìm theo status
        if (status != null && !status.trim().equals("")) {
            if (status.equals("uncheck")) {
                Predicate findByStatusNull = builder.like(root.get("status"), VariableCommon.CHUA_DUYET);
                Predicate findByStatusNull1 = builder.like(root.get("status"), VariableCommon.DA_SUA);
                Predicate findByABC=builder.or(findByStatusNull, findByStatusNull1);
                searchByTitle = builder.and(searchByTitle, findByABC);
            } else if (status.equals("active")) {
                Predicate findByStatusTrue = builder.like(root.get("status"), VariableCommon.DANG_DANG);
                searchByTitle = builder.and(searchByTitle, findByStatusTrue);
            } else if (status.equals("hidden")) {
                Predicate findByStatusFalse = builder.like(root.get("status"), VariableCommon.BI_AN);
                searchByTitle = builder.and(searchByTitle, findByStatusFalse);
            } else if (status.equals("expired")) {
                Predicate findByStatusFalse = builder.like(root.get("status"), VariableCommon.HET_HAN);
                searchByTitle = builder.and(searchByTitle, findByStatusFalse);
            } else if (status.equals("suggest-fix")) {
                Predicate findByStatusFalse = builder.like(root.get("status"), VariableCommon.SUA_LAI);
                searchByTitle = builder.and(searchByTitle, findByStatusFalse);
            }
        }

        //tìm theo vip
        if (vip != null) {
            Predicate findByVip = builder.equal(root.get("vip"), vip);
            searchByTitle = builder.and(searchByTitle, findByVip);
        }

        //tìm theo price
        if (minPrice != null) {
            Predicate findByGreaterPrice = builder.greaterThanOrEqualTo(root.get("roomPrice"), minPrice);
            searchByTitle = builder.and(searchByTitle, findByGreaterPrice);
        }
        if (maxPrice != null) {
            Predicate findByLessPrice = builder.lessThanOrEqualTo(root.get("roomPrice"), maxPrice);
            searchByTitle = builder.and(searchByTitle, findByLessPrice);
        }

        //tìm theo deleted
        Predicate findByDeleted=builder.isFalse(root.get("deleted"));
        searchByTitle=builder.and(searchByTitle, findByDeleted);

        return searchByTitle;
    }

    private Predicate findShowIntegrated(CriteriaBuilder builder,
                                         Root root, Boolean vip, Long start, Long end,
                                         Integer ward, Integer district, Integer city,
                                         Boolean roommate,
                                         String status, String search,
                                         Integer minAcreage, Integer maxAcreage,
                                         Integer minPrice, Integer maxPrice) {
        //search
        Predicate searchByName = builder.like(root.get("customer").get("name"), "%" + search + "%");
        Predicate searchByPhone = builder.like(root.get("customer").get("phone"), "%" + search + "%");
        Predicate searchByEmail = builder.like(root.get("customer").get("email"), "%" + search + "%");
        Predicate searchByTitle = builder.like(root.get("title"), "%" + search + "%");
        searchByTitle = builder.or(searchByTitle, searchByEmail, searchByPhone, searchByName);

        //tìm khoảng thời gian
        if (start != null) {
            Predicate findByGreaterTime = builder.greaterThanOrEqualTo(root.<Date>get("updateTime"), new Date(start));
            searchByTitle = builder.and(searchByTitle, findByGreaterTime);
        }
        if (end != null) {
            Predicate findByLessTime = builder.lessThanOrEqualTo(root.<Date>get("updateTime"), new Date(end));
            searchByTitle = builder.and(searchByTitle, findByLessTime);
        }

        //lọc theo diện tích
        if (minAcreage != null) {
            Predicate findByGreaterAcreage = builder.greaterThanOrEqualTo(root.get("acreage"), minAcreage);
            searchByTitle = builder.and(searchByTitle, findByGreaterAcreage);
        }
        if (maxAcreage != null) {
            Predicate findByLessAcreage = builder.lessThanOrEqualTo(root.get("acreage"), maxAcreage);
            searchByTitle = builder.and(searchByTitle, findByLessAcreage);
        }

        //tìm theo xã, huyện, tỉnh
        if (ward != null) {
            Predicate findByWard = builder.equal(root.get("ward").get("wardId"), ward);
            searchByTitle = builder.and(searchByTitle, findByWard);
        } else if (district != null) {
            Predicate findByDistrict = builder.equal(root.get("ward").get("district").get("districtId"), district);
            searchByTitle = builder.and(searchByTitle, findByDistrict);
        } else if (city != null) {
            Predicate findByCity = builder.equal(root.get("ward").get("district").get("city").get("cityId"), city);
            searchByTitle = builder.and(searchByTitle, findByCity);
        }

        //tìm theo roommate
        if (roommate != null) {
            if (roommate) {
                Predicate findByRoommateNotNull = builder.isNotNull(root.get("roommate"));
                searchByTitle = builder.and(searchByTitle, findByRoommateNotNull);
            } else {
                Predicate findByRoommateNull = builder.isNull(root.get("roommate"));
                searchByTitle = builder.and(searchByTitle, findByRoommateNull);
            }
        }

        //tìm theo status
        if (status != null && !status.trim().equals("")) {
            if (status.equals("uncheck")) {
                Predicate findByStatusNull = builder.like(root.get("status"), VariableCommon.CHUA_DUYET);
                Predicate findByStatusNull1 = builder.like(root.get("status"), VariableCommon.DA_SUA);
                Predicate findByABC=builder.or(findByStatusNull, findByStatusNull1);
                searchByTitle = builder.and(searchByTitle, findByABC);
            } else if (status.equals("active")) {
                Predicate findByStatusTrue = builder.like(root.get("status"), VariableCommon.DANG_DANG);
                searchByTitle = builder.and(searchByTitle, findByStatusTrue);
            } else if (status.equals("hidden")) {
                Predicate findByStatusFalse = builder.like(root.get("status"), VariableCommon.BI_AN);
                searchByTitle = builder.and(searchByTitle, findByStatusFalse);
            } else if (status.equals("expired")) {
                Predicate findByStatusFalse = builder.like(root.get("status"), VariableCommon.HET_HAN);
                searchByTitle = builder.and(searchByTitle, findByStatusFalse);
            } else if (status.equals("suggest-fix")) {
                Predicate findByStatusFalse = builder.like(root.get("status"), VariableCommon.SUA_LAI);
                searchByTitle = builder.and(searchByTitle, findByStatusFalse);
            }
        }

        //tìm theo vip
        if(vip!=null){
            Predicate findByVip= builder.equal(root.get("vip"), vip);
            searchByTitle=builder.and(searchByTitle, findByVip);
        }

        //tìm theo price
        if (minPrice != null) {
            Predicate findByGreaterPrice = builder.greaterThanOrEqualTo(root.get("roomPrice"), minPrice);
            searchByTitle = builder.and(searchByTitle, findByGreaterPrice);
        }
        if (maxPrice != null) {
            Predicate findByLessPrice = builder.lessThanOrEqualTo(root.get("roomPrice"), maxPrice);
            searchByTitle = builder.and(searchByTitle, findByLessPrice);
        }

        //tìm theo deleted
        Predicate findByDeleted=builder.isFalse(root.get("deleted"));
        searchByTitle=builder.and(searchByTitle, findByDeleted);

        return searchByTitle;
    }
}
