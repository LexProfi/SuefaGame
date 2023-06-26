package waruniverseclient.dto.server;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import waruniverseclient.dto.Choice;
import waruniverseclient.dto.Result;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoundResultDto {
    Result result;
    Choice previousPlayerChoice;
    Choice previousServerChoice;
}
