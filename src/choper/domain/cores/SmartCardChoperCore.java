/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.cores;

import choper.domain.*;
import choper.domain.cardReaders.*;
import choper.domain.moneyReaders.*;
import choper.platform.events.EventArgs;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mguerrini
 */
public class SmartCardChoperCore extends ChoperCore
{
    public IMoneyReaderMachine BillReader;

    public ICardReader CardReader;

    //<editor-fold desc="--- Init/Dispose ---">
    @Override
    public void Init(ChoperData data) throws Exception
    {
        super.Init(data);
        this.BillReader = MoneyReaderMachineProvider.Instance.Get();
        this.CardReader = CardReaderProvider.Instance.Get();

        this.Data.AddComponent("IMoneyReaderMachine", BillReader);
        this.Data.AddComponent("ICardReader", CardReader);

        this.Data.Display.ShowMessage("Monedero...");
        this.BillReader.Init();
        this.BillReader.GetTicketReadyEvent().Subscribe(this::OnBillTicketReady);
        this.BillReader.GetTicketAcceptedEvent().Subscribe(this::OnBillTicketAccepted);
        this.BillReader.GetTicketRejectedEvent().Subscribe(this::OnBillTicketRejected);
        this.Data.Display.ShowMessage("Monedero: OK");
        Thread.sleep(200);

        this.Data.Display.ShowMessage("Lector Tarjeta...");
        this.CardReader.Init();
        this.CardReader.GetCardInsertedEvent().Subscribe(this::OnCardInserted);
        this.CardReader.GetCardRemovedEvent().Subscribe(this::OnCardRemoved);
        this.Data.Display.ShowMessage("Lector Tarjeta: OK");
        Thread.sleep(200);
    }

    @Override
    public void ReConfigure()
    {
        super.ReConfigure();

        this.BillReader.UpdateParameters();
        this.CardReader.UpdateParameters();
    }

    @Override
    public void Dispose()
    {
        this.Disconnect();

        this.BillReader.GetTicketReadyEvent().UnSubscribe(this::OnBillTicketReady);
        this.BillReader.GetTicketAcceptedEvent().UnSubscribe(this::OnBillTicketAccepted);
        this.BillReader.GetTicketRejectedEvent().UnSubscribe(this::OnBillTicketRejected);

        this.CardReader.GetCardInsertedEvent().UnSubscribe(this::OnCardInserted);
        this.CardReader.GetCardRemovedEvent().UnSubscribe(this::OnCardRemoved);
    }
    //<editor-fold>

    //<editor-fold desc="--- Connect/Disconnect ---">
    @Override
    public void Connect()
    {
        super.Connect();
        this.BillReader.Connect();
        this.CardReader.Connect();
    }

    @Override
    public void Disconnect()
    {
        super.Disconnect();
        this.BillReader.Disconnect();
        this.CardReader.Disconnect();
    }
    //<editor-fold>

    //<editor-fold desc="--- Smartcard ---">
    public boolean IsSmartCardPresent()
    {
        return this.CardReader.IsCardPresent();
    }

    public void CleanCardMoney()
    {
        if (!this.CardReader.IsCardPresent())
        {
            Logger.getGlobal().info("Tarjeta no insertada.");
            return;
        }

        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.ClearMoney;
        op.Silent = false;

        this.ProcessOperationAsync(op);

    }

    public Float GetBalance()
    {
        if (!this.CardReader.IsCardPresent())
        {
            return null;
        }

        return this.CardReader.GetBalance();
    }

    public void AddMoney(Float value)
    {
        if (!this.CardReader.IsCardPresent())
        {
            Logger.getGlobal().info("Tarjeta no insertada.");
            return;
        }

        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.AddMoney;
        op.Amount = value;
        op.Silent = false;

        this.ProcessOperationAsync(op);

    }

    public void SubstractMoney(Float value)
    {
        if (!this.CardReader.IsCardPresent())
        {
            Logger.getGlobal().info("Tarjeta no insertada.");
            return;
        }

        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.SubMoney;
        op.Amount = value;
        op.Silent = false;

        this.ProcessOperationAsync(op);

    }

    private void OnCardInserted(Object source, EventArgs args)
    {
        try
        {
            this.BillReader.Enabled();
            float currBalance = this.CardReader.GetBalance();

            ChoperOperation op = new ChoperOperation();
            op.Operation = OperationType.CardInserted;
            op.Amount = currBalance;

            this.ProcessOperationAsync(op);
        }
        catch (Exception ex)
        {
            System.out.println(ex);
        }
    }

    private void OnCardRemoved(Object source, EventArgs args)
    {
        //la tarjeta se saco....cancelo la operacion
        this.BillReader.Disabled();

        if (this.Data.Status == ChoperStatusType.Ready)
        {
            this.StopSelling();

            //muestro el mensaje de la maquina
            ChoperOperation op = new ChoperOperation();
            op.Operation = OperationType.CardRemoved;
            this.ProcessOperationAsync(op);
        }
    }



    //</editor-fold>
    //<editor-fold desc="--- Bill Reader ---">
    private void OnBillTicketReady(Object source, Integer bill)
    {
        if (this.CardReader.IsCardPresent())
        {
            this.BillReader.Accept();
        }
        else
        {
            this.BillReader.Reject();
        }
    }

    private void OnBillTicketAccepted(Object source, Integer bill)
    {
        //this.CardWriterTask.Enqueue(source);
        //actualizo el monto de la tarjeta
        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.AddMoney;
        op.Amount = bill;

        this.ProcessOperationAsync(op);
    }

    private void OnBillTicketRejected(Object source, Integer bill)
    {
        //no hago nada
    }
    //</editor-fold>

    @Override
    public void ProcessOperationSync(ChoperOperation op)
    {
        float balance;
        try
        {
            switch (op.Operation)
            {
                case CardInserted:
                    this.Data.Display.ShowBalance(op.Amount);
                    this.Data.Display.ShowVolume(0f);

                    //abro la valvula
                    this.ValidateFinishCondition();
                    
                    break;

                case CardRemoved:
                    this.ShowReadyMessage();
                    break;

                case AddMoney:
                    balance = this.DoGetBalance();
                    boolean addBal = this.DoSetCardBalance(op.Amount + balance);

                    if (addBal)
                    {
                        if (!op.Silent)
                        {
                            balance = this.DoGetBalance();
                            this.Data.Display.UpdateBalance(balance);
                        }
                    }

                    this.ValidateFinishCondition();

                    break;

                case SubMoney:
                    balance = this.DoGetBalance();
                    boolean subBal = this.DoSetCardBalance(op.Amount - balance);

                    if (subBal)
                    {
                        if (!op.Silent)
                        {
                            balance = this.DoGetBalance();
                            this.Data.Display.UpdateBalance(balance);
                        }
                    }

                    this.ValidateFinishCondition();

                    break;

                case ClearMoney:
                    boolean cleanBal = this.DoSetCardBalance(0f);

                    if (cleanBal)
                    {
                        if (!op.Silent)
                        {
                            balance = this.DoGetBalance();
                            this.Data.Display.UpdateBalance(balance);
                        }
                    }

                    this.ValidateFinishCondition();

                    break;


                default:
                    super.ProcessOperationSync(op);
                    break;
            }
        }
        catch (Exception ex)
        {
            System.out.println(ex);
        }
    }

    @Override
    protected Float DoGetBalance()
    {
        return this.CardReader.GetBalance();
    }

    @Override
    protected void DoSetBalance(float value) throws Exception
    {
        boolean ok = this.DoSetCardBalance(value);
        if (!ok)
            throw new Exception("Can not save balance to SmartCard");
    }
    
    private boolean DoSetCardBalance(float value)
    {
        return this.CardReader.SetBalance(value);
    }
}
