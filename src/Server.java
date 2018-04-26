import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class Server {

    private static int serverPort; // server port number
    private static Service service; // APIs services

    /**
     * Server constructor
     */
    public Server() {
        this.serverPort = 5000;	//set server port number
        this.service = new Service(); // initialize a Service class to do all operations

    }

    /**
     * start method for server
     */
    private static void start() {
        try {
            // Runtime.getRuntime().exec(new String[] { "bash", "-c", "rm -f /path/*.txt" }).waitFor(); //first clear all other useless file in the directory where we will output files

            ServerSocket sSocket = new ServerSocket(serverPort); //give server port number 5000
            System.out.println("Server starting at: " + new Date()); //print to indicate we are ready to start
            Socket client = sSocket.accept(); // accept client socket connection

            DataInputStream dis = new DataInputStream(client.getInputStream()); // a dataInputStream to receive input command from client
            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream()); // a objectOutputStream to output result to client

            System.out.println(dis.readUTF() + ", server listening on port " + serverPort); // print an info from client, presenting a successful connection with client

            // initialize some useful output string :)
            String warning = "Invalid input! ";
            String success = "Success ";
            String seeya = "Server received and quitting. Thanks for using! ";
            String invalidTime = "Invalid time, please enter an integer or a smaller one! ";
            String noFriend = "This user does not have any friends yet or he/she deleted before :(. ";
            String invalidUser = "Invalid user ";

            // some codes used to mark result status
            final int NOUSER = -1; // marks that there's is no such user
            final int HASDELETED = 0; // the user has been deleted
            final int SUCCESS = 1; // operate successfully
            final int NOFRIEND = 2; // no such searching friend
            final int INVALIDTIME = 3; // invalid time error

            // keep listening client
            while (true) {
                String order = dis.readUTF(); // receive command from client
                SerialList result; // initialize a result type to send back to client
                System.out.println("command is: " + order); // output command on terminal for double checking

                // quit command
                if (order.equals("quit")) {
                    System.out.println("Server quitting..."); // print on server to indicate quiting
                    result = new SerialList(seeya, false); // still transport info to client
                    oos.writeObject(result); // send to client
                    break; // stop listening process
                }

                // processing
                String[] input = order.split(" "); // split command
                String operation = input[0]; // extract operation

                // choose different scenarios by different operations
                switch (operation) {
                    case "put":
                        // command <put user friend>
                        if (input.length != 3) {
                            // edge case: invalid input
                            result = new SerialList(warning, false);
                        } else {
                            service.put(input[1], input[2]); // do put operation
                            result = new SerialList(success, false);
                        }
                        break;
                    case "get":
                        if (input.length == 2) {
                            // command: <get user>
                            ResultType resultType = service.get(input[1]); // receive results
                            if (resultType.code == NOUSER) {
                                // no friends found, but we still set true flag since we wanna output a empty list
                                result = new SerialList(invalidUser, false);
                            } else if (resultType.code == NOFRIEND) {
                                result = new SerialList(noFriend, true);
                            } else {
                                // found friends 
                                result = new SerialList(success, true);
                            }
                            result.setList(resultType.getResult()); // deserialize friends list
                        } else if (input.length == 3) {
                            // command: <get user time>
                            String time = input[2];
                            // make sure input search time is an integer, I assume the times of operation is within integer range
                            if (!isTimeValid(time)) {
                                // it's not an integer
                                result = new SerialList(invalidTime, false);
                                break;
                            }
                            int searchingTime =  Integer.parseInt(time); // convert to int type
                            ResultType resultType = service.get(input[1], searchingTime); // receive the final result
                            if (resultType.code == INVALIDTIME) {
                                // invalid time
                                result = new SerialList(invalidTime, false);
                            } else if (resultType.code == NOUSER) {
                                // no user
                                result = new SerialList(invalidUser, false);
                            } else {
                                // valid time
                                result = new SerialList(success, true); // get result
                                result.setList(resultType.getResult()); // serialize list
                            }
                        } else {
                            // invalid input
                            result = new SerialList(warning, false);
                        }
                        break;
                    case "del":
                        if (input.length == 2) {
                            // command: <delete, user>
                            int code = service.delete((input[1])); // flag
                            if (code == SUCCESS) {
                                // we do deletion
                                result = new SerialList(success, false);
                            } else if (code == NOUSER) {
                                // no such user
                                result = new SerialList(invalidUser, false);
                            } else {
                                // the user's friends are already deleted or no friends yet
                                result = new SerialList(noFriend, false);
                            }
                        } else if (input.length == 3) {
                            // command: <delete, user, friend>
                            int code = service.delete(input[1], input[2]); // flag
                            if (code == SUCCESS) {
                                // complete deletion
                                result = new SerialList(success, false);
                            } else if (code == HASDELETED) {
                                // this friend has been deleted
                                result = new SerialList("This user does not have this friends yet!", false);
                            } else {
                                // no such user
                                result = new SerialList(invalidUser, false);
                            }
                        } else {
                            // invalid input
                            result = new SerialList(warning, false);
                        }
                        break;
                    case "diff":
                        // command: <diff, user, time1, time2>
                        // edge case: invalid input
                        if (input.length != 4) {
                            result = new SerialList(warning, false);
                            break;
                        }
                        String time1 = input[2];
                        String time2 = input[3];
                        if (!isTimeValid(time1) || !isTimeValid(time2)) {
                            // invalid input time
                            result = new SerialList(invalidTime, false);
                        } else if (Integer.parseInt(time1) > Integer.parseInt(time2)) {
                            // reversed comparative value of time1 and time2
                            result = new SerialList(warning + "Time1 should <= Time2", false);
                        } else {
                            ResultType resultType = service.diff(input[1], Integer.parseInt(input[2]), Integer.parseInt(input[3]));
                            if (resultType.code == INVALIDTIME) {
                                // invalid time
                                result = new SerialList(invalidTime, false);
                            } else if (resultType.code == NOUSER) {
                                // no such user
                                result = new SerialList(invalidUser, false);
                            } else {
                                // success
                                result = new SerialList(success, true);
                                result.setList(resultType.getResult());
                            }

                        }
                        break;
                    default:
                        // other invalid input
                        result = new SerialList(warning, false);
                        break;
                }

                // output final result to client
                oos.writeObject(result);
            }

            // close all stream and socket
            oos.close();
            dis.close();
            client.close();
            sSocket.close();

        } catch (IOException exception) {
            System.out.println("Error: " + exception);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server(); // initialize a server
        server.start(); // start this server
    }

    /**
     * method to check if input time is a valid integer
     *
     * @param s:input time with string format
     * @return true or false
     */
    private static boolean isTimeValid(String s) {
        String patterStr = "\\d+"; // regex
        Pattern pattern = Pattern.compile(patterStr);

        if (!pattern.matcher(s).matches()) {
            // it's non-numeric
            return false;
        }
        long time = Long.parseLong(s);
        return time >= 0 && time <= Integer.MAX_VALUE;
    }


    /**
     * a ResultType class to tranport info from server to client
     */
    public static class SerialList implements Serializable {
        // I define a result type to return results back to client.
        // It's basically a list, but for socket transportation, I implements Serializable to serialize it
        List<String> list; // a list to record friends names
        String signal; // record some extra output info to remind client, like success or warning
        boolean hasList; // mark if we should check and/or print friends name's list

        /**
         * Constructor
         *
         * @param signal: string of extra output info
         * @param hasList: marks of checking/printing friends name's list or not
         */
        SerialList(String signal, boolean hasList){
            this.signal = signal;
            this.hasList = hasList;
        }

        /**
         * Deserialize method when we get result
         *
         * @return a LinkedList<String> of user's friends
         */
        public List<String> getList() {
            return list;
        }

        /**
         * Serialize method
         *
         * @param list: the result list which is gonna be serialized
         */
        void setList(List<String> list) {
            this.list = list;
        }
    }
}