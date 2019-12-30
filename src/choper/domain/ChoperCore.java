/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

import choper.domain.commands.ChoperCommand;
import choper.domain.flowSensors.FlowSensorEventArgs;
import choper.domain.flowSensors.FlowSensorProvider;
import choper.domain.switches.SwitchProvider;
import choper.platform.ConfigurationProvider;
import choper.platform.threading.TaskQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mguerrini
 */
public abstract class ChoperCore
{
    protected ChoperData Data;
    private TaskQueue OperationTask;
    protected float TotalVolumen;
    protected float CurrentConsumedVolumen;
    protected float TotalMoneyConsumed = 0;
    protected long LastUpdatedTimeInMilliseconds;

    public ChoperCore()
    {
        this.OperationTask = new TaskQueue(this::DoTask);
    }

    //<editor-fold desc="--- Init/ReConfigure/Dispose ---">
    public void Init(ChoperData data) throws Exception
    {
        Data = data;

        this.Data.AddComponent("FlowSensor", FlowSensorProvider.Instance.Get());
        this.Data.AddComponent("SwitchFlowValve", SwitchProvider.Instance.Get());

        this.Data.Display.ShowMessage("Caudalímetro...");
        this.Data.FlowSensor.Init();
        this.Data.FlowSensor.GetVolumeChangedEvent().Subscribe(this::OnFlowSensorChanged);
        this.Data.Display.ShowMessage("Caudalímetro: OK");
        Thread.sleep(200);

        this.Data.Display.ShowMessage("Válvula...");
        this.Data.SwitchFlowValve.Init();
        this.Data.Display.ShowMessage("Válvula: OK");
        Thread.sleep(200);
    }

    public void ReConfigure()
    {
        this.Data.Display.UpdateParameters();
        this.Data.FlowSensor.UpdateParameters();
        this.Data.SwitchFlowValve.UpdateParameters();
    }

    public void Dispose()
    {
        this.Data.FlowSensor.GetVolumeChangedEvent().UnSubscribe(this::OnFlowSensorChanged);
    }
    //</editor-fold>

    //<editor-fold desc="--- Connect/Disconnect ---">
    public void Connect()
    {
        this.Data.SwitchFlowValve.OpenContacts();

        this.Data.Status = ChoperStatusType.Ready;
        this.ShowReadyMessage();
    }

    public void Disconnect()
    {
        this.Data.SwitchFlowValve.OpenContacts();
        this.Data.Status = ChoperStatusType.Initial;

        this.Data.FlowSensor.Disconnect();
        this.Data.FlowSensor.Dispose();
    }
    //</editor-fold>

    //<editor-fold desc="--- Money Admin ---">
    public Float GetBalance()
    {
        return this.DoGetBalance();
    }

    protected abstract Float DoGetBalance();

    protected abstract void DoSetBalance(float value) throws Exception;

    protected void DoSubstractBalance(float value)
    {
        float currAmount = this.DoGetBalance();
        currAmount -= value;
        try
        {
            this.DoSetBalance(currAmount);
        }
        catch (Exception ex)
        {
            Logger.getLogger(ChoperCore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void DoAddBalance(float value)
    {
        float currAmount = this.DoGetBalance();
        currAmount += value;
        try
        {
            this.DoSetBalance(currAmount);
        }
        catch (Exception ex)
        {
            Logger.getLogger(ChoperCore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //</editor-fold>
    //<editor-fold desc="--- Flow Sensor ---">
    private void OnFlowSensorChanged(Object source, FlowSensorEventArgs data)
    {
        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.VolumeChanged;
        op.FlowSensorData = data;

        this.OperationTask.Enqueue(op);
    }

    protected void OnVolumeChange(FlowSensorEventArgs data)
    {
        //registro cada 1 segundo
        if (this.Data.Status == ChoperStatusType.Working)
        {
            if (data.EventNumber == 1)
            {
                //es el primer evento, lo registro
                this.LastUpdatedTimeInMilliseconds = data.CurrentTimeMillis;

                if (data.TotalVolume <= 0)
                {
                    return;
                }

                this.CurrentConsumedVolumen = data.TotalVolume;

                //muestro el dinero restante
                this.UpdateBalanceForVolume(data.TotalVolume);

                this.Data.Display.ShowVolume(data.TotalVolume);
                return;
            }

            //actualizo el volumen
            if ((data.TotalVolume - this.CurrentConsumedVolumen) > this.Data.UpdateVolumeWhenGreaterThan)
            {
                //actualizo el volumen
                this.CurrentConsumedVolumen = data.TotalVolume;
                this.Data.Display.UpdateVolume(data.TotalVolume);
            }

            // actualizo el balance
            if ((data.CurrentTimeMillis - this.LastUpdatedTimeInMilliseconds) >= this.Data.WriteBalanceFrequency)
            {
                this.UpdateBalanceForVolume(data.TotalVolume);

                //actualizo el volumen
                this.LastUpdatedTimeInMilliseconds = data.CurrentTimeMillis;
            }

            this.ValidateFinishCondition();

            System.out.println("Flow Sensor - Pulses Value = " + data.Pulses);
            System.out.println("Flow Sensor - Total Volume = " + data.TotalVolume);
            System.out.println();
        }
        else if (this.Data.Status == ChoperStatusType.Calibration)
        {
            this.Data.Display.UpdatePulses(data.Pulses);
            this.Data.Display.UpdateVolume(data.TotalVolume);

            System.out.println("Flow Sensor - EventNumber = " + data.EventNumber);
            System.out.println("Flow Sensor - Pulses Value = " + data.Pulses);
            System.out.println("Flow Sensor - Total Volume = " + data.TotalVolume);
        }
    }

    private void UpdateBalanceForVolume(float totalVolume)
    {
        float currMoney = this.DoGetBalance();

        //calculo el monto total consumido
        float totalAmount = this.GetAmountForVolume(totalVolume, this.Data.StartSellingData.LiterPrice);
        //le resto el monto ya cobrado
        float toSubstract = totalAmount - this.TotalMoneyConsumed;

        try
        {
            this.DoSubstractBalance(toSubstract);
        }
        catch (Exception ex)
        {
            Logger.getLogger(ChoperCore.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (currMoney <= toSubstract)
        {
            this.TotalMoneyConsumed += currMoney;
        }
        else
        {
            this.TotalMoneyConsumed += toSubstract;
        }

        //muestro el dinero restante
        currMoney = this.DoGetBalance();

        //muestro el dinero actual y el volumen
        this.Data.Display.UpdateBalance(currMoney);

        System.out.println("Monto Total $ " + totalAmount);
        System.out.println("Monto a Descontar $ " + toSubstract);
        System.out.println("Saldo $ " + currMoney);
    }

    protected float GetAmountForVolume(float volumen, float literPrice)
    {
        //1000 cm3    -----> LiterPrice
        //volumen cm3 -----> X => x = (volumen * LiterPrice) / 1000
        float amount = (volumen * literPrice) / 1000;
        return amount;
    }

    protected float GetVolumeForAmount(float amount, float literPrice)
    {
        //1000 cm3    -----> LiterPrice
        //volumen cm3 -----> amount => volumen = (amount * 1000) / LiterPrice
        float volume = (amount * 1000) / literPrice;
        return volume;
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="--- Calibration ---">
    public void StartCalibration()
    {
        if (this.Data.Status == ChoperStatusType.Calibration || this.Data.Status == ChoperStatusType.Initial)
        {
            return;
        }

        this.Data.Display.ShowPulses(0);
        this.Data.Display.ShowVolume(0f);

        ConfigurationProvider.Instance.BeginTemporalConfiguration();
        this.Data.FlowSensor.Connect();
        this.Data.SwitchFlowValve.CloseContacts(); //abro la valvula
        this.Data.Status = ChoperStatusType.Calibration;
    }

    public void FinishCalibration()
    {
        if (this.Data.Status != ChoperStatusType.Calibration)
        {
            return;
        }

        ConfigurationProvider.Instance.FinishTemporalConfiguration();
        this.Data.FlowSensor.Disconnect();
        this.Data.SwitchFlowValve.OpenContacts();

        this.ReConfigure();
        this.Data.Status = ChoperStatusType.Ready;

        this.ShowReadyMessage();
    }

    public void CancelCalibration()
    {
        if (this.Data.Status != ChoperStatusType.Calibration)
        {
            return;
        }

        ConfigurationProvider.Instance.CancelTemporalConfiguration();
        this.Data.FlowSensor.Disconnect();
        this.Data.SwitchFlowValve.OpenContacts();

        this.ReConfigure();
        this.Data.Status = ChoperStatusType.Ready;

        this.ShowReadyMessage();
    }
    //</editor-fold>

    //<editor-fold desc="--- Process Operation ---">
    public void ProcessOperationSync(ChoperOperation op)
    {
        try
        {
            switch (op.Operation)
            {
                case StartSelling:
                    this.DoSetBalance(op.Amount);
                    this.ValidateFinishCondition();
                    break;

                case StopSelling:
                    this.DoSetBalance(0f);
                    this.ValidateFinishCondition();
                    break;

                case SubMoney:
                    this.OnSubstractBalance(op);
                    break;

                case AddMoney:
                    this.DispatchAddBalance(op);
                    break;

                case BuyAmount:
                    this.OnBuyByAmount(op);
                    break;

                case BuyLiter:
                    this.OnBuyByLiter(op);
                    break;

                case BuyPint:
                    this.OnBuyByPint(op);
                    break;

                case Free:
                    this.OnFree(op);
                    break;

                case VolumeChanged:
                    this.OnVolumeChange(op);
                    break;

                case UpdateDisplayBalance:
                    if (!op.Silent)
                    {
                        float balance = this.DoGetBalance();
                        this.Data.Display.UpdateBalance(balance);
                    }
                    this.ValidateFinishCondition();
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

    public void ProcessOperationAsync(ChoperOperation op)
    {
        this.OperationTask.Enqueue(op);
    }

    private void DoTask(Object context, Object data)
    {
        ChoperOperation op = (ChoperOperation) data;
        this.ProcessOperationSync(op);
    }

    //</editor-fold>
    //<editor-fold desc="--- Acciones ---">
    private void DispatchAddBalance(ChoperOperation op)
    {
        if (op.Amount <= 0)
        {
            return;
        }

        if (this.Data.Status == ChoperStatusType.Ready)
        {
            op.Operation = OperationType.BuyAmount;
            this.OnBuyByAmount(op);
        }
        else
        {
            this.OnAddBalance(op);
        }
    }

    protected void OnAddBalance(ChoperOperation op)
    {
        this.DoAddBalance(op.Amount);

        //obtengo el volument
        float vol = this.GetVolumeForAmount(op.Amount, this.Data.StartSellingData.LiterPrice);
        this.TotalVolumen += vol;
    }

    protected void OnSubstractBalance(ChoperOperation op)
    {
        this.DoSubstractBalance(op.Amount);

        float vol = this.GetVolumeForAmount(op.Amount, this.Data.StartSellingData.LiterPrice);
        this.TotalVolumen -= vol;

        this.ValidateFinishCondition();
    }

    protected void OnVolumeChange(ChoperOperation op)
    {
        this.OnVolumeChange(op.FlowSensorData);
    }

    protected void OnBuyByAmount(ChoperOperation op)
    {
        if (this.Data.Status != ChoperStatusType.Ready || op.Amount <= 0)
        {
            return;
        }

        StartSellingData startData = new StartSellingData();
        startData.OperationType = ChoperSellingType.ByAmount;
        startData.LiterPrice = this.Data.LiterPrice;
        startData.Amount = op.Amount;
        startData.Volumen = this.GetVolumeForAmount(op.Amount, this.Data.LiterPrice);

        this.StartSelling(startData);
    }

    protected void OnBuyByLiter(ChoperOperation op)
    {
        if (this.Data.Status != ChoperStatusType.Ready || op.Volume <= 0)
        {
            return;
        }

        StartSellingData startData = new StartSellingData();
        startData.OperationType = ChoperSellingType.ByVolume;
        startData.LiterPrice = this.Data.LiterPrice;
        startData.Amount = this.GetAmountForVolume(op.Volume, this.Data.LiterPrice);
        startData.Volumen = op.Volume;

        this.StartSelling(startData);

    }

    protected void OnBuyByPint(ChoperOperation op)
    {
        if (this.Data.Status != ChoperStatusType.Ready || op.Volume <= 0)
        {
            return;
        }

        StartSellingData startData = new StartSellingData();
        startData.OperationType = ChoperSellingType.ByVolume;
        startData.LiterPrice = this.Data.PintLiterPrice;
        startData.Amount = this.GetAmountForVolume(op.Volume, this.Data.PintLiterPrice);
        startData.Volumen = op.Volume;

        this.StartSelling(startData);
    }

    protected void OnFree(ChoperOperation op)
    {
        if (this.Data.Status != ChoperStatusType.Ready || (op.Volume <= 0 && op.Amount <= 0))
        {
            return;
        }

        StartSellingData startData = new StartSellingData();
        startData.OperationType = ChoperSellingType.Free;
        startData.LiterPrice = this.Data.LiterPrice;
        if (op.Volume > 0)
        {
            startData.Amount = this.GetAmountForVolume(op.Volume, this.Data.LiterPrice);
            startData.Volumen = op.Volume;
        }
        else
        {
            startData.Amount = op.Amount;
            startData.Volumen = this.GetVolumeForAmount(op.Amount, this.Data.LiterPrice);
        }

        this.StartSelling(startData);
    }

    //</editor-fold>
    //<editor-fold desc="--- Start / Stop Selling ---">
    protected void StartSelling(StartSellingData startData)
    {
        this.Data.Status = ChoperStatusType.Working;
        this.Data.StartSellingData = startData;

        this.CurrentConsumedVolumen = 0;
        this.TotalVolumen = startData.Volumen;

        this.TotalMoneyConsumed = 0f;
        this.LastUpdatedTimeInMilliseconds = 0l;

        this.Data.FlowSensor.Connect();//conecto el caudalimetro

        this.Data.SwitchFlowValve.CloseContacts(); //activo el switch

        try
        {
            Thread.sleep(1000); //espero un segundo y luego reseteo al sensor
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(ChoperCore.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.Data.FlowSensor.Reset(); // limpio el sensor porque a veces tira un par de pulsos
    }

    private void StopSelling()
    {
        this.DoStopSelling();
        
        this.ShowReadyMessage();
    }
    
    protected void DoStopSelling()
    {
        this.Data.Status = ChoperStatusType.Ready;

        //cierro la valvula
        this.Data.SwitchFlowValve.OpenContacts();

        //desconecto el caudalimetro
        this.Data.FlowSensor.Disconnect();
    }

    protected void ValidateFinishCondition()
    {
        if (this.Data.Status == ChoperStatusType.Working)
        {
            if (this.Data.StartSellingData.OperationType == ChoperSellingType.ByAmount)
            {
                float saldo = this.DoGetBalance();

                if (saldo <= 0)
                {
                    this.StopSelling();

                    System.out.println("Saldo insuficiente. Fin de la operación");
                }
            }
            else
            {
                if (this.CurrentConsumedVolumen > this.TotalVolumen)
                {
                    this.StopSelling();

                    System.out.println("Volumen alcanzado. Fin de la operación");
                }
            }
        }
    }

    //</editor-fold>
    protected void ShowReadyMessage()
    {
        this.Data.Display.ShowTitle("-- Disponible --");
        String msg = String.format("Litro: $ %.02f", this.Data.LiterPrice);
        this.Data.Display.ShowMessage(msg);
    }

    public void ExecuteCommand(ChoperCommand cmd)
    {
        //no hace nada...ya que los comandos principales ya son atendidos
    }
}
