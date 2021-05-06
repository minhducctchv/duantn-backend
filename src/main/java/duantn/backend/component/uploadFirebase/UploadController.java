package duantn.backend.component.uploadFirebase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import duantn.backend.authentication.CustomException;
import duantn.backend.model.dto.output.Message;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
public class UploadController {
    private final FirebaseFileService firebaseFileService;

    public UploadController(FirebaseFileService firebaseFileService) {
        this.firebaseFileService = firebaseFileService;
    }

    @PostMapping("/firebase/upload")
    public List<String> create(@RequestParam(name = "files") List<MultipartFile> files) throws CustomException{
        if(files.size()>0){
            List<String> imageLinks = new ArrayList<>();
            int index=0;
            for (MultipartFile file:files){
                try {
                    String fileName = firebaseFileService.saveTest(file);
                    // do whatever you want with that
                    String url="https://firebasestorage.googleapis.com/v0/b/du-an-tot-nghiep-fpt.appspot.com/o/"+fileName+"?alt=media&token="+fileName;

                    imageLinks.add(url);
                    index++;
                } catch (CustomException e){
                    throw new CustomException(e.getMessage());
                }catch (Exception e) {
                    //  throw internal error;
                    e.printStackTrace();
                    throw new CustomException("Upload image thất bại");
                }
            }
            return imageLinks;
        }else {
            throw new CustomException("Không có ảnh nào để upload");
        }
    }

    @GetMapping("/split")
    public String split(@RequestParam String string){
        String[] list=string.split("___");
        String chuoiCon="";
        List<String> list1= Arrays.asList(list);
        for(String s: list){
            chuoiCon+=s+"\n";
            System.out.println("la: "+s);
        }
        return chuoiCon;
    }
}
