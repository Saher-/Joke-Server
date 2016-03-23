/*--------------------------------------------------------

1. Saher Yakoub - 1/25/14

2. Java version used:

build 1.8.0-b132

3. Precise command-line compilation examples / instructions:

> javac JokeServer.java


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

-- THIS SERVER DOESN'T DISCOUNNECT AFTER EACH REQUEST. ONLY DISCONTECTS IF THE CLIENT SEND A FLAG.
-- There is no limit for the number of jokes/proverbs (not hard coded)
-- each time a client connects a welcome message on the server screen appears
-- if the client disconnect or the server process is terminated all states are reseted
-- a message will appear if any connected client got disconnected.
-- each time an Admin client change the state a message will appear on the server screen 
----------------------------------------------------------*/
import java.io.*;
import java.net.*;
import java.util.*;

/******************************
 * JokeServer Worker Thread
 ******************************/
class Worker extends Thread {
	Socket sock;
	ServerStates _cur;
	String clientName = null;
	String msg;
	List<Integer> indexJ = new ArrayList<Integer>();
	List<Integer> indexP = new ArrayList<Integer>();
	int Jnum, Pnum;
	int sizeP, sizeJ = 0;

	Worker(Socket s, ServerStates _state) {
		sock = s;
		_cur = _state;
	} // Constructor to assign to local sock.

	public void run() {
		PrintStream out = null; 		// keeps streams that are going from the server to client
		BufferedReader in = null;		// keeps streams that are coming from the client to server
		try {
			out = new PrintStream(sock.getOutputStream());
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			// reads and stores the client name from the client end user
			clientName = in.readLine();
			System.out.println("Hey " + clientName + " - nice to see you :-)");		// welcome msg on the server screen
			
			// private functions to randomize indexes to access jokes & proverbs 
			randomizeJ();
			randomizeP();

			while (sock.isClosed() != true) {
				msg = in.readLine();			// detects the client pressing a key to get next joke or proverb
				
				//MODES BRANCHING
				if (msg.indexOf("quit") < 0) {
					// If server is in JOKE mode
					if (JokeServer.getServerState() == ServerStates.Joke) {
						if (Jnum < 5) {		//get the next joke
							getJoke(out, indexJ.get(Jnum));
							Jnum++;
						} else {			//re-randomize if done with previouse randomization
							indexJ.clear();
							randomizeJ();
							Jnum = 0;
							getJoke(out, indexJ.get(Jnum));
							Jnum++;
						}
					}
					// If server is in PROVERB mode
					else if (JokeServer.getServerState() == ServerStates.Proverb) {
						if (Pnum < 5) {		//get the next proverb
							getProverb(out, indexP.get(Pnum));
							Pnum++;
						} else {		//re-randomize if done with previouse randomization
							indexP.clear();
							randomizeP();
							Pnum = 0;
							getProverb(out, indexP.get(Pnum));
							Pnum++;
						}
					}
					// If server is in MAINTAINANCE mode
					else {
						out.println("SORRY " + clientName + " :( ... Server is in MAINTAINANCE mode !!!");
					}
				} else if (msg.equalsIgnoreCase("quit") == true) {		// A client is shutting down
					System.out.println(clientName + "'s machine is shutting down ...");
					out.println("-- Bye from Server side --");
					// close the socket connection with the current client
					sock.close();
				}

			}
		} catch (IOException ioe) {
			System.out.println("ioe error !!!");
		}
	}

	// private function to randomize the JOKES
	private void randomizeJ() {
		for (int i = 0; i < JokeServer._Jokes.size(); i++) {
			indexJ.add(i);
		}
		Collections.shuffle(indexJ);
	}

	// private function to randomize the PROVERBS
	private void randomizeP() {
		for (int i = 0; i < JokeServer._Proverbs.size(); i++) {
			indexP.add(i);
		}

		Collections.shuffle(indexP);
	}

	// HELPER function to read jokes
	void getJoke(PrintStream out, int num) throws IOException {
		String joke;
		joke = JokeServer._Jokes.get(num);
		out.println(joke + " <--- a JOKE to " + clientName); 	// sent to client side
	}

	// HELPER function to read proverbs
	void getProverb(PrintStream out, int num) throws IOException {
		String proverb;
		proverb = JokeServer._Proverbs.get(num);
		out.println(proverb + " <--- a PROVERB to " + clientName);	// sent to client side
	}
}

/************************************
 * JokeServer Admin Thread & Worker
 ************************************/
class AdminLooper implements Runnable {
	private final static int Qlen = 10;
	private final static int port = 1520;		// a new port number to connect with the admin
	private static Socket socket;

	public void run() {
		System.out.println("In the admin looper thread ...");

		try {
			ServerSocket servSock = new ServerSocket(port, Qlen);
			while (true) {
				socket = servSock.accept();			// wait for the next connection
				new AdminWorker(socket).start();	// start a worker thread for the current admin
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}

/******************************
 * Admin Worker Thread
 ******************************/
class AdminWorker extends Thread {
	private Socket sock;

	AdminWorker(Socket s) {
		sock = s;
	}	//constructor to initialise local socket 

	public void run() {
		BufferedReader in = null;		 // keeps streams that are coming from the client to server
		PrintStream out = null;			// keeps streams that are going from the server to client

		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());

			try {
				System.out.println("Admin is now connected ...");
				out.println(JokeServer._state);		// sending the Admin current server state

				int req = in.read();				// reading the required change option number
				JokeServer.ChangeState(req);		// CHANGING THE SERVER STATE through a function
			} catch (IOException io) {
				System.out.println("Server Read Error !!!");
			}
			sock.close();							// closing the socket connetion with the current admin
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}

/******************************
 * JokeServer enum state class
 ******************************/
enum ServerStates {
	Joke, Proverb, Maintainance;
}

/*****************************
 * JokeServer class & Main
 *****************************/
public class JokeServer {
	public static ServerStates _state = ServerStates.Joke;
	public static ArrayList<String> _Jokes;
	public static ArrayList<String> _Proverbs;

	public static void main(String[] args) throws IOException {
		int Qlen = 7; 		// number of clients to be handled together
		int port = 1515; 	// port number
		Socket sock;

		AdminLooper AL = new AdminLooper();
		Thread t = new Thread(AL);
		t.start();

		System.out.println("Server is preparing jokes & Proverbs");
		getJready();
		getPready();

		ServerSocket Ssock = new ServerSocket(port, Qlen);

		System.out.println("Saher Inet Server V01 starting up, listening at port 1515.\n");
		while (true) {
			sock = Ssock.accept(); 				// wait for next connection attempt
			new Worker(sock, _state).start(); 	// start a worker thread for the current client
		}
	}

	// reading JOKES from the disk and storing them into ArrayLists for use
	static void getJready() {
		int size = 7;		// gets the total number of the jokes to construct the Array, initially 7
		String ptr;
		try {
			BufferedReader brJ = new BufferedReader(new FileReader("Jokes.txt"));		// open a file reader stream
			try {
				ptr = brJ.readLine();
				size = Integer.parseInt(ptr);
				_Jokes = new ArrayList<String>(size);
				while ((ptr = brJ.readLine()) != null) {
					_Jokes.add(ptr);
				}
				brJ.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// reading PROVERBS from the disk and storing them into ArrayLists for use
	static void getPready() {
		int size = 7;					// gets the total number of the proverbs to construct the Array, initially 7
		String ptr;
		try {
			BufferedReader brP = new BufferedReader(new FileReader("Proverbs.txt"));	// open a file reader stream
			try {
				ptr = brP.readLine();
				size = Integer.parseInt(ptr);
				_Proverbs = new ArrayList<String>(size);
				while ((ptr = brP.readLine()) != null) {
					_Proverbs.add(ptr);
				}
				brP.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// helper function to change the current state of the server
	public static void ChangeState(int req) {
		System.out.println("- - -Changing server state- - -");
		if (req == 1)
			_state = ServerStates.Joke;
		else if (req == 2)
			_state = ServerStates.Proverb;
		else
			_state = ServerStates.Maintainance;
		System.out.println("Server State NOW is ... " + _state);
	}

	// helper function to get the current state of the server
	public static ServerStates getServerState() {
		return _state;
	}
}