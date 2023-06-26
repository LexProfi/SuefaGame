package waruniverseclient.dto.client;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import waruniverseclient.dto.Choice;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommandsDto {
    Command command;
    Choice choice;

    public CommandsDto(Command command) {
        this.command = command;
    }

    public CommandsDto(Command command, Choice choice) {
        this.command = command;
        this.choice = choice;
    }
}
