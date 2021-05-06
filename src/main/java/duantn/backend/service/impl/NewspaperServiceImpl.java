package duantn.backend.service.impl;

import duantn.backend.authentication.CustomException;
import duantn.backend.dao.NewspaperRepository;
import duantn.backend.dao.StaffRepository;
import duantn.backend.model.dto.input.NewspaperInsertDTO;
import duantn.backend.model.dto.input.NewspaperUpdateDTO;
import duantn.backend.model.dto.output.Message;
import duantn.backend.model.dto.output.NewspaperOutputDTO;
import duantn.backend.model.entity.Newspaper;
import duantn.backend.model.entity.Staff;
import duantn.backend.service.NewspaperService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class NewspaperServiceImpl implements NewspaperService {
    final
    NewspaperRepository newspaperRepository;

    final
    StaffRepository staffRepository;

    public NewspaperServiceImpl(NewspaperRepository newspaperRepository, StaffRepository staffRepository) {
        this.newspaperRepository = newspaperRepository;
        this.staffRepository = staffRepository;
    }

    @Override
    public List<NewspaperOutputDTO> listNewspaper(String sort, Boolean hidden, String title,
                                                  Integer page, Integer limit) {
        if (title == null) title = "";
        Page<Newspaper> newspaperPage;
        if (hidden != null) {
            if (sort != null && sort.equals("asc")) {
                newspaperPage = newspaperRepository.
                        findByTitleLikeAndDeleted("%" + title + "%", hidden,
                                PageRequest.of(page, limit, Sort.by("timeCreated").ascending()));
            } else {
                newspaperPage = newspaperRepository.
                        findByTitleLikeAndDeleted("%" + title + "%", hidden,
                                PageRequest.of(page, limit, Sort.by("timeCreated").descending()));
            }
        } else {
            if (sort != null && sort.equals("asc")) {
                newspaperPage = newspaperRepository.
                        findByTitleLike("%" + title + "%",
                                PageRequest.of(page, limit, Sort.by("timeCreated").ascending()));
            } else {
                newspaperPage = newspaperRepository.
                        findByTitleLike("%" + title + "%",
                                PageRequest.of(page, limit, Sort.by("timeCreated").descending()));
            }
        }

        List<Newspaper> newspaperList = newspaperPage.toList();

        List<NewspaperOutputDTO> newspaperOutputDTOList = new ArrayList<>();
        for (Newspaper newspaper : newspaperList) {
            newspaperOutputDTOList.add(convertToOutputDTO(newspaper, newspaperPage.getTotalElements(),
                    newspaperPage.getTotalPages()));
        }

        return newspaperOutputDTOList;
    }

    @Override
    public NewspaperOutputDTO findOneNewspaper(Integer id) throws CustomException {
        Optional<Newspaper> newspaperOptional = newspaperRepository.findById(id);
        if (newspaperOptional.isPresent()) {
            return convertToOutputDTO(newspaperOptional.get(), null, null);
        } else {
            throw new CustomException("Tin tức với id " + id + " không tồn tại");
        }
    }

    @Override
    public NewspaperOutputDTO insertNewspaper(NewspaperInsertDTO newspaperInsertDTO) throws CustomException {
        Optional<Staff> staffOptional = staffRepository.findById(newspaperInsertDTO.getStaffId());
        if (!staffOptional.isPresent())
            throw new CustomException("Nhân viên với id " + newspaperInsertDTO.getStaffId() + " không tồn tại");

        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Newspaper newspaper = modelMapper.map(newspaperInsertDTO, Newspaper.class);
            newspaper.setStaff(staffOptional.get());
            return convertToOutputDTO(newspaperRepository.save(newspaper), null, null);
        } catch (Exception e) {
            throw new CustomException("Thêm mới thất bại");
        }
    }

    @Override
    public NewspaperOutputDTO updateNewspaper(NewspaperUpdateDTO newspaperUpdateDTO,
                                              Integer id) throws CustomException {
        Optional<Staff> staffOptional = staffRepository.findById(newspaperUpdateDTO.getStaffId());
        if (!staffOptional.isPresent())
            throw new CustomException("Nhân viên với id " + newspaperUpdateDTO.getStaffId() + " không tồn tại");
        Optional<Newspaper> newspaperOptional = newspaperRepository.findById(id);
        if (!newspaperOptional.isPresent())
            throw new CustomException("Bản tin với id " + id + " không tồn tại");
        try {
            Newspaper newspaper = newspaperOptional.get();
            newspaper.setTitle(newspaperUpdateDTO.getTitle());
            newspaper.setContent(newspaperUpdateDTO.getContent());
            newspaper.setImage(newspaperUpdateDTO.getImage());
            newspaper.setStaff(staffOptional.get());
            newspaper.setTimeCreated(new Date());
            return convertToOutputDTO(newspaperRepository.save(newspaper), null, null);
        } catch (Exception e) {
            throw new CustomException("Cập nhật thất bại");
        }
    }

    @Override
    public Message hiddenNewspaper(Integer id) throws CustomException {
        Optional<Newspaper> newspaperOptional = newspaperRepository.findById(id);
        if (newspaperOptional.isPresent()) {
            Newspaper newspaper = newspaperOptional.get();
            newspaper.setDeleted(true);
            newspaperRepository.save(newspaper);
            return new Message("Ẩn bài viết thành công");
        } else {
            throw new CustomException("Tin tức với id " + id + " không tồn tại");
        }
    }

    @Override
    public Message activeNewspaper(Integer id) throws CustomException {
        Optional<Newspaper> newspaperOptional = newspaperRepository.findById(id);
        if (newspaperOptional.isPresent()) {
            Newspaper newspaper = newspaperOptional.get();
            newspaper.setDeleted(false);
            newspaperRepository.save(newspaper);
            return new Message("Hiện bài viết thành công");
        } else {
            throw new CustomException("Tin tức với id " + id + " không tồn tại");
        }
    }

    @Override
    public Message deleteNewspaper(Integer id) throws CustomException {
        try {
            newspaperRepository.deleteById(id);
            return new Message("Xoá bài viết thành công");
        } catch (Exception e) {
            throw new CustomException("Tin tức với id " + id + " không tồn tại");
        }
    }

    public NewspaperOutputDTO convertToOutputDTO(Newspaper newspaper, Long elements, Integer pages) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        NewspaperOutputDTO newspaperOutputDTO = modelMapper.map(newspaper, NewspaperOutputDTO.class);
        newspaperOutputDTO.setAuthor(newspaper.getStaff().getName() + " (" + newspaper.getStaff().getEmail() + ")");
        newspaperOutputDTO.setUpdateTime(newspaper.getTimeCreated().getTime());
        newspaperOutputDTO.setElements(elements);
        newspaperOutputDTO.setPages(pages);
        return newspaperOutputDTO;
    }
}
