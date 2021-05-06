package duantn.backend.service;

import duantn.backend.authentication.CustomException;
import duantn.backend.model.dto.input.NewspaperInsertDTO;
import duantn.backend.model.dto.input.NewspaperUpdateDTO;
import duantn.backend.model.dto.output.Message;
import duantn.backend.model.dto.output.NewspaperOutputDTO;

import java.util.List;

public interface NewspaperService {
    //list tin tức
    List<NewspaperOutputDTO> listNewspaper(String sort, Boolean hidden, String title,
                                           Integer page, Integer limit);

    //tin tức details
    NewspaperOutputDTO findOneNewspaper(Integer id) throws CustomException;

    //đăng tin tức
    NewspaperOutputDTO insertNewspaper(NewspaperInsertDTO newspaperInsertDTO) throws CustomException;

    //    sửa tin tức
    NewspaperOutputDTO updateNewspaper(NewspaperUpdateDTO newspaperUpdateDTO,
                                       Integer id) throws CustomException;

    //    ẩn 1 tin tức
    Message hiddenNewspaper(Integer id) throws CustomException;

    //    hiện 1 tin tức
    Message activeNewspaper(Integer id) throws CustomException;

    //    xóa tin tức
    Message deleteNewspaper(Integer id) throws CustomException;
}
