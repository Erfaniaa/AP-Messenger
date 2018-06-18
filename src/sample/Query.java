package sample;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;

abstract class Query implements Serializable
{
	public User src;
	public Date date;
}

// abstract class GroupMessage extends Query
// {
// 	public String dstId;
// }

abstract class Message extends Query
{
	public String dstId;
	public boolean isGroup;
	public boolean seen;
}

class TextMessage extends Message
{
	public String text;
	TextMessage(User _src, boolean _isGroup, String _dstId, String _text, boolean _seen)
	{
		date = new Date();
		text = _text;
		dstId = _dstId;
		src = _src;
		isGroup = _isGroup;
		seen = _seen;
	}
	public String toString()
	{
		return src.getUsername() + ": " + text + " (" + date.toString() + ")";
	}
}

class ImageMessage extends Message
{

}

class AudioMessage extends Message
{

}

class FileMessage extends Message
{
	private byte[] content;
	private String fileName;
	FileMessage(User _src, boolean _isGroup, String _dstId, File _file)
	{
		date = new Date();
		src = _src;
		dstId = _dstId;
//		file = _file;
		isGroup = _isGroup;
		try
		{
			content = Files.readAllBytes(_file.toPath());
			fileName = _file.getName();
		}
		catch (Exception e) {}
	}
	public byte[] getContent()
	{
		return content;
	}
	public String getFileName()
	{
		return fileName;
	}
	public String toString()
	{
		return fileName + " file transferred.";
	}
}

class LoginQuery extends Query
{
	LoginQuery(User _src)
	{
		date = new Date();
		src = _src;
	}
}

class RegisterQuery extends Query
{
	RegisterQuery(User _src)
	{
		date = new Date();
		src = _src;
	}
}

class UserProfile extends Query
{
	UserProfile(User _src)
	{
		date = new Date();
		src = _src;
	}
}

class SuccessfulLogin extends Query
{

}

//class GroupProfile extends Query
//{
//	Group group;
//	GroupProfile(Group _group)
//	{
//		date = new Date();
//		group = _group;
//	}
//}

class GetChatQuery extends Query
{
	public String dstId;
	public boolean isGroup;
	GetChatQuery(User _src, boolean _isGroup, String _dstId)
	{
		date = new Date();
		dstId = _dstId;
		src = _src;
		isGroup = _isGroup;
	}
}

class GetRegisteredUsersQuery extends Query
{
	GetRegisteredUsersQuery(User _src)
	{
		date = new Date();
		src = _src;
	}
}
