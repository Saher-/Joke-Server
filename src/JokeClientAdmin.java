/*--------------------------------------------------------

1. Saher Yakoub - 1/25/14

2. Java version used:

build 1.8.0-b132

3. Precise command-line compilation examples / instructions:

> javac JokeClientAdmin.java


4. Precise examples / instructions to run this program:

In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For exmaple, if the server is running at
140.192.1.22 then you would type (or localhost):

> java JokeClient 140.192.1.22
> java JokeClientAdmin 140.192.1.22

5. List of files needed for running the program.

 a. JokeServer.java
 b. JokeClient.java
 c. JokeClientAdmin.java

5. Notes:

-- each time the admin connects a message will appear to press a key to change the state or q to exit
-- if the key q is pressed the connection will be closed
-- if another key is pressed, a list with the available states will show up along with numbers choice
-- the user press the required number to change the state
-- repeat
----------------------------------------------------------*/
import java.io.*;
import java.net.*;

public class JokeClientAdmin {

	public static void main(String[] args) {
		String serverName;
		Socket sock;
		
		// get the server name from main args OR to be localhost
		if (args.length < 1)
			serverName = "localhost";
		else
			serverName = args[0];

		System.out.println("Saher Inet Client, V01.\n");
		System.out.println("Using Server: " + serverName + ", port: 1520");

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); 		// to read the user input
		try {
			String ans;
			do {
				System.out.println("Press any key to change the Joke server mode, 'q' to exit : ");

				// get the user input & store it
				ans = in.readLine();
				
				// Create a socket connection with the specified port number & server name
				
				if (ans.indexOf("q") < 0) {			// if q exit if not proceed to server
					sock = new Socket(serverName, 1520);
					dosmthing(sock);
				}
					
			} while (ans.indexOf("q") < 0);
			System.out.println("Exitting upon user request . . . Bye!");
		} catch (IOException x) {
			x.printStackTrace();
		}
	}

	// helper function for changing the server state options
	static void dosmthing(Socket sock) {
		// I/O streams
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;

		try {
			// keeps streams that are going from the client to server
			toServer = new PrintStream(sock.getOutputStream());
			
			// keeps streams that are coming from the server to client
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			// to read the user input
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			System.out.println("Server's CURRENT state is - -" + fromServer.readLine());	// current state
			System.out.println("Change Mode by pressing a number: ");
			System.out.println("1: Joke Mode");
			System.out.println("2: Proverb Mode");
			System.out.println("3: Maintainance Mode");
			System.out.print("input > ");
			System.out.flush();

			int req = in.read();		// read the number entered to the new state
			req -= 48;			// a small hack for the Integer to be sent & received correctly
			toServer.write(req);		// send the changing state request
			sock.close();			// close the socket connection with the server
		} catch (IOException x) {
			System.out.println("Socket error");
			x.printStackTrace();
		}
	}
}