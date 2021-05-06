package duantn.backend.dao;

import duantn.backend.model.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WardRepository extends JpaRepository<Ward, Integer> {
    List<Ward> findByDistrict_DistrictId(Integer id);
}
