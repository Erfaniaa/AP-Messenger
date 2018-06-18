package sample;
import java.nio.file.Files;
import java.util.*;
import java.io.*;
import java.net.*;

class User extends Thread implements Serializable
{
	protected String firstName;
	protected String lastName;
	protected String username;
	protected String password;
	protected boolean isConnected;
	User(String _username, String _password)
	{
		username = _username;
		password = _password;
		isConnected = false;
	}
	User(String _username, String _password, String _firstName, String _lastName, boolean _isConnected)
	{
		this(_username, _password);
		firstName = _firstName;
		lastName = _lastName;
		isConnected = _isConnected;
	}
	User(User _user)
	{
		this(_user.username, _user.password, _user.firstName, _user.lastName, _user.isConnected);
	}
	public String toString()
	{
		return firstName + " " + lastName;
	}
	public String getUsername()
	{
		return username;
	}
	public String getPassword()
	{
		return password;
	}
}

class Client extends User
{
	public Controller controller;
	public Controller2 controller2;
	private String serverIp;
	private int serverPort;
	private Socket socket;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	public Client(String _serverIp, int _serverPort, String _username, String _password)
	{
		super(_username, _password);
		try
		{
			socket = new Socket(_serverIp, _serverPort);
			output = new ObjectOutputStream(socket.getOutputStream());
			input = new ObjectInputStream(socket.getInputStream());
			serverIp = _serverIp;
			serverPort = _serverPort;
			isConnected = true;
//			groupActiveChat = false;
//			activeChatId = null;
			System.out.println("Connected!");
//			sendLoginQuery();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public Client(String _serverIp, int _serverPort, String _username, String _password, String _firstName, String _lastName)
	{
		this(_serverIp, _serverPort, _username, _password);
		firstName = _firstName;
		lastName = _lastName;
	}
	public void run()
	{
		while (true)
		{
			try
			{
				Query query = (Query)input.readObject();
				if (query instanceof UserProfile)
				{
					UserProfile userProfile = (UserProfile)query;
					System.out.println("Query is instanceof UserProfile");
					controller2.receiveUserProfile(userProfile);
				}
				else if (query instanceof TextMessage)
				{
					TextMessage textMessage = (TextMessage)query;
					controller2.receiveMessage(textMessage);
					System.out.println(textMessage.toString());
				}
				else if (query instanceof FileMessage)
				{
					FileMessage fileMessage = (FileMessage)query;
					System.out.println("FileMessage received.");
					File file = new File("messenger_downloads/" + fileMessage.getFileName());
					file.createNewFile();
					Files.write(file.toPath(), fileMessage.getContent());
					controller2.receiveMessage(fileMessage);
					System.out.println(fileMessage.toString());
				}
				else if (query instanceof SuccessfulLogin)
				{
					System.out.println("Successful login!");
					isConnected = true;
					controller.showMainForm();
//					sendProfile();
				}
				else
					System.out.println("not");
//				start();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	public void sendGetChatQuery(boolean isGroup, String dstId)
	{
		try
		{
			User user = new User((User)this);
			output.writeObject(new GetChatQuery(user, isGroup, dstId));
			System.out.println("GetChat query sent.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public void sendLoginQuery()
	{
		try
		{
			User user = new User((User)this);
			output.writeObject(new LoginQuery(user));
			System.out.println("Login query sent.");
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	public void sendRegisterQuery()
	{
		try
		{
			User user = new User((User)this);
			output.writeObject(new RegisterQuery(user));
			System.out.println("Register query sent.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public void sendProfile()
	{
		try
		{
			User user = new User((User)this);
			System.out.println(user.isConnected);
			output.writeObject(new UserProfile(user));
			System.out.println("User profile sent.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public void sendTextMessage(boolean isGroup, String dstId, String text)
	{
		try
		{
			User user = new User((User)this);
			output.writeObject(new TextMessage(user, isGroup, dstId, text, false));
		}
		catch (Exception e) {}	
	}
	public void sendFileMessage(boolean isGroup, String dstId, File file)
	{
		try
		{
			User user = new User((User)this);
			output.writeObject(new FileMessage(user, isGroup, dstId, file));
		}
		catch (Exception e) {}	
	}

	public void logOut()
	{
		isConnected = false;
		sendProfile();
	}
}

public class ClientRunner
{
	public static void main(String[] args)
	{
		Scanner input = new Scanner(System.in);
		Client client = new Client("127.0.0.1", 1235, input.next(), input.next());
		client.start();
		while (input.hasNext())
		{
			client.sendTextMessage(false, input.next(), input.next());
		}
	}
}