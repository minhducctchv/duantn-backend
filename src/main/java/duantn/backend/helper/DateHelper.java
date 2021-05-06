package duantn.backend.helper;

import duantn.backend.authentication.CustomException;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class DateHelper {
    public Date changeTime(Date date, String time) throws CustomException{
        try{
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy");
            String stringDate=simpleDateFormat.format(date);
            simpleDateFormat.applyPattern("dd/MM/yyyy HH:mm:ss");
            return simpleDateFormat.parse(stringDate+" "+time);
        }catch (Exception e){
            throw new CustomException("Lỗi chuyển đổi thời gian");
        }
    }
}
