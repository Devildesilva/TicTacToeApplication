
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
        
        private Socket socket;
        private int playerNumber;
        private PrintWriter out;
        private Scanner in;
        private String mark;

        public PlayerHandler(Socket socket, int playerNumber) {
            this.socket = socket;
            this.playerNumber = playerNumber;
            this.mark = (playerNumber == 1) ? "X" : "O";
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new Scanner(socket.getInputStream());

                out.println("Welcome, Player " + playerNumber + "! You are " + mark + ".");
                if(playerNumber == 2){
                    resetGame();
                }
                out.println("PLAYER_NUM:"+playerNumber);
                out.println("MARK:"+mark);
                sendBoardState();

                while (true) {
                    if (in.hasNextLine()) {
                        String input = in.nextLine();
                        System.out.println("Received from Player " + playerNumber + ": " + input);
                        int move = Integer.parseInt(input);

                        if (isValidMove(move)) {
                            if (currentPlayer == playerNumber) {
                                makeMove(move, mark);
                                if (playerNumber == 1) {
                                    currentPlayer = 2;
                                } else {
                                    currentPlayer = 1;
                                }
                                
                                sendBoardState();
                                if (checkWinner()) {
                                    out.println("WIN");
                                    resetGame();
                                    sendBoardState();
                                } else if (isBoardFull()) {
                                    out.println("DRAW");
                                    resetGame();
                                    sendBoardState();
                                }

                                
                            } else {
                                System.out.println("Current turn is " + currentPlayer);
                                out.println("WRONG_TURN");
                            }
                        } else {
                            out.println("INVALID");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Player " + playerNumber + " disconnected: " + e.getMessage());
            } finally {
                disconnectPlayer();
            }
        }

        private void disconnectPlayer() {
            synchronized (players) {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    System.out.println("Error closing socket: " + e.getMessage());
                }

                players[playerNumber - 1] = null;
                connectedPlayers--;
                System.out.println("Player " + playerNumber + " has left the game.");
            }
        }

        private void sendBoardState() {
            synchronized (players) {
                StringBuilder boardState = new StringBuilder();
                for (String cell : board) {
                    boardState.append(cell).append(",");
                }
                String boardStateMessage = "BOARD:" + boardState.toString();
                String boardTurnMessage = "TURN:" + currentPlayer;
                System.out.println("Sent board state: " + boardState.toString());

                for (Socket player : players) {
                    if (player != null) {
                        try {
                            PrintWriter playerOut = new PrintWriter(player.getOutputStream(), true);
                            playerOut.println(boardStateMessage);
                            playerOut.println(boardTurnMessage);
                        } catch (IOException e) {
                            System.out.println("Error sending board state to player: " + e.getMessage());
                        }
                    }
                }
            }
        }

        private boolean isValidMove(int move) {
            return move >= 0 && move < 9 && board[move].equals(" ");
        }

        private void makeMove(int move, String mark) {
            board[move] = mark;
        }

        private boolean checkWinner() {
            int[][] winConditions = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Rows
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Columns
                {0, 4, 8}, {2, 4, 6} // Diagonals
            };

            for (int[] condition : winConditions) {
                if (board[condition[0]].equals(mark)
                        && board[condition[1]].equals(mark)
                        && board[condition[2]].equals(mark)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isBoardFull() {
            for (String cell : board) {
                if (cell.equals(" ")) {
                    return false;
                }
            }
            return true;
        }

        private void resetGame() {
            for (int i = 0; i < board.length; i++) {
                board[i] = " ";
            }
            currentPlayer = 1;  // Reset to Player 1's turn
        }
        
    }

}
