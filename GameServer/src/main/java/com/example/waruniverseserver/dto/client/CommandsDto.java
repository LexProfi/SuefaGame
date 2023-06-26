package com.example.waruniverseserver.dto.client;

import com.example.waruniverseserver.dto.Choice;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommandsDto {
    Command command;
    Choice choice;
}
