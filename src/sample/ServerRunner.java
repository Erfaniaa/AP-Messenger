package sample;
import java.util.*;
import java.io.*;
import java.net.*;

class ClientHandler extends Thread
{
	private boolean isLoggedin;
	private boolean isListening;
	private Socket socket;
	private User user;
	private Server server;
	public ObjectInputStream input;
	public ObjectOutputStream output;
	public ClientHandler(Server _server, Socket _socket)
	{
		try
		{
			isLoggedin = false;
			socket = _socket;
			output = new ObjectOutputStream(socket.getOutputStream());
			input = new ObjectInputStream(socket.getInputStream());
			server = _server;
		}
		catch (Exception e) {}
	}
	public void run()
	{
		while (true)
		{
			try
			{
				Query query = (Query)input.readObject();
				if (query instanceof LoginQuery)
				{
					LoginQuery loginQuery = (LoginQuery)query;
					if (isLoginValid(loginQuery))
					{
						isLoggedin = true;
						output.writeObject(new SuccessfulLogin());
						System.out.println("Login!");
					}
					else
						System.out.println("Failed!");
				}
				if (query instanceof UserProfile)
				{
					UserProfile userProfile = (UserProfile)query;
					user = userProfile.src;
					System.out.println("UserProfile received.");
					if (user.isConnected)
					{
						System.out.println("isConnected is true");
						server.onlineClients.put(user.getUsername(), this);
						isLoggedin = true;
						sendRegisteredUsers();
						sendUserProfileToAll(userProfile);
//						sendArchivedMessages();
					}
					else
					{
						server.onlineClients.remove(user.getUsername());
						isLoggedin = false;
						sendUserProfileToAll(userProfile);
						interrupt();
					}
				}
				if (query instanceof Message)
				{
					Message message = (Message)query;
					System.out.println(message.toString());
					if (!message.isGroup)
					{
						writeMessageToFile(message);
						if (server.onlineClients.keySet().contains(message.dstId))
						{
							ClientHandler dst = server.onlineClients.get(message.dstId);
							dst.output.writeObject(message);
						}
					}
					else
					{

					}
				}
				if (query instanceof GetChatQuery)
				{
					GetChatQuery getChatQuery = (GetChatQuery)query;
					sendArchivedMessages(getChatQuery.dstId);
				}
				if (query instanceof GetRegisteredUsersQuery)
				{
//					GetRegisteredUsersQuery getRegisteredUsersQuery = (GetRegisteredUsersQuery) query;
//					sendRegisteredUsers();
				}
				if (query instanceof RegisterQuery)
				{
					System.out.println("RegisterQuery received.");
					User user = ((RegisterQuery)query).src;
					if (!search(query))
						registerUser(user);
//					GetRegisteredUsersQuery getRegisteredUsersQuery = (GetRegisteredUsersQuery) query;
//					sendRegisteredUsers();
				}
			}
			catch (Exception e) {}
		}
	}

	private void sendUserProfileToAll(UserProfile userProfile)
	{
		for (String dstId: server.onlineClients.keySet())
		{
			ClientHandler dst = server.onlineClients.get(dstId);
			try
			{
				dst.output.writeObject(userProfile);
			}
			catch (Exception e) {}
		}
	}
	private void sendRegisteredUsers()
	{
		System.out.println("sendRegisteredUsers: ");
		try
		{
			Scanner fileScanner = new Scanner(new File("files/passwords.txt"));
			while (fileScanner.hasNext())
			{
				String u = fileScanner.next();
				String p = fileScanner.next();
				System.out.println(u + " " + p);
				User tmp = new User(u, p);
				if (server.onlineClients.keySet().contains(u))
					tmp.isConnected = true;
				if (!user.getUsername().equals(u))
					output.writeObject(new UserProfile(tmp));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private boolean isLoginValid(LoginQuery loginQuery)
	{
		try
		{
			Scanner fileScanner = new Scanner(new File("files/passwords.txt"));
			while (fileScanner.hasNext())
			{
				String u = fileScanner.next();
				String p = fileScanner.next();
				if (loginQuery.src.getUsername().equals(u) && loginQuery.src.getPassword().equals(p))
					return true;
			}
		}
		catch (Exception e) {}		
		return false;
	}
	private void writeMessageToFile(Message message)
	{
		try
		{
			String s1 = message.src.getUsername();
			String s2 = message.dstId;
			String fileName = s1 + "_" + s2 + ".dat";
			if (s1.compareTo(s2) > 0)
				fileName = s2 + "_" + s1 + ".dat";
			ObjectOutputStream objectWriter = null;
			File file = new File("files/" + fileName);
			if (!file.exists())
				objectWriter = new ObjectOutputStream(new FileOutputStream("files/" + fileName));
			else
				objectWriter = new AppendingObjectOutputStream(new FileOutputStream("files/" + fileName, true));
			objectWriter.writeObject(message);
			objectWriter.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private void sendArchivedMessages(String id)
	{
		try
		{
			File dir = new File("files/");
			for (File file: dir.listFiles())
			{
				if (file.isFile())
				{
					String fileName = file.getName().substring(0, file.getName().length() - 4);
					if (fileName.indexOf("_") == -1)
						continue;
					String userName1 = fileName.substring(0, fileName.indexOf("_"));
					String userName2 = fileName.substring(fileName.indexOf("_") + 1, fileName.length());
					System.out.println(userName1 + " " + userName2);
					if ((userName1.equals(user.getUsername()) || userName2.equals(user.getUsername()))
							&& (userName1.equals(id) || userName2.equals(id)))
					{
						FileInputStream fileInputStream = null;
						ObjectInputStream objectReader = null;
						try
						{
							fileInputStream = new FileInputStream(file);
							objectReader = new ObjectInputStream(fileInputStream);
							while (true)
							{
								Message message = (Message)objectReader.readObject();
								message.seen = true;
								System.out.println(message.toString());
								output.writeObject(message);
								System.out.println(message.toString());
							}
						} catch (Exception e) {e.printStackTrace();}
						output.flush();
						if (objectReader != null)
							objectReader.close();
						return;
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private void sendArchivedMessages()
	{
		try
		{
			File dir = new File("files/");
			for (File file: dir.listFiles())
			{
				if (file.isFile())
				{
					String fileName = file.getName().substring(0, file.getName().length() - 4);
					if (fileName.indexOf("_") == -1)
						continue;
					String userName1 = fileName.substring(0, fileName.indexOf("_")); 
					String userName2 = fileName.substring(fileName.indexOf("_") + 1, fileName.length());
					System.out.println(userName1 + " " + userName2);
					if (userName1.equals(user.getUsername()) || userName2.equals(user.getUsername()))
					{
						FileInputStream fileInputStream = null;
						ObjectInputStream objectReader = null;
						try
						{
							fileInputStream = new FileInputStream(file);
							objectReader = new ObjectInputStream(fileInputStream);
							while (true)
							{
								Message message = (Message)objectReader.readObject();
								message.seen = true;
								System.out.println(message.toString());
								output.writeObject(message);
							}
						} catch (Exception e) {e.printStackTrace();}
						output.flush();
						if (objectReader != null)
							objectReader.close();
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private boolean search(Query loginQuery)
	{
		try
		{
			Scanner fileScanner = new Scanner(new File("files/passwords.txt"));
			while (fileScanner.hasNext())
			{
				String u = fileScanner.next();
				String p = fileScanner.next();
//				System.out.println(u + "*****" + p + " - " + loginQuery.src.getUsername());
				if (loginQuery.src.getUsername().equals(u))
					return true;
			}
		}
		catch (Exception e) {}
		return false;
	}
	private void registerUser(User user)
	{
		try
		{
			System.out.println(user.getUsername() + " *** " + user.getPassword());
			PrintWriter printWriter = new PrintWriter(new FileWriter("files/passwords.txt", true));
			printWriter.println(user.getUsername() + " " + user.getPassword());
			printWriter.flush();
			printWriter.close();
		} catch (Exception e) {}
	}
	public User getUser()
	{
		return user;
	}
	public boolean isListening()
	{
		return isListening;
	}
	public boolean isLoggedin()
	{
		return isLoggedin;
	}
}

class Server extends Thread
{
	private ArrayList <ClientHandler> clients;
	private ServerSocket serverSocket;
	private int port;
	public HashMap <String, ClientHandler> onlineClients;
	public Server(int _port)
	{
		try
		{
			serverSocket = new ServerSocket(_port);
			serverSocket.setSoTimeout(10000);
			port = _port;
			clients = new ArrayList <ClientHandler>();
			onlineClients = new HashMap <String, ClientHandler>();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public void printRegisteredUsers()
	{
		try
		{
			Scanner fileScanner = new Scanner(new File("files/passwords.txt"));
			while (fileScanner.hasNext())
			{
				String u = fileScanner.next();
				String p = fileScanner.next();
				System.out.println("User: " + u + " - Pass: " + p);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public void printOnlineUsers()
	{
		for (String s: onlineClients.keySet())
			System.out.println(((ClientHandler)onlineClients.get(s)).getUser().getUsername());
	}
	public void run()
	{
		while (true)
		{
			try
			{
				Socket socket = serverSocket.accept();
				if (socket != null)
				{
					clients.add(new ClientHandler(this, socket));
					clients.get(clients.size() - 1).start();
				}
			}
			catch (Exception e) {}
		}
	}
}

public class ServerRunner
{
	public static void main(String[] args) throws Exception
	{
		Server server = new Server(1234);
		server.start();
		Scanner scanner = new Scanner(System.in);
		while (true)
		{
			String s = scanner.nextLine();
			if (s.equals("exit"))
			{
				server.interrupt();
				return;
			}
			if (s.equals("show all clients"))
				server.printRegisteredUsers();
			if (s.equals("show online clients"))
				server.printOnlineUsers();
		}
	}
}