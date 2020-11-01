package ryver.app.ryverbanktests;

import ryver.app.content.*;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



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
 */

@ExtendWith(MockitoExtension.class)
public class ContentControllerTest {
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