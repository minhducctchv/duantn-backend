package duantn.backend.dao;

import duantn.backend.helper.Helper;
import duantn.backend.model.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    Page<Staff> findByNameLikeAndDeletedFalseAndEnabledTrueOrEmailLikeAndDeletedFalseAndEnabledTrueOrPhoneLikeAndDeletedFalseAndEnabledTrue(
            String name, String email, String phone, Pageable pageable
    );
    Page<Staff> findByNameLikeAndDeletedTrueAndEnabledTrueOrEmailLikeAndDeletedTrueAndEnabledTrueOrPhoneLikeAndDeletedTrueAndEnabledTrue(
            String name, String email, String phone, Pageable pageable
    );
    Page<Staff> findByNameLikeAndEnabledTrueOrEmailLikeAndEnabledTrueOrPhoneLikeAndEnabledTrue(
            String name, String email, String phone, Pageable pageable
    );

    Staff findByStaffIdAndDeletedFalseAndEnabledTrue(Integer staffId);

    List<Staff> findByDeletedTrueAndEnabledTrue();

    Staff findByEmail(String email);

    Staff findByToken(String token);

    List<Staff> findByEnabledFalseAndTimeCreatedLessThanEqual(Date date);

    Staff findByEnabledTrueAndStaffId(Integer id);

    Optional<Staff> findByStaffIdAndEnabledFalse(Integer id);
}
