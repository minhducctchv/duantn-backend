package duantn.backend.component.uploadFirebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import duantn.backend.authentication.CustomException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created with YourComputer.
 * Author: DUC_PRO
 * Date: 15/04/2021
 * Time: 10:01 SA
 */

@Service
public class FirebaseFileService {

    private Storage storage;

    @EventListener
    public void init(ApplicationReadyEvent event) {
        try {
            ClassPathResource serviceAccount = new ClassPathResource("firebase.json");
            storage = StorageOptions.newBuilder().
                    setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream())).
                    setProjectId("du-an-tot-nghiep-fpt").build().getService();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String saveTest(MultipartFile file) throws Exception {
        if(!(getExtension(file.getOriginalFilename()).equalsIgnoreCase("jpg")||
                getExtension(file.getOriginalFilename()).equalsIgnoreCase("png")||
                getExtension(file.getOriginalFilename()).equalsIgnoreCase("jpeg")||
                getExtension(file.getOriginalFilename()).equalsIgnoreCase("gif")))
            throw new CustomException("Định dạng ảnh không chính xác");
        String imageName = generateFileName(file.getOriginalFilename());
        Map<String, String> map = new HashMap<>();
        map.put("firebaseStorageDownloadTokens", imageName);
        //BUCKET=PROJECT_ID + "appspot.com"
        BlobId blobId = BlobId.of("du-an-tot-nghiep-fpt.appspot.com", imageName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setMetadata(map)
                .setContentType(file.getContentType())
                .build();
        storage.create(blobInfo, file.getBytes());
        return imageName;
    }

    private String generateFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "." + getExtension(originalFileName);
    }

    private String getExtension(String originalFileName) {
        return StringUtils.getFilenameExtension(originalFileName);
    }
}
