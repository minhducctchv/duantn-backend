package duantn.backend.dao;

import duantn.backend.model.entity.Article;
import duantn.backend.model.entity.Customer;
import duantn.backend.model.entity.FavoriteArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteArticleRepository extends JpaRepository<FavoriteArticle, Integer> {
    Page<FavoriteArticle> findByCustomer_Email(String email, Pageable pageable);

    FavoriteArticle findByCustomer_EmailAndArticle_ArticleId(String email, Integer id);

    List<FavoriteArticle> findByArticle(Article article);

    Long countByArticle(Article article);

}
