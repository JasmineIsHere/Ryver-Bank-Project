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
 *  X   2.updateContent_Found_approveContentAndReturnSavedContent
 *  X   3.updateContent_NotFound_returnSavedContent
 * 
 * Notes:
 * none
*/

@ExtendWith(MockitoExtension.class)
public class ContentServiceTest {
    @Mock
    private ContentRepository contents;
    // List<Content> findByApproved(boolean approved);

    @InjectMocks
    private ContentController contentController;
    // public Content addContent(@Valid @RequestBody Content content) {
    // public Content updateContent(@PathVariable (value = "contentId") Long contentId, @Valid @RequestBody Content updatedContentInfo) {

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

    // @Test 
    // void updateContent_Found_approveContentAndReturnSavedContent(){
    //     //arrange
    //     Content content = new Content("title", "summary", "content","link", true);
    //     content.setId(1L);
    //     when(content.findById(content.getId())).thenReturn(Optional.of(content));      
    //     when(content.save(any(Content.class))).thenReturn(currentContent);

    //     Content updatedContent = new Content("title2", "summary2", "content2","link2", true);
    //     updatedContent.setId(1L);
    //     when(content.save(any(Content.class))).thenReturn(updatedContent);      

    //     //act
    //     Content savedContent = contentController.updateContent(currentContent.getId(), updatedContent);

    //     //assert
    //     assertEquals(updatedContent.getTitle(), savedContent.getTitle());
    //     assertEquals(updatedContent.getSummary(), savedContent.getSummary());
    //     assertEquals(updatedContent.getContent(), savedContent.getContent());
    //     assertEquals(updatedContent.getLink(), savedContent.getLink());
    //     assertEquals(updatedContent.isApproved(), savedContent.isApproved());

    //     verify(contents).save(content);
    //     verify(contents).save(updatedContent);
    // }  

    // @Test
    // void updateContent_ROLEAnalyst_returnSavedContent(){
    //     //arrange
    //     Customer manager = new Customer(
    //         "Janice", "password", "ANALYST", "Jolene Loh", "T0046822Z", "12345678", "address", true);
    //     when(customers.save(any(Customer.class))).thenReturn(manager);
    //     Content currentContent = new Content();
    //     when(content.save(any(Content.class))).thenReturn(currentContent);      
    //     Content updatedContent = new Content();
    //     when(content.save(any(Content.class))).thenReturn(updatedContent);      
    //     //act
    //     Content savedContent = contentController.updateContent(currentContent.getId(), updatedContent);
    //     //assert
    //     assertNotNull(savedContent);
    //     verify(customers).save(manager);
    //     verify(contents).save(currentContent);
    //     verify(contents).save(updatedContent);
    // }
}