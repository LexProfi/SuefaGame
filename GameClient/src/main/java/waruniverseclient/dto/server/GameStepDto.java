package waruniverseclient.dto.server;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameStepDto {
    int stepNumber;
}
