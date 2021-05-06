package duantn.backend.dao;

import duantn.backend.model.entity.CountRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created with YourComputer.
 * User: DUC_PRO
 * Date: 28/04/2021
 * Time: 1:19 SA
 */

@Repository
public interface CountRequestRepository extends JpaRepository<CountRequest, Date> {
    CountRequest findFirstBy(Sort sort);

    @Query("SELECT date(a.date) as ngay, sum(a.count) as number from CountRequest a WHERE a.date >= :start and a.date < :end group by ngay order by ngay asc")
    Page<Object[]> countCountRequestOfDate(@Param("start") Date start, @Param("end") Date end, Pageable pageable);

    @Query("SELECT month(a.date) as month, sum(a.count) as number from CountRequest a WHERE a.date >= :start and a.date < :end group by month order by month asc")
    Page<Object[]> countCountRequestOfMonth(@Param("start") Date start, @Param("end") Date end, Pageable pageable);

    @Query("SELECT year(a.date) as year, sum(a.count) as number from CountRequest a group by year order by year asc")
    Page<Object[]> countCountRequestOfYear(Pageable pageable);
}
