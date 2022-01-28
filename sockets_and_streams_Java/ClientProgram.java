import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientProgram implements AutoCloseable{

    private static final int PORT = 8888;

    private final Scanner reader;
    private final PrintWriter writer;
    public int userID = -2;

    public ClientProgram() throws IOException, ConnectException {

        Socket socket = new Socket("localhost", PORT);
        reader = new Scanner(socket.getInputStream());
        writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public void connect(){

        String line = reader.nextLine();
        String[] splitString = line.split(" ");

        if(splitString[0].compareTo("NEW_USER") == 0){
            userID = Integer.parseInt(splitString[1]);
        }else{
            System.out.println("Could not connect to server. Exiting");
            try {
                close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
    }

    public static void main(String[] args) throws IOException {

        ClientProgram client = null;

        try{
            client = new ClientProgram();
        }catch (ConnectException e){
            System.out.println("Could not connect to server. Exiting");
            System.exit(0);
        }

        Scanner in = new Scanner(System.in);
        LinkedBlockingQueue<String> serverResponsesQueue = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<String> userRequestsQueue = new LinkedBlockingQueue<>();

        client.connect();
        System.out.println("Type 'players' to see who is online\nType 'ball' to see who has " +
                "the ball\nType 'throw' followed by a user's id to send the ball to them. e.g. throw 4");
        System.out.println("Logged in as Player " + client.userID);

        Thread serverResponses = new Thread(new RequestHandler(client.reader, serverResponsesQueue));
        serverResponses.start();

        Thread userRequests = new Thread(new RequestHandler(in, userRequestsQueue));
        userRequests.start();

        try{
            //Initial request(s)
            client.writer.println("PLAYERS");

            while(true){

                Thread.sleep(500);

                while(!serverResponsesQueue.isEmpty()){
                    String line = serverResponsesQueue.take();
                    String[] splitString = line.split(" ");
                    int target;
                    switch (splitString[0].toLowerCase()){
                        case "ball_from":
                            target = Integer.parseInt(splitString[1]);
                            String output = target == -1 ? "You received the ball from the Server" : "You received the ball from " + target;
                            System.out.println("\n" + output);
                            System.out.println("Whom would you like to throw the ball to?");
                            break;
                        case "pass":
                            int targetA = Integer.parseInt(splitString[1]);
                            int targetB = Integer.parseInt(splitString[2]);
                            if(targetA == -1){
                                System.out.println("Server passed the ball to Player " + targetB);
                                break;
                            }else if(targetB == -1 || targetB == client.userID || targetA == client.userID)
                                break;
                            System.out.println("Player " + targetA + " passed the ball to Player " + targetB);
                            break;
                        case "user_joined":
                            target = Integer.parseInt(splitString[1]);
                            System.out.println("Player " + target + " joined the game");
                            client.writer.println("PLAYERS");
                            break;
                        case "user_left":
                            target = Integer.parseInt(splitString[1]);
                            System.out.println("Player " + target + " left the game");
                            client.writer.println("PLAYERS");
                            break;
                        case "players_online":
                            System.out.println("Online players:\n" + Arrays.asList(splitString).subList(1, splitString.length));
                            break;
                        case "player_with_ball":
                            target = Integer.parseInt(splitString[1]);
                            System.out.println("Player " + target + " has the ball");
                            break;
                        case "invalid_pass":
                            if(splitString[1].toLowerCase().equals("user_offline")){
                                System.out.println("Could not pass the ball to that player, you still have the ball");

                            }else if(splitString[1].toLowerCase().equals("no_ball")){
                                System.out.println("The ball is not yours to throw");
                                client.writer.println("BALL");
                            }
                            break;
                        case "valid_pass":
                            System.out.println("Great throw! Good technique, excellent form");
                            break;

                        case "unknown_command":
                            System.out.println("Could not compute that query");
                            System.out.println("Type 'players' to see who is online\nType 'ball' to see who has " +
                                    "the ball\nType 'throw' followed by a user's id to send the ball to them. e.g. throw 4");
                            break;

                    }
                }

                if(!userRequestsQueue.isEmpty()){
                    String line = userRequestsQueue.take();
                    client.writer.println(line);
                }

            }


        }catch(Exception e){
            System.out.println(e.getMessage());
        }finally {
            serverResponses.interrupt();
            userRequests.interrupt();
            try {
                client.close();
            } catch (Exception e) {
                System.out.println("Could not close client connection");
            }
        }
    }

    @Override
    public void close() throws Exception {
        reader.close();
        writer.close();
    }
}
















