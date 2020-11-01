package ryver.app.content;

import javax.persistence.*;

import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode

public class Content {
    // Fields
    /**
     * The auto-generated ID for each Content
     * Starts from 1
     */
    private  @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;

    /**
     * The title of the Content
     */
    private String title;

    /**
     * The summary of the Content
     */
    private String summary;

    /**
     * The text of the Content
     */
    private String content;

    /**
     * The link in the Content
     */
    private String link;

    /**
     * The approval status of the Content
     */
    private boolean approved;
    
    // Constructors
    /**
     * Create Content with the specified parameters
     * 
     * @param title
     * @param summary
     * @param content
     * @param link
     * @param approved
     */
    public Content(String title, String summary, String content, String link, boolean approved) {
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.link = link;
        this.approved = approved;
    }
}