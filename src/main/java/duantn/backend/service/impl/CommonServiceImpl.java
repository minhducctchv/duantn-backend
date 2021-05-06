package duantn.backend.service.impl;

import duantn.backend.authentication.CustomException;
import duantn.backend.dao.CityRepository;
import duantn.backend.dao.DistrictRepository;
import duantn.backend.dao.TransactionRepository;
import duantn.backend.dao.WardRepository;
import duantn.backend.model.dto.output.CityOutputDTO;
import duantn.backend.model.dto.output.TransactionOutputDTO;
import duantn.backend.model.entity.City;
import duantn.backend.model.entity.District;
import duantn.backend.model.entity.Transaction;
import duantn.backend.model.entity.Ward;
import duantn.backend.service.CommonService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommonServiceImpl implements CommonService {
    final
    TransactionRepository transactionRepository;

    final
    CityRepository cityRepository;

    final
    DistrictRepository districtRepository;

    final
    WardRepository wardRepository;

    public CommonServiceImpl(TransactionRepository transactionRepository, CityRepository cityRepository, DistrictRepository districtRepository, WardRepository wardRepository) {
        this.transactionRepository = transactionRepository;
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.wardRepository = wardRepository;
    }

    @Override
    public List<TransactionOutputDTO> listAllTransaction(String email, Integer page, Integer limit, Boolean type) throws CustomException {
        Page<Transaction> transactionPage;
        if (type == null) {
            transactionPage =
                    transactionRepository.findByCustomer_Email(email,
                            PageRequest.of(page, limit));
        } else {
            transactionPage =
                    transactionRepository.findByCustomer_EmailAndType(email, type,
                            PageRequest.of(page, limit));
        }
        List<Transaction> transactionList = transactionPage.toList();

        //convert to DTO
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        List<TransactionOutputDTO> transactionOutputDTOList = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            TransactionOutputDTO transactionOutputDTO =
                    modelMapper.map(transaction, TransactionOutputDTO.class);
            transactionOutputDTO.setDate(transaction.getTimeCreated().getTime());
            transactionOutputDTO.setMethod(
                    transaction.isType() ? "Nạp tiền" : "Thanh toán"
            );
            transactionOutputDTO.setElements(transactionPage.getTotalElements());
            transactionOutputDTO.setPages(transactionPage.getTotalPages());
            transactionOutputDTOList.add(transactionOutputDTO);
        }

        return transactionOutputDTOList;
    }

    @Override
    public List<CityOutputDTO> listAllCity() throws CustomException {
        List<City> cities = cityRepository.findAll();
        List<CityOutputDTO> cityOutputDTOList = new ArrayList<>();
        for (City city : cities) {
            CityOutputDTO cityOutputDTO = new CityOutputDTO();
            cityOutputDTO.setId(city.getCityId());
            cityOutputDTO.setName(city.getCityName());
            cityOutputDTOList.add(cityOutputDTO);
        }
        return cityOutputDTOList;
    }

    @Override
    public List<CityOutputDTO> findDistrictByCity(Integer cityId) throws CustomException {
        List<District> cities = districtRepository.findByCity_CityId(cityId);
        List<CityOutputDTO> cityOutputDTOList = new ArrayList<>();
        for (District city : cities) {
            CityOutputDTO cityOutputDTO = new CityOutputDTO();
            cityOutputDTO.setId(city.getDistrictId());
            cityOutputDTO.setName(city.getDistrictName());
            cityOutputDTOList.add(cityOutputDTO);
        }
        return cityOutputDTOList;
    }

    @Override
    public List<CityOutputDTO> findWardByDistrict(Integer districtId) throws CustomException {
        List<Ward> cities = wardRepository.findByDistrict_DistrictId(districtId);
        List<CityOutputDTO> cityOutputDTOList = new ArrayList<>();
        for (Ward city : cities) {
            CityOutputDTO cityOutputDTO = new CityOutputDTO();
            cityOutputDTO.setId(city.getWardId());
            cityOutputDTO.setName(city.getWardName());
            cityOutputDTOList.add(cityOutputDTO);
        }
        return cityOutputDTOList;
    }
}
