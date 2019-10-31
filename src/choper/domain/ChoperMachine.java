/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

import choper.domain.switches.ISwitch;
import choper.domain.smartCards.ISmartCardReader;
import choper.domain.moneyReaders.IMoneyReaderMachine;
import choper.domain.flowSensors.IFlowSensor;
import choper.domain.displays.IDisplay16x2;
import choper.domain.flowSensors.FlowSensorEventArgs;
import choper.domain.displays.*;
import choper.domain.flowSensors.*;
import choper.domain.moneyReaders.*;
import choper.domain.smartCards.*;
import choper.domain.switches.SwitchProvider;
import choper.platform.events.EventArgs;
import choper.platform.threading.TaskQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author max22
 */
public class ChoperMachine
{
    private Display Display;
    private IFlowSensor FlowSensor;
    private IMoneyReaderMachine BillReader;
    private ISmartCardReader SmartCardReader;
    private ISwitch FlowValve;

    private float LiterPrice = 100; //precio del litro de cerveza
    private TaskQueue OperationTask;

    public ChoperMachine()
    {
        this.OperationTask = new TaskQueue(this::DoTask);
    }

    public void Init()
    {
        try
        {
            IDisplay16x2 display = Display16x2Provider.Instance.Get();
            display.Init();
            this.Display = new Display(display);

            this.FlowSensor = FlowSensorProvider.Instance.Get();
            this.BillReader = MoneyReaderMachineProvider.Instance.Get();
            this.SmartCardReader = SmartCardReaderProvider.Instance.Get();
            this.FlowValve = SwitchProvider.Instance.Get();

            this.Display.ShowTitle("-- Iniciando --");

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

            this.Display.ShowMessage("Lector Chip...");
            this.SmartCardReader.Init();
            this.SmartCardReader.GetCardInsertedEvent().Subscribe(this::OnCardInserted);
            this.SmartCardReader.GetCardRemovedEvent().Subscribe(this::OnCardRemoved);
            this.Display.ShowMessage("Lector Chip: OK");
            Thread.sleep(200);

            this.Display.ShowMessage("Válvula...");
            this.FlowValve.Init();
            this.Display.ShowMessage("Válvula: OK");
            Thread.sleep(200);
            this.Display.ShowMessage("Inic. fin");
            Thread.sleep(200);

        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(ChoperMachine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Connect()
    {
        this.SmartCardReader.Connect();
        this.BillReader.Connect();
    }

    public void Disconnect()
    {
        this.SmartCardReader.Disconnect();
        this.BillReader.Disconnect();
        this.FlowSensor.Disconnect();
    }

    private void OnCardInserted(Object source, EventArgs args)
    {
        try
        {
            this.InitializeForCardInserted();
            this.BillReader.Enabled();
            float currBalance = this.SmartCardReader.GetBalance();

            ChoperOperation op = new ChoperOperation();
            op.Operation = OperationType.CardInserted;
            op.Amount = currBalance;

            this.OperationTask.Enqueue(op);

            //conecto el caudalimetro
            this.FlowSensor.Connect();

            //abro la valvula
            this.FlowValve.Open();

        }
        catch (Exception ex)
        {
            System.out.println(ex);
        }
    }

    private void OnCardRemoved(Object source, EventArgs args)
    {
        //cierro la valvula
        this.FlowValve.Close();

        //la tarjeta se saco....cancelo la operacion
        this.BillReader.Disabled();

        //conecto el caudalimetro
        this.FlowSensor.Disconnect();

        //muestro el mensaje de la maquina
        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.CardRemoved;
        this.OperationTask.Enqueue(op);
    }

    private void OnBillTicketReady(Object source, Integer bill)
    {
        if (this.SmartCardReader.IsCardPresent())
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

    private void OnFlowSensorChanged(Object source, FlowSensorEventArgs data)
    {
        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.VolumeChanged;
        op.FlowSensorData = data;

        this.OperationTask.Enqueue(op);
    }

    private void DoTask(Object context, Object data)
    {
        ChoperOperation op = (ChoperOperation) data;

        try
        {
            switch (op.Operation)
            {
                case CardInserted:
                    this.Display.ShowBalance(op.Amount);
                    this.Display.ShowVolume(0f);
                    break;

                case CardRemoved:
                    this.ShowReadyMessage();
                    break;

                case AddMoney:
                    boolean addBal = this.SmartCardReader.AddBalance(op.Amount);

                    if (addBal)
                    {
                        float balance = this.SmartCardReader.GetBalance();
                        this.Display.UpdateBalance(balance);
                    }
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

    private int UpdateVolumeFrequency = 50; //cm3
    private long WriteBalanceFrequency = 1000; //milisegundos

    private float TotalMoneyConsumed = 0;
    private float LastUpdatedVolumen;
    private long LastUpdatedTimeInMilliseconds;

    private void InitializeForCardInserted()
    {
        this.TotalMoneyConsumed = 0f;
        this.LastUpdatedVolumen = 0f;
        this.LastUpdatedTimeInMilliseconds = 0l;
    }

    private void OnVolumeChange(FlowSensorEventArgs data)
    {
        //registro cada 1 segundo
        if (data.EventNumber == 1)
        {
            this.UpdateOnFirstVolumeChanged(data);
            return;
        }

        if ((data.CurrentTimeMillis - this.LastUpdatedTimeInMilliseconds) >= this.WriteBalanceFrequency)
        {
            this.UpdateOnNonFirstVolumeChanged(data);
        }
        else
        {
            if ((data.TotalVolume - this.LastUpdatedVolumen) > this.UpdateVolumeFrequency)
            {
                //actualizo el volumen
                this.LastUpdatedVolumen = data.TotalVolume;
                this.Display.UpdateVolume(data.TotalVolume);
            }
        }

    }

    private void UpdateOnFirstVolumeChanged(FlowSensorEventArgs data)
    {
        //es el primer evento, lo registro
        this.LastUpdatedTimeInMilliseconds = data.CurrentTimeMillis;
        if (data.TotalVolume <= 0)
        {
            return;
        }

        this.LastUpdatedVolumen = data.TotalVolume;

        //muestro el dinero restante
        boolean ok = this.UpdateSaldo(data.TotalVolume);

        this.Display.ShowVolume(data.TotalVolume);

    }

    private void UpdateOnNonFirstVolumeChanged(FlowSensorEventArgs data)
    {
        this.LastUpdatedTimeInMilliseconds = data.CurrentTimeMillis;

        //actualizo el saldo
        this.UpdateSaldo(data.TotalVolume);

        //actualizo el volumen
        this.LastUpdatedVolumen = data.TotalVolume;
        this.Display.UpdateVolume(data.TotalVolume);
    }

    private boolean UpdateSaldo(float totalVolume)
    {
        //calculo el monto total consumido
        float totalAmount = this.CalculateAmountFor(totalVolume);
        //le resto el monto ya cobrado
        float toSubstract = totalAmount - this.TotalMoneyConsumed;

        boolean ok = false;
        float currMoney = this.SmartCardReader.GetBalance();

        if (currMoney <= toSubstract)
        {
            ok = this.SmartCardReader.SetBalance(0f);
            this.TotalMoneyConsumed += currMoney;
        }
        else
        //escribo en la tarjeta
        {
            ok = this.SmartCardReader.SubtractBalance(toSubstract);
            this.TotalMoneyConsumed += toSubstract;
        }

        //muestro el dinero restante
        currMoney = this.SmartCardReader.GetBalance();

        //muestro el dinero actual y el volumen
        this.Display.UpdateBalance(currMoney);

        return ok;
    }

    private float CalculateAmountFor(float volumen)
    {
        //1000 cm3    -----> LiterPrice
        //volumen cm3 -----> X => x = (volumen * LiterPrice) / 1000

        float amount = (volumen * this.LiterPrice) / 1000;
        return amount;
    }

    private void ShowReadyMessage()
    {
        this.Display.ShowTitle("-- Disponible --");
        String msg = String.format("Litro: $ %.02f", this.LiterPrice);
        this.Display.ShowMessage(msg);
    }

}
