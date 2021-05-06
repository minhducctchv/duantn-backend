package duantn.backend.dao.impl;

import duantn.backend.dao.CustomStaffArticleRepository;
import duantn.backend.model.entity.Article;
import duantn.backend.model.entity.StaffArticle;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.Date;
import java.util.List;

/**
 * Created with YourComputer.
 * User: DUC_PRO
 * Date: 28/04/2021
 * Time: 12:40 CH
 */

@Repository
public class CustomStaffArticleRepositoryImpl implements CustomStaffArticleRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<StaffArticle> listStaffArticle(Date start, Date end, String search, Integer page, Integer limit) {
        //tạo builder
        CriteriaBuilder builder = em.getCriteriaBuilder();

        //tạo query
        CriteriaQuery<StaffArticle> query = builder.createQuery(StaffArticle.class);

        //xác định chủ thể cần truy vấn (=FROM Article)
        Root<StaffArticle> root = query.from(StaffArticle.class);

        //xác định cột trả về
        query.select(root);

        Predicate integrated = createPredicate(root, builder,
                start, end, search);

        query.where(integrated);

        Order staffNameAsc = builder.asc(root.get("staff").get("name"));
        Order timeAsc = builder.asc(root.get("time"));
        query.orderBy(staffNameAsc, timeAsc);

        return em.createQuery(query).setFirstResult(page*limit).setMaxResults(limit).getResultList();
    }

    @Override
    public Long countStaffArticle(Date start, Date end, String search) {
        //tạo builder
        CriteriaBuilder builder = em.getCriteriaBuilder();

        //tạo query
        CriteriaQuery<Long> query = builder.createQuery(Long.class);

        //xác định chủ thể cần truy vấn (=FROM Article)
        Root root = query.from(StaffArticle.class);

        //xác định cột trả về
        query.select(builder.count(root));

        Predicate integrated = createPredicate(root, builder,
                start, end, search);

        query.where(integrated);

        return em.createQuery(query).getSingleResult();
    }

    private Predicate createPredicate(Root root, CriteriaBuilder builder,
                                      Date start, Date end, String search){
        if(search==null || search.trim().equals("")) search="";

        Predicate searchByName=builder.like(root.get("staff").get("name"), "%"+search+"%");
        Predicate searchByPhone=builder.like(root.get("staff").get("phone"), "%"+search+"%");
        Predicate searchByEmail=builder.like(root.get("staff").get("email"), "%"+search+"%");
        searchByName=builder.or(searchByName, searchByPhone, searchByEmail);

        Predicate findByStart=builder.greaterThanOrEqualTo(root.<Date>get("time"), start);
        Predicate findByEnd=builder.lessThan(root.<Date>get("time"), end);
        searchByName=builder.and(searchByName, findByStart, findByEnd);

        return searchByName;
    }
}
