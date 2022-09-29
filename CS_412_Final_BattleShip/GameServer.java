import java.io.*;
import java.net.*;
import java.lang.*;

public class GameServer extends Ship {
    private ServerSocket ss;
    private int numPlayers;
    private ServerConnection player1;
    private ServerConnection player2;
    private String p1ButtonName;
    private String p2ButtonName;
    private String[] p1ShipLoc = new String[2];
    private String[] p2ShipLoc = new String[2];
    private int playerID;


    public GameServer(){
        System.out.println("~~~Game Server~~~");
        numPlayers = 0;

        try{
            ss = new ServerSocket(6420);
        } catch(IOException ex){
            System.out.println("IOException from GameServer constructor");
        }
    }

    public void acceptConnections(){
        try{
            System.out.println("Waiting for connections....");
            while(numPlayers < 2){
                Socket s = ss.accept();
                numPlayers++;
                System.out.println("Number of players connected: " + numPlayers);
                ServerConnection serverConnection = new ServerConnection(s, numPlayers);
                if(numPlayers == 1){
                    player1 = serverConnection;
                }else{
                    player2 = serverConnection;
                }
                Thread t = new Thread(serverConnection);
                t.start();
            }
            System.out.println("Max Number of player reached no longer accepting connections.");
        }catch(IOException ex){
            System.out.println("IOException from accept connections");
        }
    }

    private class ServerConnection implements Runnable {
        private Socket socket;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;

        public ServerConnection(Socket s, int id){
            socket = s;
            playerID = id;
            newBattleshipLoc();
            try{
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());
            }catch(IOException ex){
                System.out.println("IOException in server connection constructor");
            }
        }
        public void run(){
            try{
                dataOut.writeInt(playerID);
                if(playerID == 1){
                    for(int i = 0; i < p1ShipLoc.length; i++) {
                        dataOut.writeUTF(p1ShipLoc[i]);
                    }
                }else{
                    for(int i = 0; i < p2ShipLoc.length; i++) {
                        dataOut.writeUTF(p2ShipLoc[i]);
                    }
                }
                dataOut.flush();
                while (true){
                    if(playerID == 1){
                        p1ButtonName = dataIn.readUTF();
                        System.out.println("Player 1 clicked button # " + p1ButtonName);
                        player2.sendButtonName(p1ButtonName);
                        playerID = 1;
                        hitOrMiss(p1ButtonName, playerID);
                        continue;
                    } else{
                        p2ButtonName = dataIn.readUTF();
                        System.out.println("Player 2 clicked button # " + p2ButtonName);
                        player1.sendButtonName(p2ButtonName);
                        playerID = 2;
                        hitOrMiss(p2ButtonName, playerID);
                        continue;
                    }
                }
            } catch(IOException ex){
                System.out.println("IOException from run()");
            }
        }

        public void hitOrMiss(String x, int pid){
            if(pid == 1){
                for(int i = 0; i < p2ShipLoc.length; i++){
                    if(x.equals(p2ShipLoc[i])){
                        System.out.println("p1 hit at " + x);
                        if (p2ShipLoc[i].equals(x))
                        {
                            p2ShipLoc[i] = null;

                            if(p2ShipLoc[0] == null && p2ShipLoc[1] == null){
                                System.out.println("P1 wins");
                                System.exit(0);
                            }
                            return;
                        }
                    }
                }
                System.out.println("p1 missed at " + x);
                return;
            }else{
                for(int i = 0; i < p1ShipLoc.length; i++){
                    if(x.equals(p1ShipLoc[i])){
                        System.out.println("p2 hit at " + x);
                        if (p1ShipLoc[i].equals(x))
                        {
                            p1ShipLoc[i] = null;
                            if(p1ShipLoc[0] == null && p1ShipLoc[1] == null){
                                System.out.println("P2 wins");
                                System.exit(0);
                            }
                            return;
                        }
                        return;
                    }
                }
                System.out.println("p2 missed at " + x);
                return;
            }
        }

        public void sendButtonName(String n){
            try{
                dataOut.writeUTF(n);
                dataOut.flush();
            } catch(IOException e){
                System.out.println("IOException from sendButtonNum server side");
            }
        }
    }

    private void newBattleshipLoc() {
        int iterator = 0;
        Ship s = new Ship();
        s.positionNumber = (int)Math.floor(Math.random() * (4 - 1) + 1);
        s.positionLetter = (int)Math.floor(Math.random() * (4 - 1) + 1);
        s.size = 2;
        s.firstPosition = String.valueOf(s.positionNumber);
        char charofPos = (char)(s.positionLetter + 64);
        int lastNumberPos;
        int direction = (int)Math.floor(Math.random() * (3 - 1) + 1);
        if(direction == 1) // 1 will stand for vertical placement
        {
            System.out.println("Direction: Vertical");
            lastNumberPos = s.setCoordsVert(s.positionLetter);
            if(((s.size - 1) + s.positionNumber) > 3)
            {
                for(int i = s.size; i > 0; i--)
                {
                    String coord = String.valueOf(i + (s.positionNumber - 2));
                    coord += charofPos;
                    System.out.println(coord);
                    if(playerID == 1){
                        p1ShipLoc[iterator] = coord;
                        System.out.println("Player 1 Ship Location " + p1ShipLoc[iterator]);
                        iterator++;
                    }else{
                        p2ShipLoc[iterator] = coord;
                        System.out.println("Player 2 Ship Location " + p2ShipLoc[iterator]);
                        iterator++;
                    }
                }
            }
            else
            {
                for(int i = 0; i < s.size; i++)
                {
                    String coord = String.valueOf(i + s.positionNumber);
                    coord += charofPos;
                    //coord += i;
                    System.out.println(coord);
                    if(playerID == 1){
                        p1ShipLoc[iterator] = coord;
                        System.out.println("Player 1 Ship Location " + p1ShipLoc[iterator]);
                        iterator++;
                    }else{
                        p2ShipLoc[iterator] = coord;
                        System.out.println("Player 2 Ship Location " + p2ShipLoc[iterator]);
                        iterator++;
                    }
                }
            }
        }else
        {
            System.out.println("Direction: Horizontal");
            lastNumberPos = s.setCoordsHoriz(s.positionNumber);
            s.lastPostition = String.valueOf(s.positionNumber);
            charofPos = (char)(lastNumberPos + 64);
            s.lastPostition += charofPos;
            if(((s.size - 1) + (s.positionLetter) > 3))
            {
                for(int i = s.size; i > 0; i--)
                {
                    String coord = String.valueOf(s.positionNumber);
                    charofPos = (char)(i + s.positionLetter + 62);
                    coord += charofPos;
                    System.out.println(coord);
                    if(playerID == 1){
                        p1ShipLoc[iterator] = coord;
                        System.out.println("Player 1 Ship Location " + p1ShipLoc[iterator]);
                        iterator++;
                    }else{
                        p2ShipLoc[iterator] = coord;
                        System.out.println("Player 2 Ship Location " + p2ShipLoc[iterator]);
                        iterator++;
                    }
                }
            }
            else
            {
                for(int i = 0; i < s.size; i++)
                {
                    String coord = String.valueOf(s.positionNumber);
                    charofPos = (char)(i + s.positionLetter + 64);
                    coord += charofPos;
                    System.out.println(coord);
                    if(playerID == 1){
                        p1ShipLoc[iterator] = coord;
                        System.out.println("Player 1 Ship Location " + p1ShipLoc[iterator]);
                        iterator++;
                    }else{
                        p2ShipLoc[iterator] = coord;
                        System.out.println("Player 2 Ship Location " + p2ShipLoc[iterator]);
                        iterator++;
                    }
                }
            }
        }
    }

    public static void main(String[] args){
        GameServer gs = new GameServer();

        gs.acceptConnections();
    }
}
