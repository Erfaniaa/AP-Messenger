package sample;
import java.util.*;
import java.io.*;
import java.net.*;

class AppendingObjectOutputStream extends ObjectOutputStream {
  public AppendingObjectOutputStream(OutputStream out) throws Exception
  {
    super(out);
  }
  @Override
  protected void writeStreamHeader() throws IOException
  {
    reset();
  }
}

public class AppendingObject
{
  public static void main(String[] args)
  {
    
  }
}