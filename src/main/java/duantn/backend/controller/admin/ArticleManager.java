package duantn.backend.controller.admin;

import duantn.backend.authentication.CustomException;
import duantn.backend.helper.Helper;
import duantn.backend.model.dto.input.ArticleInsertDTO;
import duantn.backend.model.dto.input.ArticleUpdateDTO;
import duantn.backend.model.dto.input.ContactCustomerDTO;
import duantn.backend.model.dto.output.ArticleOutputDTO;
import duantn.backend.model.dto.output.Message;
import duantn.backend.service.ArticleService;
import duantn.backend.service.CustomerArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/")
public class ArticleManager {
    final
    ArticleService articleService;
    final
    Helper helper;
    final
    CustomerArticleService customerArticleService;

    public ArticleManager(ArticleService articleService, Helper helper, CustomerArticleService customerArticleService) {
        this.articleService = articleService;
        this.helper = helper;
        this.customerArticleService = customerArticleService;
    }

    //    list bài đăng	/admin/article
//    xếp bài đăng theo ngày tăng dần	/admin/article?sort=asc
//    xếp bài đăng theo ngày giảm dần	/admin/article?sort=desc
//    lọc bài đăng theo khoảng thời gian	/admin/article?start={millisecond}&end={millisecond}
//    lọc bài đăng theo thành phố / huyện / phường	/admin/article?city= hoặc district= hoặc ward=
//    lọc bài đang theo loại: thuê phòng/ ở ghép	/admin/article?roommate={true/false}
//    lọc bài đăng theo trạng thái: chưa duyệt/ đã duyệt/ đã ẩn	/admin/article?status={uncheck/activated/hidden}
//    lọc bài đăng theo isVip	/admin/article?vip={true/false}
//    tìm kiếm bài đăng theo title/customer-name/customer-phone	/admin/article?search={search}
    @GetMapping("/article")
    public List<ArticleOutputDTO> listArticle(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long start, @RequestParam(required = false) Long end,
            @RequestParam(required = false) Integer ward,
            @RequestParam(required = false) Integer district,
            @RequestParam(required = false) Integer city,
            @RequestParam(required = false) Boolean roommate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean vip,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer minAcreage,
            @RequestParam(required = false) Integer maxAcreage,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam Integer page,
            @RequestParam Integer limit
    ) {
        return articleService.listArticle(sort, start, end, ward, district, city,
                roommate, status, vip, search, minAcreage, maxAcreage,minPrice, maxPrice, page, limit);
    }

    //    contact với khách hàng (gửi mail cho khách hàng về bài viết này)	/admin/article/contact/{id}
    @PostMapping("/article/contact/{id}")
    public Message contactToCustomer(@PathVariable Integer id,
                                     @Valid @RequestBody ContactCustomerDTO contactCustomerDTO,
                                     HttpServletRequest request) throws CustomException {
        return articleService.contactToCustomer(id, contactCustomerDTO, request);
    }

    //    duyệt bài đăng (hiện) (gửi mail)	/admin/article/active/{id}
    @GetMapping("/article/active/{id}")
    public Message activeArticle(@PathVariable Integer id, HttpServletRequest request)
            throws CustomException {
        return articleService.activeArticle(id, request);
    }

    //    ẩn bài đăng (gửi mail)	/admin/article/block/{id}
    @PostMapping("/article/hidden/{id}")
    public Message hiddenArticle(@PathVariable Integer id,
                                 @RequestBody String mess,
                                 HttpServletRequest request)
            throws CustomException {
        return articleService.hiddenArticle(id, mess, request);
    }

    //    yêu cầu sửa lại bài đăng (gửi mail)	/admin/article/block/{id}
    @PostMapping("/article/suggest-fix/{id}")
    public Message suggestCorrectingArticle(@PathVariable Integer id,
                                 @RequestBody String mess,
                                 HttpServletRequest request)
            throws CustomException {
        return articleService.suggestCorrectingArticle(id, mess, request);
    }

    @GetMapping("/article/{id}")
    public ArticleOutputDTO detailArticle(@PathVariable Integer id) throws CustomException {
        return articleService.detailArticle(id);
    }

    @PostMapping("/article")
    public ArticleOutputDTO insertArticle(@Valid @RequestBody ArticleInsertDTO articleInsertDTO,
                                          HttpServletRequest request) throws CustomException {
        String email= helper.getEmailFromRequest(request);
        return articleService.insertArticle(email, articleInsertDTO);
    }

    @PutMapping("/article/{id}")
    public ArticleOutputDTO updateArticle(@Valid @RequestBody ArticleUpdateDTO articleUpdateDTO,
                                          @PathVariable Integer id,
                                          HttpServletRequest request) throws CustomException {
        String email = (String) request.getAttribute("email");
        return articleService.updateArticle(email, articleUpdateDTO, id);
    }

    //    gia hạn bài đăng	/customer/article/extension/{id}?days={int}
    @GetMapping("/article/extension/{id}")
    public Message extensionExp(@PathVariable Integer id,
                                @RequestParam Integer number,
                                @RequestParam String type,
                                HttpServletRequest request) throws CustomException {
        String email = (String) request.getAttribute("email");
        return articleService.extensionExp(email, id, number, type);
    }

    //    đăng lại bài đăng đã ẩn	/customer/article/post/{id}?days={int}
    @GetMapping("/article/post/{id}")
    public Message postOldArticle(@PathVariable Integer id,
                                  @RequestParam Integer number,
                                  @RequestParam String type,
                                  @RequestParam Boolean vip,
                                  HttpServletRequest request) throws CustomException {
        String email = (String) request.getAttribute("email");
        return articleService.postOldArticle(email, id, number, type, vip);
    }

    @GetMapping("/article/buff/{id}")
    public Message buffPoint(@PathVariable Integer id,
                                  @RequestParam Integer point,
                                  HttpServletRequest request) throws CustomException {
        String email = (String) request.getAttribute("email");
        return articleService.buffPoint(email,id,point);
    }

    @GetMapping("/article/point/{id}")
    public Map<String, Object> showPoint(@PathVariable Integer id) throws CustomException {
        return customerArticleService.showPoint(id);
    }
}
