

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private static final int PORT = 8888;
    public static Map<Integer, Boolean> uid = Collections.synchronizedMap(new HashMap<>()); //int UserID and bool isOnline?
    public static Integer[] hasTheBall = {-1, -1}; //[to, from]

    public static void main(String[] args) {

//        uid.put(0, false);
//        uid.put(1, true);
//        uid.put(2, true);
        runServer();
    }

    private static void runServer(){

        ServerSocket serverSocket = null;

        try{
            serverSocket = new ServerSocket(PORT);
            System.out.println("waiting on clients");
            while(true){
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }


}
