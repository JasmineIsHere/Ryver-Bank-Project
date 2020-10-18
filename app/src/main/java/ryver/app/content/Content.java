package ryver.app.content;

import javax.persistence.*;
import javax.validation.constraints.*;

import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Content{
    private  @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;
    private String title;
    private String summary;
    private String content;
    private String link;
    private boolean approved;
    
    //JOLENE: for testing
    public Content(String title, String summary, String content, String link, boolean approved){
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.link = link;
        this.approved = approved;
    }

    public String getTitle(){
        return title;
    }
    public String getSummary(){
        return summary;
    }
    public String getContent(){
        return content;
    }
    public String getLink(){
        return link;
    }
}