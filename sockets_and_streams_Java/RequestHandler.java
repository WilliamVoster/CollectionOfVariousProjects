import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

public class RequestHandler implements Runnable {

    Scanner scanner;
    LinkedBlockingQueue<String> queue;

    public RequestHandler(Scanner scanner, LinkedBlockingQueue<String> queue){

            this.scanner = scanner;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {

            while (scanner.hasNextLine()) {

                String line = scanner.nextLine();

                try{
                    queue.put(line);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }

        } catch (NoSuchElementException | IllegalStateException ignored) {

        } finally {
            scanner.close();

            try {
                queue.put("DISCONNECTED");
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}