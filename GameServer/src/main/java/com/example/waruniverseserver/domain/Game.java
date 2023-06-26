package com.example.waruniverseserver.domain;

import com.example.waruniverseserver.dto.Choice;
import com.example.waruniverseserver.dto.Result;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "game")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    Account account;
    int step = 1;
    int timeRemaining;
    @Enumerated(EnumType.STRING)
    Choice playerFirst;
    @Enumerated(EnumType.STRING)
    Choice playerSecond;
    @Enumerated(EnumType.STRING)
    Choice playerThird;
    @Enumerated(EnumType.STRING)
    Choice serverFirst;
    @Enumerated(EnumType.STRING)
    Choice serverSecond;
    @Enumerated(EnumType.STRING)
    Choice serverThird;
    @Enumerated(EnumType.STRING)
    Result result;

    public void setPlayerChoice(Choice choice){
        switch (this.step){
            case 1 -> setPlayerFirst(choice);
            case 2 -> setPlayerSecond(choice);
            case 3 -> setPlayerThird(choice);
        }
    }

    public void setServerChoice(Choice choice){
        switch (this.step){
            case 1 -> setServerFirst(choice);
            case 2 -> setServerSecond(choice);
            case 3 -> setServerThird(choice);
        }
    }

    public Choice getPrevPlayerChoice(){
        if (this.step == 3) return this.playerSecond;
        return this.playerFirst;
    }

    public Choice getPrevServerChoice(){
        if (this.step == 3) return this.serverSecond;
        return this.serverFirst;
    }

}
