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
            throw new CustomException("Tin t???c v???i id " + id + " kh??ng t???n t???i");
        }
    }

    @Override
    public NewspaperOutputDTO insertNewspaper(NewspaperInsertDTO newspaperInsertDTO) throws CustomException {
        Optional<Staff> staffOptional = staffRepository.findById(newspaperInsertDTO.getStaffId());
        if (!staffOptional.isPresent())
            throw new CustomException("Nh??n vi??n v???i id " + newspaperInsertDTO.getStaffId() + " kh??ng t???n t???i");

        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Newspaper newspaper = modelMapper.map(newspaperInsertDTO, Newspaper.class);
            newspaper.setStaff(staffOptional.get());
            return convertToOutputDTO(newspaperRepository.save(newspaper), null, null);
        } catch (Exception e) {
            throw new CustomException("Th??m m???i th???t b???i");
        }
    }

    @Override
    public NewspaperOutputDTO updateNewspaper(NewspaperUpdateDTO newspaperUpdateDTO,
                                              Integer id) throws CustomException {
        Optional<Staff> staffOptional = staffRepository.findById(newspaperUpdateDTO.getStaffId());
        if (!staffOptional.isPresent())
            throw new CustomException("Nh??n vi??n v???i id " + newspaperUpdateDTO.getStaffId() + " kh??ng t???n t???i");
        Optional<Newspaper> newspaperOptional = newspaperRepository.findById(id);
        if (!newspaperOptional.isPresent())
            throw new CustomException("B???n tin v???i id " + id + " kh??ng t???n t???i");
        try {
            Newspaper newspaper = newspaperOptional.get();
            newspaper.setTitle(newspaperUpdateDTO.getTitle());
            newspaper.setContent(newspaperUpdateDTO.getContent());
            newspaper.setImage(newspaperUpdateDTO.getImage());
            newspaper.setStaff(staffOptional.get());
            newspaper.setTimeCreated(new Date());
            return convertToOutputDTO(newspaperRepository.save(newspaper), null, null);
        } catch (Exception e) {
            throw new CustomException("C???p nh???t th???t b???i");
        }
    }

    @Override
    public Message hiddenNewspaper(Integer id) throws CustomException {
        Optional<Newspaper> newspaperOptional = newspaperRepository.findById(id);
        if (newspaperOptional.isPresent()) {
            Newspaper newspaper = newspaperOptional.get();
            newspaper.setDeleted(true);
            newspaperRepository.save(newspaper);
            return new Message("???n b??i vi???t th??nh c??ng");
        } else {
            throw new CustomException("Tin t???c v???i id " + id + " kh??ng t???n t???i");
        }
    }

    @Override
    public Message activeNewspaper(Integer id) throws CustomException {
        Optional<Newspaper> newspaperOptional = newspaperRepository.findById(id);
        if (newspaperOptional.isPresent()) {
            Newspaper newspaper = newspaperOptional.get();
            newspaper.setDeleted(false);
            newspaperRepository.save(newspaper);
            return new Message("Hi???n b??i vi???t th??nh c??ng");
        } else {
            throw new CustomException("Tin t???c v???i id " + id + " kh??ng t???n t???i");
        }
    }

    @Override
    public Message deleteNewspaper(Integer id) throws CustomException {
        try {
            newspaperRepository.deleteById(id);
            return new Message("Xo?? b??i vi???t th??nh c??ng");
        } catch (Exception e) {
            throw new CustomException("Tin t???c v???i id " + id + " kh??ng t???n t???i");
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
