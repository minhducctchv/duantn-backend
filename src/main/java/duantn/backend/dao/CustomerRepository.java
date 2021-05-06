package duantn.backend.dao;

import duantn.backend.model.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Customer findByEmail(String email);

    Customer findByToken(String token);

    Page<Customer> findByNameLikeAndEnabledTrueAndDeletedTrueOrPhoneLikeAndEnabledTrueAndDeletedTrueOrEmailLikeAndEnabledTrueAndDeletedTrue
            (String name, String phone, String email, Pageable pageable);

    Page<Customer> findByNameLikeAndEnabledTrueAndDeletedFalseOrPhoneLikeAndEnabledTrueAndDeletedFalseOrEmailLikeAndEnabledTrueAndDeletedFalse
            (String name, String phone, String email, Pageable pageable);

    Page<Customer> findByNameLikeAndEnabledTrueOrPhoneLikeAndEnabledTrueOrEmailLikeAndEnabledTrue
            (String name, String phone, String email, Pageable pageable);

    Customer findByCustomerIdAndDeletedFalseAndEnabledTrue(Integer id);

    List<Customer> findByDeletedTrueAndEnabledTrue();

    List<Customer> findByEnabledFalseAndTimeCreatedLessThanEqual(Date date);

    Customer findByCustomerIdAndEnabledTrue(Integer id);

    Optional<Customer> findByCustomerIdAndEnabledFalse(Integer id);

    @Query("SELECT date(a.timeCreated) as date, count(a) as number from Customer a WHERE a.timeCreated >= :start and a.timeCreated < :end group by date order by date asc")
    Page<Object[]> countCustomerOfDate(@Param("start") Date start, @Param("end") Date end, Pageable pageable);

    @Query("SELECT month(a.timeCreated) as month, count(a) as number from Customer a WHERE a.timeCreated >= :start and a.timeCreated < :end group by month order by month asc")
    Page<Object[]> countCustomerOfMonth(@Param("start") Date start, @Param("end") Date end, Pageable pageable);

    @Query("SELECT year(a.timeCreated) as year, count(a) as number from Customer a group by year order by year asc")
    Page<Object[]> countCustomerOfYear(Pageable pageable);
}
