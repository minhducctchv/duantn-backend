package duantn.backend.service.impl;

import duantn.backend.authentication.CustomException;
import duantn.backend.dao.ArticleRepository;
import duantn.backend.dao.CustomerRepository;
import duantn.backend.dao.FavoriteArticleRepository;
import duantn.backend.helper.VariableCommon;
import duantn.backend.model.dto.output.Message;
import duantn.backend.model.entity.Article;
import duantn.backend.model.entity.Customer;
import duantn.backend.model.entity.FavoriteArticle;
import duantn.backend.service.ArticleFavoriteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleFavoriteServiceImpl implements ArticleFavoriteService {
    final
    FavoriteArticleRepository favoriteArticleRepository;
    final
    CustomerRepository customerRepository;
    final
    ArticleRepository articleRepository;

    public ArticleFavoriteServiceImpl(FavoriteArticleRepository favoriteArticleRepository, CustomerRepository customerRepository, ArticleRepository articleRepository) {
        this.favoriteArticleRepository = favoriteArticleRepository;
        this.customerRepository = customerRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    public List<Map<String, String>> listArticle(String email,
                                                 Integer page, Integer limit) throws CustomException {
        Page<FavoriteArticle> favoriteArticlePage =
                favoriteArticleRepository.findByCustomer_Email(email,
                        PageRequest.of(page, limit));
        List<FavoriteArticle> favoriteArticleList = favoriteArticlePage.toList();
        List<Map<String, String>> mapList = new ArrayList<>();
        for (FavoriteArticle favoriteArticle : favoriteArticleList) {
            Map<String, String> map = new HashMap<>();
            if (favoriteArticle.getArticle() != null && !favoriteArticle.getArticle().getDeleted()) {
                if (favoriteArticle.getArticle().getStatus().equals(VariableCommon.DANG_DANG) ||
                        favoriteArticle.getArticle().getStatus().equals(VariableCommon.HET_HAN)) {
                    map.put("id", favoriteArticle.getId().toString());
                    map.put("articleId", favoriteArticle.getArticle().getArticleId().toString());
                    map.put("title", favoriteArticle.getArticle().getTitle());
                    map.put("image", favoriteArticle.getArticle().getImage());
                    map.put("timeUpdate", String.valueOf(favoriteArticle.getArticle().getUpdateTime().getTime()));
                    map.put("expDate", String.valueOf(favoriteArticle.getArticle().getExpTime().getTime()));
                    map.put("price", String.valueOf(favoriteArticle.getArticle().getRoomPrice()));
                    map.put("ward", favoriteArticle.getArticle().getWard().getWardName());
                    map.put("status", favoriteArticle.getArticle().getStatus());
                } else {
                    map.put("id", favoriteArticle.getId().toString());
                    map.put("title", "Bài đăng đã bị ẩn");
                    map.put("articleId", "Bài đăng đã bị ẩn");
                    map.put("image", "Bài đăng đã bị ẩn");
                    map.put("timeUpdate", "Bài đăng đã bị ẩn");
                    map.put("expDate", "Bài đăng đã bị ẩn");
                    map.put("price", "Bài đăng đã bị ẩn");
                    map.put("ward", "Bài đăng đã bị ẩn");
                    map.put("status", favoriteArticle.getArticle().getStatus());
                }
            } else {
                map.put("id", favoriteArticle.getId().toString());
                map.put("title", "Bài đăng đã bị xóa");
                map.put("articleId", "Bài đăng đã bị xóa");
                map.put("image", "Bài đăng đã bị xóa");
                map.put("timeUpdate", "Bài đăng đã bị xóa");
                map.put("expDate", "Bài đăng đã bị xóa");
                map.put("price", "Bài đăng đã bị xóa");
                map.put("ward", "Bài đăng đã bị xóa");
                map.put("status", "Đã xóa");
            }

            map.put("pages", "" + favoriteArticlePage.getTotalPages());
            map.put("elements", "" + favoriteArticlePage.getTotalElements());

            mapList.add(map);
        }

        return mapList;
    }

    @Override
    public Message addRemoveArticle(String email, Integer id) throws CustomException {
        FavoriteArticle favoriteArticle = favoriteArticleRepository.
                findByCustomer_EmailAndArticle_ArticleId(email, id);
        if (favoriteArticle != null) {
            Article article=favoriteArticle.getArticle();
            article.setPoint(article.getPoint()-2);
            articleRepository.save(article);

            favoriteArticleRepository.delete(favoriteArticle);
            return new Message("Bỏ yêu thích thành công");
        } else {
            Customer customer = customerRepository.findByEmail(email);
            if (customer == null) throw new CustomException("Email không hợp lệ");
            Article article = articleRepository.findByArticleIdAndDeletedFalse(id);
            if (article == null) throw new CustomException("Id bài đăng không hợp lệ");

            article.setPoint(article.getPoint() + 2);
            Article newArticle = articleRepository.save(article);

            FavoriteArticle favoriteArticle1 = new FavoriteArticle();
            favoriteArticle1.setCustomer(customer);
            favoriteArticle1.setArticle(newArticle);
            favoriteArticleRepository.save(favoriteArticle1);
            return new Message("Đã thêm vào bài đăng yêu thích");
        }
    }
}
