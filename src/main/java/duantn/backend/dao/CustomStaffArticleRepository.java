package duantn.backend.dao;

import duantn.backend.model.entity.StaffArticle;

import java.util.Date;
import java.util.List;

/**
 * Created with YourComputer.
 * User: DUC_PRO
 * Date: 28/04/2021
 * Time: 12:38 CH
 */

public interface CustomStaffArticleRepository {
    List<StaffArticle> listStaffArticle(Date start, Date end, String search, Integer page, Integer limit);
    Long countStaffArticle(Date start, Date end, String search);
}
