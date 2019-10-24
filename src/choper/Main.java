/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper;

import choper.domain.moneyReaders.MoneyReaderMachineProvider;
import choper.domain.moneyReaders.MoneyReaderMachine;
import choper.domain.smartCards.SmartCardReader;
import choper.domain.*;
import choper.domain.test.BillAndCardManager;
import choper.platform.events.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.smartcardio.*;

/**
 *
 * @author max22
 */
public class Main
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        //MoneyReaderMachine reader = MoneyReaderMachineProvider.Instance.Get();

        //String port = reader.FindPort();
        //ShowSerialPorts();
        
        BillAndCardTest();
    }

    private static void BillAndCardTest()
    {
        String input = "";
                BillAndCardManager mgr = new BillAndCardManager();
        mgr.Open();
        


            while (input.equalsIgnoreCase("q"))
            {

                try
                {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    String name = reader.readLine();
                    System.out.println("Command: " + name);

                    if (name.equalsIgnoreCase("q"))
                    {
                        System.out.println("Quit");
                    } else if (name.equalsIgnoreCase("a"))
                    {
                        if (this.AutoAcceptReject)
                        {
                            this.MustAccept = true;
                        } else
                        {
                            boolean b = this.BillReader.Accept();
                            this.WriteLine("Command Accepted - Executed: " + b);
                        }
                    } else if (name.equalsIgnoreCase("r"))
                    {
                        if (this.AutoAcceptReject)
                        {
                            this.MustAccept = false;
                        } else
                        {
                            boolean b = this.BillReader.Reject();
                            this.WriteLine("Command Reject - Executed: " + b);
                        }
                    } else if (name.equalsIgnoreCase("auto"))
                    {
                        this.AutoAcceptReject = !this.AutoAcceptReject;
                        this.WriteLine("Auto accept/reject bill: " + this.AutoAcceptReject);
                    }
                } catch (IOException ex)
                {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

    }


    private static void ShowSerialPorts()
    {
        //File folder = new File("/dev/serial");
        try (Stream<Path> walk = Files.walk(Paths.get("/dev/serial/by-id")))
        {
            List<String> result = walk.map(x -> x.toString()).collect(Collectors.toList());
            result.forEach(System.out::println);

        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private static void TestSmartCard()
    {
        // TODO code application logic here
        SmartCardReader reader = new SmartCardReader();
        reader.CardInserted.Subscribe((a, e) -> OnCardInserted(a, e));
        reader.CardRemoved.Subscribe(Main::OnCardRemoved);

        reader.Connect();

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
        } catch (Exception ex)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void OnCardRemoved(Object sender, EventArgs args)
    {
        System.out.println("Card Removed...");
    }
}
