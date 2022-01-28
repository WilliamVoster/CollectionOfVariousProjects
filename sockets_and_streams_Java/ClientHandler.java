import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientHandler implements Runnable{

    private Socket socket;
    private Map<Integer, Boolean> localCopyUsers;
    private Integer[] localHasTheBall = {-1, -1};

    public ClientHandler(Socket socket){
        this.socket = socket;
    }


    private int createAndLogIn(){
        // Returns the logged in users id

        synchronized (Server.uid){
            int lastID = -1;
            for (var pair : Server.uid.entrySet()) {
                if(pair.getKey() > lastID){
                    lastID = pair.getKey();
                }
            }
            lastID++;

            Server.uid.put(lastID, true);

            //System.out.println("User " + lastID + " logged in");
            return lastID;
        }
    }

    private void logOutUser(int userID){
        if(userID == -1) return;
        for (var pair : Server.uid.entrySet()) {
            if(pair.getKey() == userID){
                pair.setValue(false);
                break;
            }
        }
        if(Server.hasTheBall[0] == userID){
            Server.hasTheBall[1] = userID;
            Server.hasTheBall[0] = -1;
        }
    }

    private void deepUpdateLocal(){
        localCopyUsers = Server.uid.entrySet().stream() // deep copy from server to local
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void checkUsersChange(PrintWriter output){
        synchronized (Server.uid){

            boolean updateLocal = false;
            for (var serverPair : Server.uid.entrySet()){
                for (var localPair : localCopyUsers.entrySet()){
                    if(
                        (serverPair.getKey().equals(localPair.getKey())) &&
                        (serverPair.getValue() != localPair.getValue())
                    ){
                        output.println("USER_LEFT " + serverPair.getKey());
                        updateLocal = true;
                    }
                }
            }

            if(!localCopyUsers.entrySet().containsAll(Server.uid.entrySet())){ //if local copy is missing a user

                updateLocal = true;
                Map<Integer, Boolean> newUsers = Server.uid.entrySet().stream()
                        .filter(serverEntry -> {
                            if(!serverEntry.getValue()) return false; //ignore new users who are not logged in
                            boolean inSet = false;
                            for (var localEntry : localCopyUsers.entrySet()) {
                                if(serverEntry.getKey().equals(localEntry.getKey()))
                                    inSet = true;
                            }
                            return !inSet;
                        }).collect(Collectors.toMap(Map.Entry::getKey, willAlwaysBeTrue -> true));

                for (var entry : newUsers.entrySet()) {
                    output.println("USER_JOINED " + entry.getKey());
                }

            }

            if(updateLocal)
                deepUpdateLocal();

        }
    }

    private void checkBallChange(PrintWriter output){
        synchronized (Server.hasTheBall){
            if(!Server.hasTheBall[0].equals(localHasTheBall[0])){
                output.println("PASS " + Server.hasTheBall[1] + " " + Server.hasTheBall[0]);
                localHasTheBall = new Integer[]{Server.hasTheBall[0], Server.hasTheBall[1]};
            }
        }
    }

    @Override
    public void run() {

        int userID = -1;
        boolean youHaveTheBall = false;

        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
        Thread requests = null;

        try(
            Scanner scanner = new Scanner(socket.getInputStream());
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            // ^^ these will close with the try/catch
        ) {

            userID = createAndLogIn();
            writer.println("NEW_USER " + userID);
            System.out.println("Created new user: " + userID);

            deepUpdateLocal();

            requests = new Thread(new RequestHandler(scanner, queue));
            requests.start();

            String line = "";

            while (true) {

                Thread.sleep(500);

                checkUsersChange(writer); // if any users have left or joined notify client and update local copy

                checkBallChange(writer);  // if the ball changed hands (without current player being involved)

                if (Server.hasTheBall[0] == -1) {
                    synchronized (Server.hasTheBall) {
                        if (Server.hasTheBall[0] != -1) break;
                        Server.hasTheBall[0] = userID;
                        Server.hasTheBall[1] = -1; // the ball came from the server
                        localHasTheBall = new Integer[]{userID, -1};
                        youHaveTheBall = true;
                        writer.println("BALL_FROM " + -1);
                    }
                }else if (Server.hasTheBall[0] == userID && !youHaveTheBall){

                    System.out.println(String.format("Ball received by %d from %d", Server.hasTheBall[0], Server.hasTheBall[1]));
                    writer.println("BALL_FROM " + Server.hasTheBall[1]);
                    youHaveTheBall = true;
                }


                if(!queue.isEmpty()){

                    line = queue.take();

                    String[] splitString = line.split(" ");
                    switch (splitString[0].toLowerCase()) {
                        case "disconnected":
                            throw new UserDisconnectedException();
                        case "players":
                            String onlinePlayers = "";
                            for (var pair : Server.uid.entrySet()) {
                                if (pair.getValue()) {
                                    onlinePlayers = onlinePlayers + " " + pair.getKey();
                                }
                            }
                            writer.println("PLAYERS_ONLINE" + onlinePlayers);
                            break;

                        case "ball":
                            writer.println("PLAYER_WITH_BALL " + Server.hasTheBall[0]);
                            break;

                        case "throw":
                            int target = Integer.parseInt(splitString[1]);

                            synchronized (Server.hasTheBall) {
                                synchronized (Server.uid){
                                    if (Server.hasTheBall[0] == userID) {
                                        boolean userOnline = false;
                                        for (var pair :Server.uid.entrySet()) {
                                            if(pair.getKey().equals(target) && pair.getValue()){
                                                userOnline = true;
                                                break;
                                            }
                                        }
                                        if(!userOnline){
                                            writer.println("INVALID_PASS USER_OFFLINE");
                                            break;
                                        }
                                        Server.hasTheBall[0] = target;
                                        Server.hasTheBall[1] = userID; // index 2 keeps track of who sent the ball
                                        localHasTheBall = new Integer[]{target, userID};
                                        youHaveTheBall = false;
                                        writer.println("VALID_PASS");
                                    }else{
                                        writer.println("INVALID_PASS NO_BALL");
                                    }
                                }
                            }
                            break;

                        default:
                            System.out.println("unknown command " + line);
                            writer.println("UNKNOWN_COMMAND");
                    }
                }
            }

        }catch (NoSuchElementException e) {
            System.out.println("Could not parse client input " + e.getMessage());

        }catch (UserDisconnectedException ignored){

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            logOutUser(userID);
            System.out.println("User " + userID + " disconnected.");
            if (requests != null)
                requests.interrupt();
        }


    }

    private class UserDisconnectedException extends Exception{
        public UserDisconnectedException(){ super(); }
    }
}












