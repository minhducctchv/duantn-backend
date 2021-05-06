package duantn.backend.service;

import duantn.backend.authentication.CustomException;
import duantn.backend.model.dto.output.ArticleOutputDTO;

import java.util.List;

public interface CustomerNoLoginService {
    List<ArticleOutputDTO> listArticle(Boolean vip, String email,
                                       Long start, Long end,
                                       Integer ward,
                                       Integer district,
                                       Integer city,
                                       Boolean roommate,
                                       String search,
                                       Integer minAcreage, Integer maxAcreage,
                                       Integer minPrice, Integer maxPrice,
                                       Boolean sort,
                                       Integer page,
                                       Integer limit
    );

    ArticleOutputDTO findOneArticle(String email, Integer id) throws CustomException;
}
