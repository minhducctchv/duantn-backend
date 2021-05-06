package duantn.backend.controller.common;

import duantn.backend.authentication.CustomException;
import duantn.backend.model.dto.output.CityOutputDTO;
import duantn.backend.service.CommonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CommonController {
    final
    CommonService commonService;

    public CommonController(CommonService commonService) {
        this.commonService = commonService;
    }

    //Trả về các tỉnh
    @GetMapping("/city")
    List<CityOutputDTO> listAllCity() throws CustomException {
        return commonService.listAllCity();
    }

    //trả về các huyện
    @GetMapping("/district")
    List<CityOutputDTO> findDistrictByCity(@RequestParam("city-id") Integer cityId) throws CustomException {
        return commonService.findDistrictByCity(cityId);
    }

    //trả về các xã
    @GetMapping("ward")
    List<CityOutputDTO> findWardByDistrict(@RequestParam("district-id") Integer districtId) throws CustomException {
        return commonService.findWardByDistrict(districtId);
    }
}
