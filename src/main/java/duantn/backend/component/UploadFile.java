package duantn.backend.component;

import duantn.backend.authentication.CustomException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class UploadFile {

    private final Path root = Paths.get("src/main/resources/uploads");
    @EventListener
    public void init(ApplicationReadyEvent event) throws CustomException{
        try {
            if(!root.toFile().exists()) Files.createDirectory(root);
        } catch (IOException e) {
            throw new CustomException("Could not initialize folder for upload!");
        }
    }

    //upload
    public void save(MultipartFile file) throws CustomException{
        try {
            Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));
        } catch (Exception e) {
            throw new CustomException("Could not store the file. Error: " + e.getMessage());
        }
    }
    // all files
    public Stream<Path> loadAll() throws CustomException{
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new CustomException("Could not load the files!");
        }
    }
    // one file
    public File load(String filename) throws CustomException{
        try {
            Path path = root.resolve(filename);
            File file= path.toFile();
            if(file.exists()){
                return file;
            }else{
                throw new CustomException("Could not read the file!");
            }
        } catch (Exception e) {
            throw new CustomException("Error: " + e.getMessage());
        }
    }

    //delete all file
    public void deleteAll() throws CustomException{
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    // delete one file
    public void deleteOneFile(String filename) throws CustomException{
        Path path = root.resolve(filename);
        FileSystemUtils.deleteRecursively(path.toFile());
    }
}
