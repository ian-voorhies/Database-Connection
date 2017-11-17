import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
public class Client{
	public static void main(String argv[]){  
		Socket socket=null;
		PrintStream output=null;
		Scanner serverInput=null;
		Scanner userInput=null;
		boolean running=false;
		int clientID=new Random().nextInt(9999999);
		int errorCode=0;
		try{
			System.out.println("\nAttempting to connect to database...");
			socket=new Socket("localhost",5555);	//modify
			output=new PrintStream(socket.getOutputStream()); 
			serverInput=new Scanner(socket.getInputStream()); 
			userInput=new Scanner(System.in);
			if(socket==null||output==null||serverInput==null||userInput==null){
				if(socket!=null){
					socket.close();
					output.flush();
				}
				if(output!=null)output.close();
				if(serverInput!=null)serverInput.close();
				if(userInput!=null)userInput.close();
				errorCode=1;
				throw new Exception();
			}
			output.println(""+clientID);
			int status=Integer.parseInt(serverInput.nextLine());
			if(status==ReplyCodes._SUCCESS){
				running=true;
				System.out.println("\nConnected to database\n");
			}
			else if(status==ReplyCodes._FULL){
				System.out.println("\nServer is full\nClosing...");
				System.exit(-1);
			}
			else{
				if(socket!=null){
					socket.close();
					output.flush();
				}
				if(output!=null)output.close();
				if(serverInput!=null)serverInput.close();
				if(userInput!=null)userInput.close();
				throw new Exception();
			}
		}
		catch(Exception e){
			System.out.println("Unable to connect to database.\nError code: "+errorCode+"\nClosing...");
			System.exit(-1);
		}
		try{
			while(running){
				System.out.println("Enter \"new\" to make a new entry, \"search\" to search the database, or \"exit\" to exit");
				String inputString=userInput.nextLine().toLowerCase();
				if(inputString.equals("new")){
					System.out.println("\nEnter key");
					String key=userInput.nextLine();
					System.out.println("Enter value");
					String value=userInput.nextLine();
					System.out.println("\nAttempting to add "+key+"["+value+"] into the database...");
					output.println(""+ReplyCodes._NEW);
					output.println(key);
					output.println(value);
					if(Integer.parseInt(serverInput.nextLine())==ReplyCodes._SUCCESS)System.out.println("Successfully added "+key+"["+value+"] to database\n");
					else System.out.println("Unable to add "+key+"["+value+"] to database\n");
				}
				else if(inputString.equals("search")){
					System.out.println("\nEnter search value: ");
					String search=userInput.nextLine();
					System.out.println("\nSearching database for "+search+"...");
					output.println(""+ReplyCodes._SEARCH);
					output.println(search);
					if(Integer.parseInt(serverInput.nextLine())==ReplyCodes._SUCCESS){
						int results=Integer.parseInt(serverInput.nextLine());
						String result=serverInput.nextLine();
						if(results>0){
							String[]resultArray=result.split("#");
							System.out.println("\nDoneResults ("+results+"):");
							for(int i=0;i<resultArray.length;i++)
							System.out.println(resultArray[i]);
							System.out.println();
						}
						else System.out.println("\nDone\nNo results matching key["+search+"] were found in the database\n");
					}
					else System.out.println("Search failed\n");
				}
				else if(inputString.equals("exit")){
					output.println(""+ReplyCodes._EXIT);
					running=false;
					socket.close();
					output.flush();
					output.close();
					serverInput.close();
					userInput.close();
					throw new Exception();
				}
			}
		}
		catch(Exception e){
			System.out.println("\nDisconnected from database\nClosing...\n");
			System.exit(-1);
		}
	}
}
