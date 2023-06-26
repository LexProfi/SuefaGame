package waruniverseclient.client;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import waruniverseclient.dto.Choice;
import waruniverseclient.dto.Result;
import waruniverseclient.dto.client.*;
import waruniverseclient.dto.server.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class GameClient {
    private final Client client;
    private final String serverIp;
    private final int serverPort;
    private Stage stage;

    public GameClient(String serverIp, int serverPort) {
        Log.set(Log.LEVEL_NONE);
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.stage = Stage.LOGIN;
        client = new Client();
        registerClasses();
        addListeners();
        System.out.println("Rock-paper-scissors game. Use the 'signup' commands to register an account, or 'signin' " +
                "to connect to the server.");
    }

    private void registerClasses() {
        Kryo kryo = client.getKryo();
        kryo.register(CommandsDto.class);
        kryo.register(SignInDto.class);
        kryo.register(SignUpDto.class);
        kryo.register(GameStepDto.class);
        kryo.register(RoundResultDto.class);
        kryo.register(GameResultDto.class);
        kryo.register(SignUpResultDto.class);
        kryo.register(SignInResultDto.class);
        kryo.register(TimeRemainingDto.class);
        kryo.register(Choice.class);
        kryo.register(Choice[].class);
        kryo.register(Result.class);
        kryo.register(Command.class);
    }

    private void addListeners() {
        client.addListener(new Listener() {
            public void received(Connection connection, Object dto) {
                switch (dto.getClass().getSimpleName()) {
                    case "SignUpResultDto" -> handleSignUpResult((SignUpResultDto) dto);
                    case "SignInResultDto" -> handleSignInResult((SignInResultDto) dto);
                    case "GameStepDto" -> handleGameStep((GameStepDto) dto);
                    case "TimeRemainingDto" -> handleTimeRemainingDto((TimeRemainingDto) dto);
                    case "RoundResultDto" -> handleRoundResult((RoundResultDto) dto);
                    case "GameResultDto" -> handleGameResult((GameResultDto) dto);
                }
            }

            public void disconnected (Connection connection){
                stage = Stage.LOGIN;
                System.out.println("Use the 'signup' command to register an account or 'signin' to connect to the server.");
            }
        });
    }

    private void connect() {
        try {
            client.start();
            client.connect(5000, serverIp, serverPort);
        } catch (IOException e) {
            System.out.println("Failed to connect to the server: " + e.getMessage());
        }
    }


    public void handleUserCommand(String input) {
        switch (stage) {
            case LOGIN -> handleLoginStageCommand(input);
            case MENU -> handleMenuStageCommand(input);
            case GAME -> handleGameStageCommand(input);
            case RESULT -> handleResultStageCommand(input);
        }
    }

    private void handleLoginStageCommand(String input) {
        String[] params = input.split("=");
        if ((input.startsWith("signup=") || input.startsWith("signin=")) && params.length == 3) {
            connect();
            if (params[0].equals("signup")) {
                client.sendTCP(new SignUpDto(params[1], params[2]));
            } else {
                client.sendTCP(new SignInDto(params[1], params[2]));
            }
        } else {
            System.out.println("Use 'signup=login=password' or 'signin=login=password' command.");
        }
    }

    private void handleMenuStageCommand(String input) {
        if (input.equals("start")) {
            client.sendTCP(new CommandsDto(Command.start));
        } else if (input.equals("logout")) {
            client.stop();
        } else {
            System.out.println("Use 'start' command to start game, or 'logout' command to disconnect.");
        }
    }

    private void handleGameStageCommand(String input) {
        if (input.equals("rock") || input.equals("paper") || input.equals("scissors")) {
            client.sendTCP(new CommandsDto(Command.choice, Choice.valueOf(input)));
        } else if (input.equals("logout")) {
            client.stop();
        } else {
            System.out.println("Use 'rock', 'paper', 'scissors' to choice, or 'logout' command to disconnect.");
        }
    }

    private void handleResultStageCommand(String input) {
        if (input.equals("logout")) {
            client.stop();
            System.out.println("You're logout. Use the 'signup' commands to register an account, or 'signin' to connect" +
                    " to the server.");
        } else {
            System.out.println("Use the 'logout' command or wait for the transition to the main menu.");
        }
    }

    private void handleSignUpResult(SignUpResultDto dto) {
        if(dto.isResult()){
            System.out.println("Account created. Use the 'signup' commands to register an account, or 'signin' to connect" +
                    " to the server.");
        } else {
            System.out.println("Login already used. Try again. Use the 'signup' commands to register an account, or " +
                    "'signin' to connect to the server.");
        }
        client.stop();
    }

    private void handleSignInResult(SignInResultDto dto) {
        if(dto.isResult()){
            stage = Stage.MENU;
            System.out.println("You're successfully logged in. Use 'start' to play game, or 'logout' command.");
        } else {
            System.out.println("Wrong login or password. Try again.");
        }
    }

    private void handleGameStep(GameStepDto dto) {
        stage = Stage.GAME;
        System.out.println("Game Step: " + dto.getStepNumber());
        System.out.println("Use 'rock', 'paper', 'scissors' to choice, or 'logout' command to disconnect.");
    }
    
    private void handleTimeRemainingDto(TimeRemainingDto dto) {
        System.out.println("Time remaining: " + dto.getTimeRemaining() + " seconds");
    }

    private void handleRoundResult(RoundResultDto dto) {
        System.out.println("Round result: " + dto.getResult());
        System.out.println("Your choice: " + dto.getPreviousPlayerChoice());
        System.out.println("Opponent choice: " + dto.getPreviousServerChoice());
    }

    private void handleGameResult(GameResultDto dto) {
        System.out.println("Game Result: " + dto.getResult());

        System.out.println("Your choices:");
        Arrays.stream(dto.getPlayerChoices()).forEach(System.out::println);

        System.out.println("Opponent choices:");
        Arrays.stream(dto.getServerChoices()).forEach(System.out::println);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stage = Stage.MENU;
                System.out.println("Play again? Enter 'start' to play a game or 'logout' to disconnect.");
            }
        }, 5000);
    }
}