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
import choper.domain.switches.ISwitch;
import choper.domain.switches.SwitchProvider;
import choper.domain.test.BillAndCardManager;
import choper.domain.test.FlowSensorTest;
import choper.domain.test.LcdTest;
import choper.platform.ConfigurationProvider;
import choper.platform.events.*;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import java.io.BufferedReader;
import java.io.Console;
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

        try
        {
            Logger.getGlobal().setLevel(Level.INFO);

            //MoneyReaderMachine reader = MoneyReaderMachineProvider.Instance.Get();
            //ShowSerialPocardrts();
            //BillAndCardTest();
            //LcdTest();
            //FlowSensorTest();
            //SwitchTest();
            StartChoper();
        }
        catch (Exception ex)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void StartChoper()
    {
        ChoperConsole console = new ChoperConsole();
        console.Start();
    }

    private static void SwitchTest()
    {
        try
        {
            ISwitch s = SwitchProvider.Instance.Get();
            s.Init();
            System.out.println("IsOpened:" + s.IsOpened());
            System.out.println("IsClosed:" + s.IsClosed());

            s.OpenContacts();
            System.out.println("IsOpened:" + s.IsOpened());
            System.out.println("IsClosed:" + s.IsClosed());
            Thread.sleep(3000);

            s.CloseContacts();
            System.out.println("IsOpened:" + s.IsOpened());
            System.out.println("IsClosed:" + s.IsClosed());
            Thread.sleep(3000);

            s.OpenContacts();
            System.out.println("IsOpened:" + s.IsOpened());
            System.out.println("IsClosed:" + s.IsClosed());
            Thread.sleep(3000);

            s.CloseContacts();
            System.out.println("IsOpened:" + s.IsOpened());
            System.out.println("IsClosed:" + s.IsClosed());

            s.OpenContacts();
            System.out.println("IsOpened:" + s.IsOpened());
            System.out.println("IsClosed:" + s.IsClosed());
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void FlowSensorTest() throws InterruptedException
    {
        FlowSensorTest t = new FlowSensorTest();
        t.Setup();

        t.Start();
    }

    private static void LcdTest() throws Exception
    {
        LcdTest t = new LcdTest();
        t.Test2();

    }

    private static void BillAndCardTest()
    {
        BillAndCardManager mgr = new BillAndCardManager();
        mgr.Open();
        mgr.StartListenCommands();
    }

    private static void ShowSerialPorts()
    {
        //File folder = new File("/dev/serial");
        try ( Stream<Path> walk = Files.walk(Paths.get("/dev/serial/by-id")))
        {
            List<String> result = walk.map(x -> x.toString()).collect(Collectors.toList());
            result.forEach(System.out::println);

        }
        catch (IOException e)
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
        }
        catch (Exception ex)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void OnCardRemoved(Object sender, EventArgs args)
    {
        System.out.println("Card Removed...");
    }
}
