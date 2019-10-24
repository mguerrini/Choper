/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper;

import choper.domain.*;
import choper.platform.events.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.smartcardio.*;

/**
 *
 * @author max22
 */
public class Choper
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        MoneyReaderMachine reader = MoneyReaderMachineProvider.Instance.Get();
        
        String port = reader.FindPort();
        //ShowSerialPorts();
    }

    private static void ShowSerialPorts()
    {
        //File folder = new File("/dev/serial");
        try (Stream<Path> walk = Files.walk(Paths.get("/dev/serial/by-id"))) 
        {
            List<String> result = walk.map(x -> x.toString()).collect(Collectors.toList());
            result.forEach(System.out::println);

	} catch (IOException e) {
		e.printStackTrace();
	}

/*        
        String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i < portNames.length; i++){
            System.out.println(portNames[i]);
        }
*/        
    }

    
    private static void TestSmartCard()
    {
                // TODO code application logic here
        SmartCardReader reader = new SmartCardReader();
        reader.CardInserted.Subscribe((a, e) -> OnCardInserted(a, e));
        reader.CardRemoved.Subscribe(Choper::OnCardRemoved);

        reader.Start();

        Scanner s = new Scanner(System.in);
        String str = s.nextLine();
    }
    
    private static void OnCardInserted(Object sender, EventArgs args)
    {
        System.out.println("Card Inserted...");
        SmartCardReader reader = (SmartCardReader) sender;
        Card c = reader.GetActiveCard();
        
        
        System.out.println("Card Inserted...ATR = " + c.getATR());
        try
        {
            reader.SetBalance(58);
            float val = reader.GetBalance();
            System.out.println("Card Inserted...Valor leido = " + val);
        }
        catch (Exception ex)
        {
            Logger.getLogger(Choper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void OnCardRemoved(Object sender, EventArgs args)
    {
        System.out.println("Card Removed...");
    }
}
