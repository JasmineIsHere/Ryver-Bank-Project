package ryver.app.content;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    /**
     * Get a List of Contents, filtered by the approval status
     * 
     * @param approved
     * @return
     */
    List<Content> findByApproved(boolean approved);
}