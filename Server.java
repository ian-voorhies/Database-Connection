import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
