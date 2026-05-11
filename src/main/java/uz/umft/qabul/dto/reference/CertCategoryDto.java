package uz.umft.qabul.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.umft.qabul.enums.CertType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertCategoryDto {
    private Integer id;
    private String title;
    private CertType type;
}
