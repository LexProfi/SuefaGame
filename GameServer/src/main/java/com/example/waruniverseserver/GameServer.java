package com.example.waruniverseserver;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.example.waruniverseserver.domain.Account;
import com.example.waruniverseserver.domain.Game;
import com.example.waruniverseserver.dto.Choice;
import com.example.waruniverseserver.dto.Result;
import com.example.waruniverseserver.dto.client.Command;
import com.example.waruniverseserver.dto.client.CommandsDto;
import com.example.waruniverseserver.dto.client.SignInDto;
import com.example.waruniverseserver.dto.client.SignUpDto;
import com.example.waruniverseserver.dto.server.*;
import com.example.waruniverseserver.repositories.AccountRepository;
import com.example.waruniverseserver.repositories.GameRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
public class GameServer {
    @Value("${server.port}")
    private int serverPort;
    @Value("${game.choice.time}")
    private int choiceTime;
    private final Server server;
    private final AccountRepository accountRepository;
    private final GameRepository gameRepository;
    private final Map<Integer, Timer> timers;
    private final Map<Integer, Account> connections;
    private final Map<Integer, Game> activeGames;

    public GameServer(AccountRepository accountRepository, GameRepository gameRepository) {
        this.accountRepository = accountRepository;
        this.gameRepository = gameRepository;
        this.server = new Server();
        this.timers = new HashMap<>();
        this.connections = new HashMap<>();
        this.activeGames = new HashMap<>();
    }

    @PostConstruct
    public void start() {
        server.start();
        try {
            server.bind(serverPort);
            registerPackets();
            addServerListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerPackets() {
        server.getKryo().register(CommandsDto.class);
        server.getKryo().register(SignInDto.class);
        server.getKryo().register(SignUpDto.class);
        server.getKryo().register(GameStepDto.class);
        server.getKryo().register(RoundResultDto.class);
        server.getKryo().register(GameResultDto.class);
        server.getKryo().register(SignUpResultDto.class);
        server.getKryo().register(SignInResultDto.class);
        server.getKryo().register(TimeRemainingDto.class);
        server.getKryo().register(Choice.class);
        server.getKryo().register(Choice[].class);
        server.getKryo().register(Result.class);
        server.getKryo().register(Command.class);
    }

    private void addServerListener() {
        server.addListener(new Listener() {

            @Override
            public void received(Connection connection, Object dto) {
                switch (dto.getClass().getSimpleName()) {
                    case "SignUpDto" -> handleSignUp(connection, (SignUpDto) dto);
                    case "SignInDto" -> handleSignIn(connection, (SignInDto) dto);
                    case "CommandsDto" -> handleCommand(connection, (CommandsDto) dto);
                    default -> {}
                }
            }

            @Override
            public void disconnected(Connection connection) {
                int connectionId = connection.getID();
                Game game = activeGames.get(connectionId);
                if (game != null) {
                    Timer timer = timers.get(connectionId);
                    if(timer != null){
                        timer.cancel();
                        timers.remove(connectionId);
                    }
                    activeGames.remove(connectionId);
                    gameRepository.save(game);
                }
                connections.remove(connectionId);
            }
        });
    }

    private void handleSignUp(Connection connection, SignUpDto signUpDto) {
        String login = signUpDto.getLogin();
        String password = signUpDto.getPassword();
        Account existingAccount = accountRepository.findByLogin(login);
        if (existingAccount != null) {
            connection.sendTCP(new SignUpResultDto(false));
            connection.close();
        } else {
            Account account = new Account();
            account.setLogin(login);
            account.setPassword(password);
            account.setRegistrationDate(LocalDate.now());
            accountRepository.save(account);
            connection.sendTCP(new SignUpResultDto(true));
            connection.close();
        }
    }

    private void handleSignIn(Connection connection, SignInDto signInDto) {
        String login = signInDto.getLogin();
        String password = signInDto.getPassword();
        Account account = accountRepository.findByLoginAndPassword(login, password);
        if (account != null) {
            account.setLastLogin(LocalDate.now());
            accountRepository.save(account);
            connection.sendTCP(new SignInResultDto(true));
            connections.put(connection.getID(), account);
        } else {
            connection.sendTCP(new SignInResultDto(false));
            connection.close();
        }
    }

    private void handleCommand(Connection connection, CommandsDto commandsDto) {
        Account account = connections.get(connection.getID());
        if(account != null) {
            Game activeGame = activeGames.get(connection.getID());
            if (activeGame == null && commandsDto.getCommand() == Command.start) {
                Game lastActiveGame = gameRepository.findFirstByAccountAndResultIsNull(account);
                if (lastActiveGame != null) {
                    resumeGame(connection, lastActiveGame);
                } else {
                    startNewGame(connection, account);
                }
            } else if (activeGame != null && commandsDto.getCommand() == Command.choice) {
                makeChoice(connection, activeGame, commandsDto.getChoice());
            }
        }
    }

    private void startNewGame(Connection connection, Account account) {
        Game game = new Game();
        game.setAccount(account);
        game.setTimeRemaining(choiceTime);
        gameRepository.save(game);
        activeGames.put(connection.getID(), game);
        connection.sendTCP(new GameStepDto(game.getStep()));
        connection.sendTCP(new TimeRemainingDto(game.getTimeRemaining()));
        createTimer(connection, game);
    }

    private void resumeGame(Connection connection, Game game) {
        activeGames.put(connection.getID(), game);
        if(game.getStep() > 1) {
            connection.sendTCP(new RoundResultDto(getRoundResult(game.getPrevPlayerChoice(), game.getPrevPlayerChoice()),
                    game.getPrevPlayerChoice(), game.getPrevServerChoice()));
        }
        connection.sendTCP(new GameStepDto(game.getStep()));
        connection.sendTCP(new TimeRemainingDto(game.getTimeRemaining()));
        createTimer(connection, game);
    }

    private void makeChoice(Connection connection, Game game, Choice choice) {
        timers.get(connection.getID()).cancel();
        timers.remove(connection.getID());
        game.setPlayerChoice(choice);
        game.setServerChoice(getRandomChoice());
        if (game.getStep() < 3) {
            game.setStep(game.getStep() + 1);
            game.setTimeRemaining(choiceTime);
            connection.sendTCP(new RoundResultDto(getRoundResult(game.getPrevPlayerChoice(), game.getPrevPlayerChoice()),
                    game.getPrevPlayerChoice(), game.getPrevServerChoice()));
            connection.sendTCP(new GameStepDto(game.getStep()));
            connection.sendTCP(new TimeRemainingDto(game.getTimeRemaining()));
            createTimer(connection, game);
        } else {
            game.setResult(getGameResult(game));
            activeGames.remove(connection.getID());
            connection.sendTCP(new GameResultDto(game.getResult(), new Choice[]{game.getPlayerFirst(), game.getPlayerSecond(),
                    game.getPlayerThird()}, new Choice[]{game.getServerFirst(), game.getServerSecond(), game.getServerThird()}));
        }
        gameRepository.save(game);
    }

    private Choice getRandomChoice() {
        Random random = new Random();
        int index = random.nextInt(3);
        return Choice.values()[index];
    }

    private Result getRoundResult(Choice playerChoice, Choice serverChoice) {
        if (playerChoice == Choice.none) {
            return Result.lose;
        } else if (playerChoice == serverChoice) {
            return Result.draw;
        } else if (playerChoice == Choice.rock && serverChoice == Choice.scissors ||
                playerChoice == Choice.paper && serverChoice == Choice.rock ||
                playerChoice == Choice.scissors && serverChoice == Choice.paper) {
            return Result.win;
        } else {
            return Result.lose;
        }
    }

    private Result getGameResult(Game game) {
        int playerWins = 0;
        int serverWins = 0;

        playerWins += getRoundResult(game.getPlayerFirst(), game.getServerFirst()) == Result.win ? 1 : 0;
        serverWins += getRoundResult(game.getPlayerFirst(), game.getServerFirst()) == Result.lose ? 1 : 0;

        playerWins += getRoundResult(game.getPlayerSecond(), game.getServerSecond()) == Result.win ? 1 : 0;
        serverWins += getRoundResult(game.getPlayerSecond(), game.getServerSecond()) == Result.lose ? 1 : 0;

        playerWins += getRoundResult(game.getPlayerThird(), game.getServerThird()) == Result.win ? 1 : 0;
        serverWins += getRoundResult(game.getPlayerThird(), game.getServerThird()) == Result.lose ? 1 : 0;

        if (playerWins > serverWins) {
            return Result.win;
        } else if (playerWins < serverWins) {
            return Result.lose;
        } else {
            return Result.draw;
        }
    }

    private void createTimer(Connection connection, Game game){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int remainingTime = game.getTimeRemaining();

            @Override
            public void run() {
                game.setTimeRemaining(remainingTime);
                if (remainingTime == 15 || remainingTime == 5 || remainingTime == 3 || remainingTime == 1) {
                    connection.sendTCP(new TimeRemainingDto(remainingTime));
                }
                if (remainingTime == 0) {
                    makeChoice(connection, game, Choice.none);
                }
                remainingTime--;
            }
        }, 1000, 1000);

        timers.put(connection.getID(), timer);
    }

    @PreDestroy
    private void shutdown(){
        server.stop();
        timers.values().forEach(Timer::cancel);
        gameRepository.saveAll(activeGames.values());
    }
}