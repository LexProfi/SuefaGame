package com.example.waruniverseserver.dto.server;

import com.example.waruniverseserver.dto.Choice;
import com.example.waruniverseserver.dto.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoundResultDto {
    Result result;
    Choice previousPlayerChoice;
    Choice previousServerChoice;
}
