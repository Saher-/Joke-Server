/*--------------------------------------------------------

1. Saher Yakoub - 1/25/14

2. Java version used:

build 1.8.0-b132

3. Precise command-line compilation examples / instructions:

> javac JokeClient.java


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

-- each time a client connects, he/she will be asked to enter the user name to be used later
-- press enter to get the next available joke/proverb according to the server state
-- repeat
-- press q to exit 
-- Display the disconnecting messages
----------------------------------------------------------*/
import java.io.*;
import java.net.*;
import java.util.*;

public class JokeClient {

	public static void main(String[] args) {
		BufferedReader in;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		Socket sock;

		// get the server name from main args OR to be localhost
		String serverName;
		String usrName;

		if (args.length < 1)
			serverName = "localhost";
		else
			serverName = args[0];

		System.out.println("Saher Joke Client, V01.\n");
		System.out.println("Using Server: " + serverName + ", port: 1515");

		in = new BufferedReader(new InputStreamReader(System.in));		// to read the user input
		try {
			String msg;
			System.out.println("Enter the USER name: ");		// asking for the user name to send it once in a session
			System.out.flush();

			// get the user input & store it
			usrName = in.readLine();
			if (usrName == null) 			// if no user input is enter
				usrName = "TempUser";
			
			// Create a socket connection with the specified port number & server name
			sock = new Socket(serverName, 1515);

			// I/O streams
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			
			toServer.println(usrName); 		// send the server the current user name
			do {
				System.out.println("Hit Enter to get something, or 'q' to exit ...");
				System.out.flush();

				// get the user input
				msg = in.readLine();
				if (msg.indexOf("q") < 0) {		// if q exit if not proceed to server
					toServer.println(msg);
					textFromServer = fromServer.readLine();		// read response from the server (JOKE/PROVERB)
					System.out.println(textFromServer);
				}
			} while (msg.indexOf("q") < 0);

			// if the user enters 'q' to exit
			System.out.println("Exitting upon user request . . . Bye!");
			toServer.println("quit");		// send the server a flag to exit
			System.out.println(fromServer.readLine());		// read the goodbye msg from the server
			sock.close();				// close the socket connection with the server
		} catch (IOException x) {
			x.printStackTrace();
		}
	}
}