package duantn.backend.service;

import duantn.backend.authentication.CustomException;

import java.util.Map;

/**
 * Created with YourComputer.
 * User: DUC_PRO
 * Date: 27/04/2021
 * Time: 9:39 CH
 */

public interface StatisticalService {
    Map<String, Object> statisticArticle(Integer month, Integer year, Integer page, Integer limit) throws CustomException;

    Map<String, Object> statisticRevenue(Integer month, Integer year, Integer page, Integer limit) throws CustomException;

    Map<String, Object> statisticCustomer(Integer month, Integer year, Integer page, Integer limit) throws CustomException;

    Map<String, Object> statisticRequest(Integer month, Integer year, Integer page, Integer limit) throws CustomException;

    Map<String, Object> statisticStaffAction(String search, Long date, Integer month, Integer year, Integer page, Integer limit) throws CustomException;
}
