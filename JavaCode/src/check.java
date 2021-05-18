import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

class check
{
    public static void main(String [] args)
    {
      System.out.println("its working");  
      try 
        {
          File myFile = new File(args[0]);
          Scanner myReader = new Scanner(myFile);
          while (myReader.hasNextLine()) 
          {
            String data = myReader.nextLine();
            System.out.println(data);
          }
          myReader.close();
        } 
        catch (FileNotFoundException e) 
        {
          System.out.println("An error occurred.");
          e.printStackTrace();
        }
    }
}