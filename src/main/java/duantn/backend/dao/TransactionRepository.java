package duantn.backend.dao;

import duantn.backend.model.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Page<Transaction> findByCustomer_Email(String email, Pageable pageable);
    Page<Transaction> findByCustomer_EmailAndType(String email, Boolean type, Pageable pageable);

    @Query("SELECT date(t.timeCreated) as date, sum(t.amount) as sum from Transaction t WHERE t.timeCreated >= :start and t.timeCreated < :end and t.type = true group by date order by date asc")
    Page<Object[]> sumRevenueByDate(@Param("start") Date start, @Param("end") Date end, Pageable pageable);

    @Query("SELECT month(t.timeCreated) as month, sum(t.amount) as sum from Transaction t WHERE t.timeCreated >= :start and t.timeCreated < :end and t.type = true group by month order by month asc")
    Page<Object[]> sumRevenueByMonth(@Param("start") Date start, @Param("end") Date end, Pageable pageable);

    @Query("SELECT year(t.timeCreated) as year, sum(t.amount) as sum from Transaction t WHERE t.type = true group by year order by year asc")
    Page<Object[]> sumRevenueByYear(Pageable pageable);
}
