package duantn.backend;

import duantn.backend.authentication.CustomException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletContext;

@SpringBootTest
class DuantnBackendApplicationTests {
    @Autowired
    ServletContext servletContext;
    @Autowired
    ApplicationContext applicationContext;

    @Test
    void contextLoads() throws CustomException{

    }

}
