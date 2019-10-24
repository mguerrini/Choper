/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.test;

import choper.Main;
import choper.domain.moneyReaders.*;
import choper.domain.smartCards.*;
import choper.platform.events.EventArgs;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mguerrini
 */
public class BillAndCardManager
{
    public BillAndCardManager()
    {

    }

    public SmartCardReader CardReader;

    public MoneyReaderMachine BillReader;

    public boolean MustAccept = false;

    public boolean AutoAcceptReject = true;

    
    public void Open()
    {
        this.CardReader = SmartCardReaderProvider.Instance.Get();
        this.BillReader = MoneyReaderMachineProvider.Instance.Get();

        this.CardReader.CardInserted.Subscribe(this::OnCardInserted);
        this.CardReader.CardRemoved.Subscribe(this::OnCardRemoved);
        this.CardReader.BalanceChanged.Subscribe(this::OnCardRemoved);

        this.BillReader.DataReceived.Subscribe(this::OnBillDataReceived);
        this.BillReader.TicketAccepted.Subscribe(this::OnBillTicketAccepted);
        this.BillReader.TicketReady.Subscribe(this::OnBillTicketReady);
        this.BillReader.TicketRejected.Subscribe(this::OnBillTicketRejected);

        this.BillReader.Connect();
        this.CardReader.Connect();
    }

    public void Close()
    {
        this.CardReader.CardInserted.UnSubscribe(this::OnCardInserted);
        this.CardReader.CardRemoved.UnSubscribe(this::OnCardRemoved);
        this.CardReader.BalanceChanged.UnSubscribe(this::OnCardRemoved);

        this.BillReader.DataReceived.UnSubscribe(this::OnBillDataReceived);
        this.BillReader.TicketAccepted.UnSubscribe(this::OnBillTicketAccepted);
        this.BillReader.TicketReady.UnSubscribe(this::OnBillTicketReady);
        this.BillReader.TicketRejected.UnSubscribe(this::OnBillTicketRejected);

        this.BillReader.Disconnect();
        this.CardReader.Disconnect();
    }

    private void OnCardInserted(Object source, EventArgs args)
    {
        try
        {
            this.WriteLine("Card Inserted");
            float money = this.CardReader.GetBalance();
            this.WriteLine("Card Inserted - Saldo: " + money);
        } catch (Exception ex)
        {
            this.WriteLine("Get Balance Error");
            this.WriteLine(ex);
        }
    }

    private void OnCardRemoved(Object source, EventArgs args)
    {
        this.WriteLine("Card Inserted");
    }

    private void OnBalanceChanged(Object source, EventArgs args)
    {
        try
        {
            this.WriteLine("Balance changed");
            float money = this.CardReader.GetBalance();
            this.WriteLine("Balance changed - Saldo: " + money);
        } catch (Exception ex)
        {
            this.WriteLine("Get Balance Error");
            this.WriteLine(ex);
        }
    }

    private void OnBillDataReceived(Object source, MoneyReaderMachineDataReceivedEventArgs args)
    {
        this.WriteLine("BV20 event received: " + args.Data);
    }

    private void OnBillTicketReady(Object source, Integer bill)
    {
        this.WriteLine("BV20 ticket ready: " + bill);

        if (this.AutoAcceptReject)
        {
            if (this.MustAccept)
            {
                boolean b = this.BillReader.Accept();
                this.WriteLine("Command Accepted - Executed: " + b);
            } else
            {
                boolean b = this.BillReader.Reject();
                this.WriteLine("Command Reject - Executed: " + b);
            }
        }
    }

    private void OnBillTicketAccepted(Object source, Integer bill)
    {
        this.WriteLine("BV20 ticket accepted: " + bill);

        boolean addBal = this.CardReader.AddBalance(bill);

        if (addBal)
        {
            this.WriteLine("Add Balance success");
        } else
        {
            this.WriteLine("Add Balance failed");
        }

    }

    private void OnBillTicketRejected(Object source, Integer bill)
    {
        this.WriteLine("BV20 ticket rejected: " + bill);
    }

    private void ListeInputs()
    {
        String input = "";

        Runnable runnableTask = () ->
        {
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
        };

        CompletableFuture.runAsync(runnableTask);
    }

    public void WriteLine(Object msg)
    {
        System.out.println(msg);
    }
}
