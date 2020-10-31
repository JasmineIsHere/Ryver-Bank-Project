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
    private ContentRepository contents;

    public ContentController(ContentRepository contents) {
        this.contents = contents;
    }

    @GetMapping("/api/contents")
    public List<Content> getContent() {

        //Users can only see approved content
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            return contents.findByApproved(true);
        }
        return contents.findAll();
    }

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
    
    // Approved is false by default
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/contents")
    public Content addContent(@Valid @RequestBody Content content) {
        content.setApproved(false);
        return contents.save(content);
    }

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

    @DeleteMapping("/api/contents/{contentId}")
    public void deleteContent(@PathVariable (value = "contentId") Long contentId) {
        // To check if content exists
        Content content = contents.findById(contentId)
            .orElseThrow(() -> new ContentNotFoundException(contentId));

        contents.deleteById(contentId);
    }
}