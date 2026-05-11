package uz.umft.qabul.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorTypeDto {
    private Integer id;
    private String type;
    private Integer active;
}
