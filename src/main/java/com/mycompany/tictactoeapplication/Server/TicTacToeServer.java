
package com.mycompany.tictactoeapplication.Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicTacToeServer {
    
    private static final int PORT = 12345;
    private static final int MAX_PLAYERS = 2;
    private static Socket[] players = new Socket[MAX_PLAYERS];
    private static int connectedPlayers = 0;
    private static String[] board = {" ", " ", " ", " ", " ", " ", " ", " ", " "};
    private static int currentPlayer = 1;

    
    public static void main(String[] args) {
        System.out.println("Tic Tac Toe Server is Running...");
        ExecutorService pool = Executors.newFixedThreadPool(MAX_PLAYERS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                if (connectedPlayers < MAX_PLAYERS) {
                    Socket socket = serverSocket.accept();
                    System.out.println("New player connected: " + socket);
                    synchronized (players) {
                        if (connectedPlayers < MAX_PLAYERS) {
                            players[connectedPlayers] = socket;
                            connectedPlayers++;
                            pool.execute(new PlayerHandler(socket, connectedPlayers));
                        } else {
                            socket.close();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static class PlayerHandler implements Runnable {
        
    }

    
}
