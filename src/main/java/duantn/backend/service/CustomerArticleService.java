package duantn.backend.service;

import duantn.backend.authentication.CustomException;
import duantn.backend.model.dto.input.ArticleInsertDTO;
import duantn.backend.model.dto.input.ArticleUpdateDTO;
import duantn.backend.model.dto.output.ArticleOutputDTO;
import duantn.backend.model.dto.output.Message;

import java.util.List;
import java.util.Map;

public interface CustomerArticleService {
    //    list bài đăng cá nhân	/customer/article
//    lọc bài đăng theo trạng thái: chưa duyệt, đang đăng, đã ẩn	/customer/article?status={uncheck/active/hidden}
//    tìm kiếm bài đăng theo title	/customer/article?title={title}
    List<ArticleOutputDTO> listArticle(String email, String sort, Long start, Long end,
                                       Integer ward, Integer district, Integer city,
                                       Boolean roommate,
                                       String status, Boolean vip, String search,
                                       Integer minAcreage, Integer maxAcreage,
                                       Integer minPrice, Integer maxPrice,
                                       Integer page, Integer limit);

    //    đăng bài	/customer/article
    ArticleOutputDTO insertArticle(String email, ArticleInsertDTO articleInsertDTO)
            throws CustomException;

    //    sửa bài đăng	/customer/article
    ArticleOutputDTO updateArticle(String email, ArticleUpdateDTO articleUpdateDTO,
                                   Integer id)
            throws CustomException;

    //    ẩn bài đăng	/customer/article/hidden/{id}
    Message hiddenArticle(String email, Integer id) throws CustomException;

    //    xóa bài đăng	/customer/article/{id}
    Message deleteArticle(String email, Integer id) throws CustomException;

    //    gia hạn bài đăng	/customer/article/extension/{id}?date={int}
    Message extensionExp(String email, Integer id, Integer date, String type) throws CustomException;

    //đăng lại bài đăng đã ẩn	/customer/article/post/{id}?days={int}
    Message postOldArticle(String email, Integer Id, Integer date, String type, Boolean vip) throws CustomException;

    //chi tiết bài đăng	/customer/article/{id}
    ArticleOutputDTO detailArticle(String email, Integer id) throws CustomException;

    Message buffPoint(String email, Integer id, Integer point) throws CustomException;

    Map<String, Object> showPoint(Integer id) throws CustomException;
}
