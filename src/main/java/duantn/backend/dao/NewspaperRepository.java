package duantn.backend.dao;

import duantn.backend.model.entity.Newspaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewspaperRepository extends JpaRepository<Newspaper, Integer> {
    Page<Newspaper> findByTitleLikeAndDeleted(String title, Boolean deleted, Pageable pageable);
    Page<Newspaper> findByTitleLike(String title, Pageable pageable);
}
