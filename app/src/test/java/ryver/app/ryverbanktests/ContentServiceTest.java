package ryver.app.ryverbanktests;

import ryver.app.customer.*;
import ryver.app.content.*;
import ryver.app.customer.Customer.*;
import ryver.app.content.Content.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 
 * KEY: 
 * W --> Works 
 * X --> Doesnt work
 * 
 *      T E S T
 *  W   1.addContent_approveContent_returnSavedContent
 * 
 * Notes:
 * none
*/

@ExtendWith(MockitoExtension.class)
public class ContentServiceTest {
    @Mock
    private ContentRepository contents;

    @InjectMocks
    private ContentController contentController;

    @Test
    void addContent_approveContent_returnSavedContent(){
        //arrange
        Content content = new Content("title", "summary", "content", "link", false);
        when(contents.save(any(Content.class))).thenReturn(content);      
        
        //act
        Content savedContent = contentController.addContent(content);
        
        //assert
        assertNotNull(savedContent);
        verify(contents).save(content);
    }
}