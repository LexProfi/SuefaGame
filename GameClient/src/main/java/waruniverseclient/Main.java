package waruniverseclient;


import waruniverseclient.client.GameClient;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String serverIp = "127.0.0.1";
        int serverPort = 9000;

        if (args.length >= 2) {
            serverIp = args[0];
            serverPort = Integer.parseInt(args[1]);
        }

        GameClient client = new GameClient(serverIp, serverPort);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();
            client.handleUserCommand(input);
        }
    }
}