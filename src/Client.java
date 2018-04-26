import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client {

    private int serverPort; // server port number to connect

    /**
     * Constructor
     *
     * @param serverPort: the server port number to connect with server
     */
    private Client(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * method to run this client
     */
    private void start() {
        try {
            // set up socket to connect to server
            Socket socket = new Socket("127.0.0.1", serverPort); // initialize a socket
            System.out.println("Connected server!");

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream()); // initialize a dataOutputStream to output command to server
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream()); //initialize a objectInputStream to receive results from srever

            dos.writeUTF("Client connected now"); // send a message to server to present a successful connection

            // print instructions on terminal
            System.out.println("Please enter an order as format: <operation key value/time> with 1 space between each argument, e.g. put A b, get B 2, del A b, diff A 1 5.");
            System.out.println("If you want to quit, just enter quit.");

            // initialize a result type
            Server.SerialList result;

            // scanner to read input command from terminal
            Scanner sc = new Scanner(System.in);
            while (sc.hasNext()) {
                String order = sc.nextLine(); // catch the command

                // quit command, ready to terminate
                if (order.equals("quit")) {
                    dos.writeUTF("quit");
                    System.out.println("Client quitting..."); // output quit info
                    result = (Server.SerialList) ois.readObject(); // still receive an object from server
                    System.out.println(result.signal); // make user server receive quit command, and quit together
                    break;
                }

                // pre-decide if it's an invalid order
                String command = order.split(" ")[0];
                if (!command.equals("get") && !command.equals("put") && !command.equals("del") && !command.equals("diff")) {
                    System.out.println("Invalid input!");
                    System.out.println("Please enter an order as format: <operation key value/time> with 1 space between each argument, e.g. put A b, get B 2, del A b, diff A 1 5.");
                    System.out.println("If you want to quit, just enter quit.");
                    continue;
                }

                // System.out.println("Command is: " + order + ", and results are: "); // too many info printed

                dos.writeUTF(order); // send order to server

                // receive result from server
                result =  (Server.SerialList) ois.readObject(); // receive result from server
                List<String> friendList = result.getList(); // get friends list, attention it may be empty
                System.out.print(result.signal); // print result of operation, success or not
                if (result.hasList) {
                    // if this operation requires output friends list
                    printFriendList(friendList);
                }
                System.out.println();
            }

            // close stream and socket
            sc.close();
            dos.close();
            socket.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * method to print friends lit
     *
     * @param list: friends list to be printed
     */
    private static void printFriendList(List<String> list) {
        System.out.print("{");
        // if list is an empty, we should print an empty list
        if (list != null && !list.isEmpty()) {
            System.out.print(list.get(0));
            for (int i = 1; i < list.size(); i++) {
                System.out.print(", " + list.get(i));
            }
        }
        System.out.print("}");
    }

    public static void main(String[] args) {
        int serverPort = 5000; // set server port
        Client client = new Client(serverPort); //define a new client
        client.start(); // start client
    }
}
