package ryver.app.content;

import java.util.List;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "api")
public class ContentController {
    // Repositories
    private ContentRepository contents;

    public ContentController(ContentRepository contents) {
        this.contents = contents;
    }

    /** 
     * Get a List of Contents, differs based on user authority
     * Valid customer - Return approved Content only, 200 OK
     * Valid manager/analyst - Return all Content, 200 OK
     * Invalid User - Return 401 Unauthorized
     * 
     * @return List<Content>
     */
    @GetMapping("/api/contents")
    public List<Content> getContent() {

        // Customers can only see approved content
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            return contents.findByApproved(true);
        }
        return contents.findAll();
    }
    
    /** 
     * Get a specific Content based on the specified contentId
     * 
     * @param contentId
     * @return Content
     * If specified Content not found, 400 bad request
     * Returns 200 OK
     */
    @GetMapping("/api/contents/{contentId}")
    public Content getSpecificContent(@PathVariable (value = "contentId") Long contentId){
        Content content = contents.findById(contentId)
            .orElseThrow(() -> new ContentNotFoundException(contentId));

        if (content.isApproved()){
            return content;
        } else{
            throw new ContentNotFoundException(contentId);
        }
    }
    
    /** 
     * Create a new Content
     * Approved is false by default
     * Returns 201 Created
     * 
     * @param content
     * @return Content
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/contents")
    public Content addContent(@Valid @RequestBody Content content) {
        content.setApproved(false);
        return contents.save(content);
    }

    /** 
     * Update a specific Content, based on the specified contentId
     * Based on JSON data
     * Returns 200 OK
     * 
     * @param contentId
     * @param updatedContentInfo
     * @return Content
     */
    @PutMapping("/api/contents/{contentId}")
    public Content updateContent(@PathVariable (value = "contentId") Long contentId, @Valid @RequestBody Content updatedContentInfo) {

        Content content = contents.findById(contentId)
            .orElseThrow(() -> new ContentNotFoundException(contentId));

        content.setTitle(updatedContentInfo.getTitle());   
        content.setSummary(updatedContentInfo.getSummary());
        content.setContent(updatedContentInfo.getContent());
        content.setLink(updatedContentInfo.getLink());

        // Managers can approve/unapprove content
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
            content.setApproved(updatedContentInfo.isApproved());
        }

        return contents.save(content);
    }
    
    /** 
     * Delete a specified Content, based on the specified contentId
     * Returns 200 OK
     * 
     * @param contentId
     */
    @DeleteMapping("/api/contents/{contentId}")
    public void deleteContent(@PathVariable (value = "contentId") Long contentId) {
        // To check if content exists
        Content content = contents.findById(contentId)
            .orElseThrow(() -> new ContentNotFoundException(contentId));

        contents.deleteById(contentId);
    }
}