package duantn.backend.controller.superAdmin;

import duantn.backend.authentication.CustomException;
import duantn.backend.service.StatisticalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created with YourComputer.
 * User: DUC_PRO
 * Date: 27/04/2021
 * Time: 9:32 CH
 */

@RestController
@RequestMapping("/super-admin")
public class Statistical {
    final
    StatisticalService statisticalService;

    public Statistical(StatisticalService statisticalService) {
        this.statisticalService = statisticalService;
    }

    //    thống kê bài số đăng theo ngày trong tháng/tháng trong năm/ theo năm
    @GetMapping("/statistic/article")
    public Map<String, Object> StatisticArticle(@RequestParam(required = false) Integer month,
                                                @RequestParam(required = false) Integer year,
                                                @RequestParam Integer page,
                                                @RequestParam Integer limit) throws CustomException {
        if(year!=null && (year>2199 || year<1000)) throw new CustomException("Năm quá lớn hoặc quá nhỏ");
        if(month!=null && (month<0 || month>12)) throw new CustomException("Tháng không hợp lệ");
        return statisticalService.statisticArticle(month, year, page, limit);
    }

    //    thống kê doanh thu theo ngày trong tháng/tháng trong năm/ theo năm
    @GetMapping("/statistic/revenue")
    public Map<String, Object> StatisticRevenue(@RequestParam(required = false) Integer month,
                                                @RequestParam(required = false) Integer year,
                                                @RequestParam Integer page,
                                                @RequestParam Integer limit) throws CustomException{
        if(year!=null && (year>2199 || year<1000)) throw new CustomException("Năm quá lớn hoặc quá nhỏ");
        if(month!=null && (month<0 || month>12)) throw new CustomException("Tháng không hợp lệ");
        return statisticalService.statisticRevenue(month, year, page, limit);
    }

    //    thống kê số tài khoản mới theo ngày trong tháng/tháng trong năm/ theo năm
    @GetMapping("/statistic/customer")
    public Map<String, Object> StatisticCustomer(@RequestParam(required = false) Integer month,
                                                 @RequestParam(required = false) Integer year,
                                                 @RequestParam Integer page,
                                                 @RequestParam Integer limit) throws CustomException {
        if(year!=null && (year>2199 || year<1000)) throw new CustomException("Năm quá lớn hoặc quá nhỏ");
        if(month!=null && (month<0 || month>12)) throw new CustomException("Tháng không hợp lệ");
        return statisticalService.statisticCustomer(month, year, page, limit);
    }

    //    thống kê số lượt truy cập theo ngày tháng năm
    @GetMapping("/statistic/count-request")
    public Map<String, Object> StatisticRequest(@RequestParam(required = false) Integer month,
                                                @RequestParam(required = false) Integer year,
                                                @RequestParam Integer page,
                                                @RequestParam Integer limit) throws CustomException {
        if(year!=null && (year>2199 || year<1000)) throw new CustomException("Năm quá lớn hoặc quá nhỏ");
        if(month!=null && (month<0 || month>12)) throw new CustomException("Tháng không hợp lệ");
        return statisticalService.statisticRequest(month, year, page, limit);
    }

    //    thống kê hoạt động của nhân viên theo ngày/tháng
    @GetMapping("/statistic/staff-action")
    public Map<String, Object> StatisticStaffAction(@RequestParam(required = false) String search,
                                                    @RequestParam(required = false) Long date,
                                                    @RequestParam(required = false) Integer month,
                                                    @RequestParam(required = false) Integer year,
                                                    @RequestParam Integer page,
                                                    @RequestParam Integer limit) throws CustomException{
        if(year!=null && (year>2199 || year<1000)) throw new CustomException("Năm quá lớn hoặc quá nhỏ");
        if(month!=null && (month<0 || month>12)) throw new CustomException("Tháng không hợp lệ");
        return statisticalService.statisticStaffAction(search, date, month, year, page, limit);
    }
}
