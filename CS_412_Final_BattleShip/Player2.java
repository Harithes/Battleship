import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.awt.event.*;
import java.util.Arrays;

public class Player2 extends JFrame {
    private int height;
    private int width;
    private Container contentPane;
    private JTextArea message;
    private JTextArea shipLocMessage;
    private JButton a1;
    private JButton a2;
    private JButton a3;
    private JButton b1;
    private JButton b2;
    private JButton b3;
    private JButton c1;
    private JButton c2;
    private JButton c3;
    private int playerID;
    private int opposingPlayerID;

    private boolean buttonsEnabled;
    private String[] shipLoc = new String[2];

    private ClientConnection clientC;


    public Player2(int h, int w){
        width = w;
        height = h;
        contentPane = this.getContentPane();
        message = new JTextArea();
        shipLocMessage = new JTextArea();

        a1 = new JButton("1A");
        a2 = new JButton("2A");
        a3 = new JButton("3A");
        b1 = new JButton("1B");
        b2 = new JButton("2B");
        b3 = new JButton("3B");
        c1 = new JButton("1C");
        c2 = new JButton("2C");
        c3 = new JButton("3C");

    }

    public void setUpGameBoard(){
        this.setSize(width, height);
        this.setTitle("BattleShip Player " + playerID);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        contentPane.setLayout(new GridLayout(4, 3));
        contentPane.add(a1);
        contentPane.add(a2);
        contentPane.add(a3);
        contentPane.add(b1);
        contentPane.add(b2);
        contentPane.add(b3);
        contentPane.add(c1);
        contentPane.add(c2);
        contentPane.add(c3);

        contentPane.add(message);

        message.setText("This is a test.");
        message.setWrapStyleWord(true);
        message.setLineWrap(true);
        message.setEditable(false);

        contentPane.add(shipLocMessage);
        shipLocMessage.setText("This is where you ship location will be displayed");
        shipLocMessage.setWrapStyleWord(true);
        shipLocMessage.setLineWrap(true);
        shipLocMessage.setEditable(false);


        if(playerID == 1){
            message.setText("Your are player 1 you start.");
            shipLocMessage.setText("Your ship cordinates are " + Arrays.toString(shipLoc));
            opposingPlayerID = 2;
            buttonsEnabled = true;
            toggleButtons();
        }else{
            message.setText("You are player 2 you go second.");
            shipLocMessage.setText("Your ship cordinates are " + Arrays.toString(shipLoc));
            opposingPlayerID = 1;

            buttonsEnabled = false;
            toggleButtons();
            Thread t = new Thread(new Runnable() {
                public void run() {
                    updateTurn();
                }
            });
            t.start();
        }



        this.setVisible(true);
    }

    public void connectToServer() {
        clientC = new ClientConnection();
    }

    public void setUpButtons() {
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JButton b = (JButton) ae.getSource();
                String bName = b.getText();
                message.setText("You clicked " + bName + ". Now wait for Player " + opposingPlayerID + " to shoot.");


                buttonsEnabled = false;
                toggleButtons();

                System.out.println("Cordinate to attack: " + bName);

                clientC.sendButtonName(bName);

                Thread t = new Thread(new Runnable() {
                    public void run() {
                        updateTurn();
                    }
                });
                t.start();
            }
        };

        a1.addActionListener(al);
        a2.addActionListener(al);
        a3.addActionListener(al);
        b1.addActionListener(al);
        b2.addActionListener(al);
        b3.addActionListener(al);
        c1.addActionListener(al);
        c2.addActionListener(al);
        c3.addActionListener(al);
    }

    public void toggleButtons(){
        a1.setEnabled(buttonsEnabled);
        a2.setEnabled(buttonsEnabled);
        a3.setEnabled(buttonsEnabled);
        b1.setEnabled(buttonsEnabled);
        b2.setEnabled(buttonsEnabled);
        b3.setEnabled(buttonsEnabled);
        c1.setEnabled(buttonsEnabled);
        c2.setEnabled(buttonsEnabled);
        c3.setEnabled(buttonsEnabled);
    }


    public void updateTurn(){
        String n = clientC.recieveButtonName();
        message.setText("Your enemy attacked at " + n + ". Your Turn");
        buttonsEnabled = true;
        toggleButtons();
    }


    private class ClientConnection {
        private Socket socket;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;

        public ClientConnection(){
            System.out.println("~~~Client~~~");
            try{
                socket = new Socket("localhost", 6420);
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());
                playerID = dataIn.readInt();
                shipLoc[0] = dataIn.readUTF();
                shipLoc[1] = dataIn.readUTF();


                System.out.println("Player " + playerID + " has connected");
                for(int i = 0; i < shipLoc.length; i++){
                    System.out.println("Player Ship Location: " + shipLoc[i] + " ");
                }

            } catch(IOException ex){
                System.out.println("IOException from client connection constructor");
            }
        }

        public void sendButtonName(String n){
            try{
                dataOut.writeUTF(n);
                dataOut.flush();
            } catch(IOException e){
                System.out.println("IOException from sendButtonName");
            }
        }


        public String recieveButtonName(){
            String tempVal = null;
            try{
                tempVal = dataIn.readUTF();
                System.out.println("Player" + opposingPlayerID + " clicked button " + tempVal);
            } catch(IOException e){
                System.out.println("IOException recieve button num ");
            }
            return tempVal;
        }
    }

    public static void main(String[] args) {
        Player2 p = new Player2(500, 500);
        p.connectToServer();
        p.setUpGameBoard();
        p.setUpButtons();
    }
}
