/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

import choper.domain.cardReaders.CardReaderProvider;
import choper.domain.switches.ISwitch;
import choper.domain.moneyReaders.IMoneyReaderMachine;
import choper.domain.flowSensors.IFlowSensor;
import choper.domain.displays.IDisplay16x2;
import choper.domain.flowSensors.FlowSensorEventArgs;
import choper.domain.displays.*;
import choper.domain.flowSensors.*;
import choper.domain.moneyReaders.*;
import choper.domain.switches.SwitchProvider;
import choper.platform.ConfigurationProvider;
import choper.platform.events.EventArgs;
import choper.platform.threading.TaskQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import choper.domain.cardReaders.ICardReader;

/**
 *
 * @author max22
 */
public class ChoperMachine
{
    private Display Display;
    private IFlowSensor FlowSensor;
    private IMoneyReaderMachine BillReader;
    private ICardReader CardReader;
    private ISwitch SwitchFlowValve;

    private float LiterPrice = 100; //precio del litro de cerveza
    private int UpdateVolumeWhenGreaterThan = 10; //cm3
    private long WriteBalanceFrequency = 1000; //milisegundos

    private TaskQueue OperationTask;
    private ChoperStatusType Status;
    private boolean HasBalance = false;

    private float TotalMoneyConsumed = 0;
    private float LastUpdatedVolumen;
    private long LastUpdatedTimeInMilliseconds;

    public ChoperMachine()
    {
        this.OperationTask = new TaskQueue(this::DoTask);
        this.Status = ChoperStatusType.Initial;
    }

    public ChoperStatusType GetStatus()
    {
        return this.Status;
    }

    public IFlowSensor GetFlowSensor()
    {
        return this.FlowSensor;
    }

    public ICardReader GetCardReader()
    {
        return this.CardReader;
    }
    
    // <editor-fold defaultstate="collapsed" desc="--- Init / Connect / Disconnect / Reset ---">
    public void Init()
    {
        try
        {
            IDisplay16x2 display = Display16x2Provider.Instance.Get();
            display.Init();
            this.Display = new Display(display);

            this.Display.ShowTitle("-- Iniciando --");

            this.ReInit();
        }
        catch (Exception ex)
        {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
    }

    private void ReInit()
    {
        try
        {
            this.FlowSensor = FlowSensorProvider.Instance.Get();
            this.BillReader = MoneyReaderMachineProvider.Instance.Get();
            this.CardReader = CardReaderProvider.Instance.Get();
            this.SwitchFlowValve = SwitchProvider.Instance.Get();

            this.Display.ShowMessage("Caudalímetro...");
            this.FlowSensor.Init();
            this.FlowSensor.GetVolumeChangedEvent().Subscribe(this::OnFlowSensorChanged);
            this.Display.ShowMessage("Caudalímetro: OK");
            Thread.sleep(200);

            this.Display.ShowMessage("Monedero...");
            this.BillReader.Init();
            this.BillReader.GetTicketReadyEvent().Subscribe(this::OnBillTicketReady);
            this.BillReader.GetTicketAcceptedEvent().Subscribe(this::OnBillTicketAccepted);
            this.BillReader.GetTicketRejectedEvent().Subscribe(this::OnBillTicketRejected);
            this.Display.ShowMessage("Monedero: OK");
            Thread.sleep(200);

            this.Display.ShowMessage("Lector Tarjeta...");
            this.CardReader.Init();
            this.CardReader.GetCardInsertedEvent().Subscribe(this::OnCardInserted);
            this.CardReader.GetCardRemovedEvent().Subscribe(this::OnCardRemoved);
            this.Display.ShowMessage("Lector Tarjeta: OK");
            Thread.sleep(200);

            this.Display.ShowMessage("Válvula...");
            this.SwitchFlowValve.Init();
            this.Display.ShowMessage("Válvula: OK");
            Thread.sleep(200);
            this.Display.ShowMessage("Inic. fin");
            Thread.sleep(200);
            
            this.DoUpdateParameters();

        }
        catch (InterruptedException ex)
        {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
    }

    public void Connect()
    {
        this.SwitchFlowValve.OpenContacts();

        this.BillReader.Connect();
        this.CardReader.Connect();

        this.Status = ChoperStatusType.Ready;

        this.ShowReadyMessage();
    }

    public void Disconnect()
    {
        this.SwitchFlowValve.OpenContacts();

        this.CardReader.Disconnect();
        this.BillReader.Disconnect();
        this.FlowSensor.Disconnect();
        this.FlowSensor.Dispose();
    }

    public void Reset()
    {
        this.Disconnect();

        this.Display.ShowTitle("-- Reseteando --");

        this.FlowSensor.GetVolumeChangedEvent().UnSubscribe(this::OnFlowSensorChanged);

        this.BillReader.GetTicketReadyEvent().UnSubscribe(this::OnBillTicketReady);
        this.BillReader.GetTicketAcceptedEvent().UnSubscribe(this::OnBillTicketAccepted);
        this.BillReader.GetTicketRejectedEvent().UnSubscribe(this::OnBillTicketRejected);

        this.CardReader.GetCardInsertedEvent().UnSubscribe(this::OnCardInserted);
        this.CardReader.GetCardRemovedEvent().UnSubscribe(this::OnCardRemoved);

        this.ReInit();

        this.Connect();
    }

    // </editor-fold>
    //<editor-fold desc="--- Configuration --">
    public void SetParameter(String component, String key, Object value)
    {
        this.SetParameter(component + "." + key, value);
    }

    public void SetParameter(String key, Object value)
    {
        ConfigurationProvider.Instance.Save(key, value);

        if (this.Status == ChoperStatusType.Calibration)
        {
            this.UpdateParameters();
        }
    }

    public Object GetParameter(String key)
    {
        return ConfigurationProvider.Instance.Get(key);
    }

    private void DoUpdateParameters()
    {
        this.LiterPrice = ConfigurationProvider.Instance.GetFloat(this.getClass(), "Price");
        this.UpdateVolumeWhenGreaterThan = ConfigurationProvider.Instance.GetInt(this.getClass(), "UpdateVolumeWhenGreaterThan");
        this.WriteBalanceFrequency = ConfigurationProvider.Instance.GetLong(this.getClass(), "WriteBalanceFrequency");
    }

    public void UpdateParameters()
    {
        this.DoUpdateParameters();

        this.Display.UpdateParameters();
        this.FlowSensor.UpdateParameters();
        this.BillReader.UpdateParameters();
        this.CardReader.UpdateParameters();
        this.SwitchFlowValve.UpdateParameters();
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="--- Calibration ---">
    public void StartCalibration()
    {
        if (this.Status == ChoperStatusType.Calibration || this.Status == ChoperStatusType.Initial)
        {
            return;
        }

        this.Display.ShowPulses(0);
        this.Display.ShowVolume(0f);

        ConfigurationProvider.Instance.BeginTemporalConfiguration();
        this.FlowSensor.Connect();
        this.SwitchFlowValve.CloseContacts(); //abro la valvula
        this.Status = ChoperStatusType.Calibration;
    }

    public void FinishCalibration()
    {
        if (this.Status != ChoperStatusType.Calibration)
        {
            return;
        }

        ConfigurationProvider.Instance.FinishTemporalConfiguration();
        this.FlowSensor.Disconnect();
        this.SwitchFlowValve.OpenContacts();

        this.UpdateParameters();
        this.Status = ChoperStatusType.Ready;

        this.ShowReadyMessage();
    }

    public void CancelCalibration()
    {
        if (this.Status != ChoperStatusType.Calibration)
        {
            return;
        }

        ConfigurationProvider.Instance.CancelTemporalConfiguration();
        this.FlowSensor.Disconnect();
        this.SwitchFlowValve.OpenContacts();

        this.UpdateParameters();
        this.Status = ChoperStatusType.Ready;
        this.ShowReadyMessage();
    }

    // </editor-fold >
    // <editor-fold desc="--- Valvula ---">
    public void OpenFlowValve()
    {
        if (this.Status == ChoperStatusType.Calibration)
        {
            this.SwitchFlowValve.CloseContacts();
        }
    }

    public void CloseFlowValve()
    {
        if (this.Status == ChoperStatusType.Calibration)
        {
            this.SwitchFlowValve.OpenContacts();
        }
    }

    public boolean IsValveOpen()
    {
        return this.SwitchFlowValve.IsClosed();
    }

    // </editor-fold>
    //<editor-fold desc="--- Flow Sensor ---">
    public void SetFlowSensor(int factor)
    {
        if (this.Status == ChoperStatusType.Calibration)
        {
            this.FlowSensor.SetCalibrationFactor(factor);
        }
    }

    public void FlowSensorReset()
    {
        if (this.Status == ChoperStatusType.Calibration)
        {
            this.FlowSensor.Reset();

            this.Display.UpdateVolume(0);
            this.Display.UpdatePulses(0);
        }
    }

    private void OnFlowSensorChanged(Object source, FlowSensorEventArgs data)
    {
        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.VolumeChanged;
        op.FlowSensorData = data;

        this.OperationTask.Enqueue(op);
    }

    private void OnVolumeChange(FlowSensorEventArgs data)
    {
        //registro cada 1 segundo
        if (this.Status == ChoperStatusType.Ready)
        {
            if (data.EventNumber == 1)
            {
                //es el primer evento, lo registro
                this.LastUpdatedTimeInMilliseconds = data.CurrentTimeMillis;

                if (data.TotalVolume <= 0)
                {
                    return;
                }
                
                this.LastUpdatedVolumen = data.TotalVolume;

                //muestro el dinero restante
                boolean ok = this.UpdateBalanceForVolume(data.TotalVolume);

                this.Display.ShowVolume(data.TotalVolume);
                return;
            }

            //actualizo el volumen
            if ((data.TotalVolume - this.LastUpdatedVolumen) > this.UpdateVolumeWhenGreaterThan)
            {
                //actualizo el volumen
                this.LastUpdatedVolumen = data.TotalVolume;
                this.Display.UpdateVolume(data.TotalVolume);
            }

            // actualizo el balance
            if ((data.CurrentTimeMillis - this.LastUpdatedTimeInMilliseconds) >= this.WriteBalanceFrequency)
            {
                this.UpdateBalanceForVolume(data.TotalVolume);
                
                //actualizo el volumen
                this.LastUpdatedTimeInMilliseconds = data.CurrentTimeMillis;
            }

            this.VerifyBalance();

            System.out.println("Flow Sensor - Pulses Value = " + data.Pulses);
            System.out.println("Flow Sensor - Total Volume = " + data.TotalVolume);
            System.out.println();
        }
        else if (this.Status == ChoperStatusType.Calibration)
        {
            this.Display.UpdatePulses(data.Pulses);
            this.Display.UpdateVolume(data.TotalVolume);

            System.out.println("Flow Sensor - EventNumber = " + data.EventNumber);
            System.out.println("Flow Sensor - Pulses Value = " + data.Pulses);
            System.out.println("Flow Sensor - Total Volume = " + data.TotalVolume);
        }
    }

    private boolean UpdateBalanceForVolume(float totalVolume)
    {
        //calculo el monto total consumido
        float totalAmount = this.CalculateAmountForVolume(totalVolume);
        //le resto el monto ya cobrado
        float toSubstract = totalAmount - this.TotalMoneyConsumed;

        boolean ok = false;
        float currMoney = this.CardReader.GetBalance();

        if (currMoney <= toSubstract)
        {
            ok = this.CardReader.SetBalance(0f);
            this.TotalMoneyConsumed += currMoney;
        }
        else
        //escribo en la tarjeta
        {
            float balance = this.CardReader.GetBalance();
            ok = this.CardReader.SetBalance(balance - toSubstract);
            this.TotalMoneyConsumed += toSubstract;
        }

        //muestro el dinero restante
        currMoney = this.CardReader.GetBalance();

        //muestro el dinero actual y el volumen
        this.Display.UpdateBalance(currMoney);

        System.out.println("Monto Total $ " + totalAmount);
        System.out.println("Monto a Descontar $ " + toSubstract);
        System.out.println("Saldo $ " + currMoney);

        return ok;
    }

    private float CalculateAmountForVolume(float volumen)
    {
        //1000 cm3    -----> LiterPrice
        //volumen cm3 -----> X => x = (volumen * LiterPrice) / 1000

        float amount = (volumen * this.LiterPrice) / 1000;
        return amount;
    }

    private boolean VerifyBalance()
    {
        if (this.Status == ChoperStatusType.Ready)
        {
            float saldo = this.CardReader.GetBalance();

            if (saldo > 0)
            {
                this.SwitchFlowValve.CloseContacts(); //activo el switch
                System.out.println("Saldo disponible.");
                return true;
            }
            else
            {
                this.SwitchFlowValve.OpenContacts();//activo el switch
                System.out.println("Saldo insuficiente.");
                return false;
            }
        }

        return true;
    }

    //</editor-fold>
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

        this.OperationTask.Enqueue(op);
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

        this.OperationTask.Enqueue(op);
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

        this.OperationTask.Enqueue(op);
    }

    private void OnCardInserted(Object source, EventArgs args)
    {
        try
        {
            this.HasBalance = false;

            this.InitializeForCardInserted();
            this.BillReader.Enabled();
            float currBalance = this.CardReader.GetBalance();

            ChoperOperation op = new ChoperOperation();
            op.Operation = OperationType.CardInserted;
            op.Amount = currBalance;

            this.OperationTask.Enqueue(op);

            if (this.Status == ChoperStatusType.Ready)
            {
                //conecto el caudalimetro
                this.FlowSensor.Connect();
            }
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

        if (this.Status == ChoperStatusType.Ready)
        {
            //cierro la valvula
            this.SwitchFlowValve.OpenContacts();

            //conecto el caudalimetro
            this.FlowSensor.Disconnect();

            //muestro el mensaje de la maquina
            ChoperOperation op = new ChoperOperation();
            op.Operation = OperationType.CardRemoved;
            this.OperationTask.Enqueue(op);
        }
    }

    private void InitializeForCardInserted()
    {
        this.TotalMoneyConsumed = 0f;
        this.LastUpdatedVolumen = 0f;
        this.LastUpdatedTimeInMilliseconds = 0l;
    }


    /*
    private void EnqueueCardTask(ChoperOperation op)
    {
        //elimino todas las operaciones anteriores de cambio de volumen
        if (op.Operation == OperationType.VolumeChanged)
        {
            this.CardTask.Enqueue(op, (task, index) ->
            {
                ChoperOperation innerOp = (ChoperOperation) task;
                return innerOp.Operation == OperationType.VolumeChanged;
            });
        }
        else
        {
            this.CardTask.Enqueue(op);
        }
    }
    

    private void DoCardTask(Object context, Object data)
    {
        ChoperOperation op = (ChoperOperation) data;

        try
        {
            switch (op.Operation)
            {
                case CardInserted:
                case CardRemoved:
                    break;
                case AddMoney:
                    boolean addBal = this.CardReader.AddBalance(op.Amount);

                    if (addBal)
                    {
                        op.Operation = OperationType.UpdateDisplayBalance;
                        this.OperationTask.Enqueue(op);
                    }

                    break;
                case SubMoney:
                    boolean subBal = this.CardReader.SubtractBalance(op.Amount);

                    if (subBal)
                    {
                        float balance = this.CardReader.GetBalance();
                        this.OperationTask.Enqueue(op);
                    }

                    break;

                case ClearMoney:
                    boolean cleanBal = this.CardReader.SetBalance(0f);

                    if (cleanBal)
                    {
                        float balance = this.CardReader.GetBalance();
                        this.OperationTask.Enqueue(op);
                    }

                    break;
                case VolumeChanged:
                    break;
                default:
                    break;
            }
        }
        catch (Exception ex)
        {
            System.out.println(ex);
        }
    }
     */
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

        this.OperationTask.Enqueue(op);
    }

    private void OnBillTicketRejected(Object source, Integer bill)
    {
        //no hago nada
    }
//</editor-fold>

    private void DoTask(Object context, Object data)
    {
        ChoperOperation op = (ChoperOperation) data;
        float balance;
        try
        {
            switch (op.Operation)
            {
                case CardInserted:
                    this.Display.ShowBalance(op.Amount);
                    this.Display.ShowVolume(0f);

                    //abro la valvula
                    this.VerifyBalance();
                    this.FlowSensor.Reset(); // limpio el sensor porque a veces tira un par de pulsos
                    break;

                case CardRemoved:
                    this.ShowReadyMessage();
                    break;

                case AddMoney:
                    balance = this.CardReader.GetBalance();
                    boolean addBal = this.CardReader.SetBalance(op.Amount + balance);

                    if (addBal)
                    {
                        if (!op.Silent)
                        {
                            balance = this.CardReader.GetBalance();
                            this.Display.UpdateBalance(balance);
                        }
                    }

                    this.VerifyBalance();

                    break;

                case SubMoney:
                    balance = this.CardReader.GetBalance();
                    boolean subBal = this.CardReader.SetBalance(op.Amount - balance);

                    if (subBal)
                    {
                        if (!op.Silent)
                        {
                            balance = this.CardReader.GetBalance();
                            this.Display.UpdateBalance(balance);
                        }
                    }

                    this.VerifyBalance();

                    break;

                case ClearMoney:
                    boolean cleanBal = this.CardReader.SetBalance(0f);

                    if (cleanBal)
                    {
                        if (!op.Silent)
                        {
                            balance = this.CardReader.GetBalance();
                            this.Display.UpdateBalance(balance);
                        }
                    }

                    this.VerifyBalance();

                    break;

                case VolumeChanged:
                    this.OnVolumeChange(op.FlowSensorData);
                    break;

                case UpdateDisplayBalance:
                    if (!op.Silent)
                    {
                        balance = this.CardReader.GetBalance();
                        this.Display.UpdateBalance(balance);
                    }
                    this.VerifyBalance();
                    break;

                default:
                    break;
            }
        }
        catch (Exception ex)
        {
            System.out.println(ex);
        }
    }

    private void ShowReadyMessage()
    {
        this.Display.ShowTitle("-- Disponible --");
        String msg = String.format("Litro: $ %.02f", this.LiterPrice);
        this.Display.ShowMessage(msg);
    }
}
