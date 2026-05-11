package uz.umft.qabul.dto.tuition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.umft.qabul.enums.Degree;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TuitionDto {
    private UUID id;
    private Integer majorCode;
    private String schoolYearTitle;
    private String majorTitle;
    private String majorTypeName;
    private String majorLangName;
    private Degree degree;
    private Long amount;
}
