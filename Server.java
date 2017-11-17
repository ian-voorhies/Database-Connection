import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
public class Server{
	private ArrayList<Connection>connections;
	private static final int _MAX_CONNECTIONS=100;
	public static void main(String argv[]){new Server();}
	public Server(){
		connections=new ArrayList<Connection>();
		Semaphore mutex=new Semaphore(1);
		try{
			ServerSocket serverSocket=new ServerSocket(5555); 
			int connectionID=1;
			boolean running=true;
			while(running){
				Socket socket=serverSocket.accept();
				boolean allow=true;
				if(connections.size()>=_MAX_CONNECTIONS)allow=false;
				connections.add(new Connection(this,socket,connectionID,allow,mutex));
				startConnection(connectionID);
				connectionID++;
			}
			serverSocket.close();
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
	private void startConnection(int connectionID){
		System.out.println("Attempting to launch connection "+connectionID);
		for(int i=0;i<connections.size();i++){
			if(connections.get(i).getConnectionID()==connectionID){
				connections.get(i).start();
				return;
			}
		}
	}
	protected void removeConnection(Connection connection){connections.remove(connection);}
}
class Connection{
	private Server server;
	private int connectionID;
	private Thread connectionThread;
	public Connection(Server server,Socket socket,int connectionID,boolean allow,Semaphore mutex){
		this.server=server;
		this.connectionID=connectionID;
		connectionThread=new ConnectionThread(this,server,socket,connectionID,allow,mutex);
	}
	public int getConnectionID(){return connectionID;}
	public void start(){connectionThread.start();}
	public void close(){
		try{
			System.out.println("Connection "+connectionID+" closed");
			server.removeConnection(this);
			connectionThread.join();
			connectionThread=null;
		}
		catch(Exception e){System.out.println("Error closing connection "+connectionID);}
	}
}
class ConnectionThread extends Thread{
	private static final String database="";	//modify
	private Connection connection;
	private Socket socket;
	private int connectionID,clientID;
	private boolean allow;
	private Semaphore mutex;
	public ConnectionThread(Connection connection,Server server,Socket socket,int connectionID,boolean allow,Semaphore mutex){
		this.connection=connection;
		this.socket=socket;
		this.connectionID=connectionID;
		this.allow=allow;
		this.mutex=mutex;
	}
	public void run(){
		try{
			PrintStream output=new PrintStream(socket.getOutputStream());
			Scanner input=new Scanner(socket.getInputStream());
			System.out.println("Connection "+connectionID+" launched");
			clientID=Integer.parseInt(input.nextLine());
			if(allow){
				output.println(""+ReplyCodes._SUCCESS);
				System.out.println("Client "+clientID+" is connected using connection "+connectionID);
			}
			else{
				output.println(""+ReplyCodes._FULL);
				System.out.println("Client "+clientID+" was rejected on connection "+connectionID);
				socket.close();
				connection.close();
			}
			while(input.hasNextLine()){ 
				int function=Integer.parseInt(input.nextLine());
				switch(function){
				case ReplyCodes._NEW:
					try{
						String key=input.nextLine();
						String value=input.nextLine();
						mutex.acquire();
						BufferedWriter file=new BufferedWriter(new FileWriter(database,true));
						file.append(key+"["+value+"]\n");
						file.close();
						mutex.release();
						output.println(""+ReplyCodes._SUCCESS);
					}
					catch(Exception e){
						mutex.release();
						output.println(""+ReplyCodes._FAIL);
					}
					break;
				case ReplyCodes._SEARCH:
					try{
						mutex.acquire();
						String search=input.nextLine().toLowerCase();
						Scanner file=new Scanner(new File(database));
						ArrayList<String>lines=new ArrayList<String>();
						while(file.hasNextLine())lines.add(file.nextLine());
						String result="";
						int results=0;
						for(int i=0;i<lines.size();i++)
							if(lines.get(i).contains(search)){
								result+=lines.get(i)+"#";
								results++;
							}
						file.close();
						mutex.release();
						output.println(""+ReplyCodes._SUCCESS);
						output.println(""+results);
						output.println(result);
					}
					catch(Exception e){
						mutex.release();
						output.println(""+ReplyCodes._FAIL);
					}
					break;
				case ReplyCodes._EXIT:
					System.out.println("Client "+clientID+" disconnected");
					socket.close();
					connection.close();
					break;
				}
			}
			input.close();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
