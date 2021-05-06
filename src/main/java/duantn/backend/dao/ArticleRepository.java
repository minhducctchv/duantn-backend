
package duantn.backend.dao;

import duantn.backend.model.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer>, CustomArticleRepository{
    Article findByArticleIdAndDeletedFalse(Integer id);

    List<Article> findByStatusAndExpTimeBeforeAndDeletedFalse(String status, Date date);

    List<Article> findByStatusAndDeletedFalse(String status);

    List<Article> findByStatusAndExpTimeBetweenAndDeletedFalse(String status, Date start, Date end);

    @Query("SELECT date(a.timeCreated) as date, count(a) as number from Article a WHERE a.timeCreated >= :start and a.timeCreated < :end group by date order by date asc")
    Page<Object[]> countArticleOfDate(@Param("start") Date start, @Param("end") Date end, Pageable pageable);

    @Query("SELECT month(a.timeCreated) as month, count(a) as number from Article a WHERE a.timeCreated >= :start and a.timeCreated < :end group by month order by month asc")
    Page<Object[]> countArticleOfMonth(@Param("start") Date start, @Param("end") Date end, Pageable pageable);

    @Query("SELECT year(a.timeCreated) as year, count(a) as number from Article a group by year order by year asc")
    Page<Object[]> countArticleOfYear(Pageable pageable);
}
