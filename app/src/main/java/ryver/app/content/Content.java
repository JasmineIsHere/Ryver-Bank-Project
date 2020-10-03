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
    
}