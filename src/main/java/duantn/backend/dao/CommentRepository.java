package duantn.backend.dao;

import duantn.backend.model.entity.Article;
import duantn.backend.model.entity.Comment;
import duantn.backend.model.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created with YourComputer.
 * User: DUC_PRO
 * Date: 28/04/2021
 * Time: 5:08 CH
 */

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    Comment findByArticleAndCustomer(Article article, Customer customer);
    Page<Comment> findByArticle_ArticleId(Integer articleId, Pageable pageable);
    List<Comment> findByArticle(Article article);
}
