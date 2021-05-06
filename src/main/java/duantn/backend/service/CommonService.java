package duantn.backend.service;

import duantn.backend.authentication.CustomException;
import duantn.backend.model.dto.output.CityOutputDTO;
import duantn.backend.model.dto.output.TransactionOutputDTO;

import java.util.List;

public interface CommonService {
    //Lịch sử nạp tiền
    List<TransactionOutputDTO> listAllTransaction(String email,
                                                  Integer page, Integer limit,
                                                  Boolean type) throws CustomException;

    //Trả về các tỉnh
    List<CityOutputDTO> listAllCity() throws CustomException;

    //trả về các huyện
    List<CityOutputDTO> findDistrictByCity(Integer cityId) throws CustomException;

    //trả về các xã
    List<CityOutputDTO> findWardByDistrict(Integer districtId) throws CustomException;
}
