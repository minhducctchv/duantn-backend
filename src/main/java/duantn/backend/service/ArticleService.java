package duantn.backend.service;

import duantn.backend.authentication.CustomException;
import duantn.backend.model.dto.input.ArticleInsertDTO;
import duantn.backend.model.dto.input.ArticleUpdateDTO;
import duantn.backend.model.dto.input.ContactCustomerDTO;
import duantn.backend.model.dto.output.ArticleOutputDTO;
import duantn.backend.model.dto.output.Message;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ArticleService {
    List<ArticleOutputDTO> listArticle(
            String sort,
            Long start, Long end,
            Integer ward,
            Integer district,
            Integer city,
            Boolean roommate,
            String status,
            Boolean vip,
            String search,
            Integer minAcreage, Integer maxAcreage,
            Integer minPrice, Integer maxPrice,
            Integer page,
            Integer limit
    );

    //    contact với khách hàng (gửi mail cho khách hàng về bài viết này)	/admin/article/contact/{id}
    Message contactToCustomer(Integer id, ContactCustomerDTO contactCustomerDTO,
                              HttpServletRequest request) throws CustomException;

    //    duyệt bài đăng (hiện) (gửi mail)	/admin/article/active/{id}
    Message activeArticle(Integer id, HttpServletRequest request) throws CustomException;

    //duyệt bài: yêu cầu sửa lại
    Message suggestCorrectingArticle(Integer id, String reason, HttpServletRequest request) throws CustomException;

    //    ẩn bài đăng (gửi mail)	/admin/article/block/{id}
    Message hiddenArticle(Integer id, String reason, HttpServletRequest request) throws CustomException;

    //chi tiết bài đăng
    ArticleOutputDTO detailArticle(Integer id) throws CustomException;

    //đăng bài
    ArticleOutputDTO insertArticle(String email, ArticleInsertDTO articleInsertDTO)
            throws CustomException;

    //    sửa bài đăng	/customer/article
    ArticleOutputDTO updateArticle(String email, ArticleUpdateDTO articleUpdateDTO,
                                   Integer id)
            throws CustomException;

    //    gia hạn bài đăng	/customer/article/extension/{id}?date={int}
    Message extensionExp(String email, Integer id, Integer date, String type) throws CustomException;

    //đăng lại bài đăng đã ẩn	/customer/article/post/{id}?days={int}
    Message postOldArticle(String email, Integer Id, Integer date, String type, Boolean vip) throws CustomException;

    Message buffPoint(String email, Integer id, Integer point) throws CustomException;
}
