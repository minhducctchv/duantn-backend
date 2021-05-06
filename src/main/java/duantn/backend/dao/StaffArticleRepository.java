package duantn.backend.dao;

import duantn.backend.model.entity.Article;
import duantn.backend.model.entity.StaffArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffArticleRepository extends JpaRepository<StaffArticle, Integer>, CustomStaffArticleRepository {
    StaffArticle findFirstByArticle_ArticleId(Integer id, Sort sort);
    List<StaffArticle> findByArticle(Article article);

}
