package waruniverseclient.dto.server;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import waruniverseclient.dto.Choice;
import waruniverseclient.dto.Result;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameResultDto {
    Result result;
    Choice[] playerChoices;
    Choice[] serverChoices;
}
