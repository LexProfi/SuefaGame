package com.example.waruniverseserver.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "account")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String login;
    String password;
    @Column(name = "last_login")
    LocalDate lastLogin;
    @Column(name = "registration_date")
    LocalDate registrationDate;
}
