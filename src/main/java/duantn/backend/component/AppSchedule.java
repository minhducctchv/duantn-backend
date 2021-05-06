package duantn.backend.component;

import duantn.backend.dao.ArticleRepository;
import duantn.backend.dao.CountRequestRepository;
import duantn.backend.helper.VariableCommon;
import duantn.backend.model.entity.Article;
import duantn.backend.model.entity.CountRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class AppSchedule {
    final
    ArticleRepository articleRepository;
    final
    MailSender mailSender;
    final
    CountRequestRepository countRequestRepository;

    public AppSchedule(ArticleRepository articleRepository, MailSender mailSender, CountRequestRepository countRequestRepository) {
        this.articleRepository = articleRepository;
        this.mailSender = mailSender;
        this.countRequestRepository = countRequestRepository;
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 2 * 60 * 1000)
    public void AutoArticle() {
        List<Article> articleList = articleRepository.findByStatusAndExpTimeBeforeAndDeletedFalse(VariableCommon.DANG_DANG, new Date());
        if (articleList.size() > 0) {
            for (Article article : articleList) {
                //ẩn bài đăng hết hạn
                article.setStatus(VariableCommon.HET_HAN);
                articleRepository.save(article);
            }
        }
    }

    @Scheduled(fixedDelay = 1 * 3600 * 1000, initialDelay = 3 * 60 * 1000)
    public void AutoSendMail() {
        List<Article> articleList1 = articleRepository.findByStatusAndExpTimeBetweenAndDeletedFalse(VariableCommon.DANG_DANG,
                new Date(new Date().getTime() - 25 * 3600 * 1000), new Date());
        if (articleList1.size() > 0) {
            for (Article article : articleList1) {
                //gửi mail bài đăng sắp hết hạn
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                if (article.getCustomer() != null) {
                    mailSender.send(
                            article.getCustomer().getEmail(),
                            "Bài đăng số: " + article.getArticleId() + " sắp hết hạn",
                            "<p><strong>Chúng tôi xin trân trọng thông báo:</strong></p>\n" +
                                    "<p>Bài đăng số: " + article.getArticleId() + "</p>\n" +
                                    "<p>Tiêu đề: " + article.getTitle() + "</p>\n" +
                                    "<p>Link: xxxx</p>\n" +
                                    "<p>Sẽ hết hạn vào ngày: <span style=\"color: #0000ff;\">" + sdf.format(article.getExpTime()) + "</span></p>\n" +
                                    "<p>Nếu bạn muốn bài đăng tiếp tục được đăng vui lòng gia hạn bài đăng trước thời hạn trên.</p>",
                            "Xin cảm ơn quý khách đã sử dụng dịch vụ của chúng tôi."
                    );
                }
            }
        }
    }

    @Scheduled(fixedDelay = 1 * 3600 * 1000)
    public void calculationTimeGroup() {
        List<Article> articleList = articleRepository.findByStatusAndDeletedFalse(VariableCommon.DANG_DANG);
        for (Article article : articleList) {
            int timeGroup =
                    (int) (((double) (new Date().getTime() - article.getUpdateTime().getTime())) / ((double) (7 * 24 * 3600 * 1000)));
            article.setTimeGroup(timeGroup);
            articleRepository.save(article);
        }
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void updateVariableTime() {
        CountRequest countRequest = countRequestRepository.findFirstBy(Sort.by("date").descending());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String now = sdf.format(new Date());
        String date = "";
        if (countRequest != null) {
            date = sdf.format(countRequest.getDate());
        }
        if (countRequest == null || !now.equals(date)) {
            CountRequest countRequest1 = new CountRequest();
            countRequestRepository.save(countRequest1);
        }
    }
}
