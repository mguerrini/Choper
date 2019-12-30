/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

import choper.domain.displays.IDisplay16x2;
import choper.domain.flowSensors.*;
import choper.domain.displays.*;

import choper.platform.ConfigurationProvider;
import java.util.logging.Level;
import java.util.logging.Logger;
import choper.domain.cardReaders.ICardReader;
import choper.domain.commands.ChoperCommand;
import choper.domain.commands.CommandFactory;
import choper.domain.commands.ICommand;
import choper.domain.cores.ChoperCoreFactory;

/**
 *
 * @author max22
 */
public class ChoperMachine
{
    private ChoperData Data = new ChoperData();
    private ChoperCore Core;

    public ChoperMachine()
    {
    }

    public ChoperStatusType GetStatus()
    {
        return this.Data.Status;
    }

    public IFlowSensor GetFlowSensor()
    {
        return this.Data.FlowSensor;
    }

    public ICardReader GetCardReader()
    {
        return (ICardReader) this.Data.GetComponent(ICardReader.class);
    }

    public Object GetComponent(Class componentType)
    {
        return this.Core.Data.GetComponent(componentType);
    }

    // <editor-fold defaultstate="collapsed" desc="--- Init / Connect / Disconnect / Reset ---">
    public void Init()
    {
        try
        {
            this.Data.CommandChannel.GetCommandReadyEvent().Subscribe(this::OnCommandReady);

            //creo el core..
            this.Core = ChoperCoreFactory.Instance.Get();

            //creo los comandos
            this.Data.Commands = CommandFactory.Instance.CreateAll();

            IDisplay16x2 display = Display16x2Provider.Instance.Get();
            display.Init();
            this.Data.AddComponent("Display", new Display(display));

            this.Data.Display.ShowTitle("-- Iniciando --");

            this.ReInit();
            
            this.Data.Display.ShowMessage("-- Inic. Fin --");
            Thread.sleep(200);
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
            this.DoUpdateParameters();

            this.Core.Init(this.Data);

            for (ICommand cmd : this.Data.Commands)
            {
                cmd.Init(this.Data.CommandChannel);
            }
        }
        catch (Exception ex)
        {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
    }

    public void Dispose()
    {
        this.Data.CommandChannel.GetCommandReadyEvent().UnSubscribe(this::OnCommandReady);
        this.Core.Dispose();
    }

    public void Connect()
    {
        this.Core.Connect();
    }

    public void Disconnect()
    {
        this.Core.Disconnect();
    }

    public void Reset()
    {
        this.Disconnect();

        this.Data.Display.ShowTitle("-- Reseteando --");

        this.Core.Dispose();

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

        if (this.Data.Status == ChoperStatusType.Calibration)
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
        this.Data.LiterPrice = ConfigurationProvider.Instance.GetFloat(this.getClass(), "Liter", "Price");

        Float aux = ConfigurationProvider.Instance.GetFloat(this.getClass(), "Pint", "Size");
        if (aux == null || aux <= 0)
        {
            this.Data.PintSize = 500;
        }
        else
        {
            this.Data.PintSize = aux;
        }

        aux = ConfigurationProvider.Instance.GetFloat(this.getClass(), "Pint", "Price");
        if (aux == null || aux <= 0)
        {
            //1000 ---> literprice
            //pintsize ---> X
            this.Data.PintPrice = (this.Data.PintSize * this.Data.LiterPrice) / 1000;
        }
        else
        {
            this.Data.PintPrice = aux;
        }

        //pintsize ----> pintprice
        //1000     ----> X = (1000 * pintprice) / pintsize
        this.Data.PintLiterPrice = (1000 * this.Data.PintPrice) / this.Data.PintSize;
        aux = ConfigurationProvider.Instance.GetFloat(this.getClass(), "Free", "Size");
        if (aux == null || aux <= 0)
        {
            this.Data.FreeSize = 0;
        }
        else
        {
            this.Data.FreeSize = aux;
        }

        this.Data.UpdateVolumeWhenGreaterThan = ConfigurationProvider.Instance.GetInt(this.getClass(), "UpdateVolumeWhenGreaterThan");
        this.Data.WriteBalanceFrequency = ConfigurationProvider.Instance.GetLong(this.getClass(), "WriteBalanceFrequency");
    }

    public void UpdateParameters()
    {
        this.DoUpdateParameters();

        this.Core.ReConfigure();

        for (ICommand cmd : this.Data.Commands)
        {
            cmd.Reconfigure();
        }        
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="--- Calibration ---">
    public void StartCalibration()
    {
        this.Core.StartCalibration();
    }

    public void FinishCalibration()
    {
        this.Core.FinishCalibration();
    }

    public void CancelCalibration()
    {
        this.Core.CancelCalibration();
    }

    // </editor-fold >
    // <editor-fold desc="--- Valvula ---">
    public void OpenFlowValve()
    {
        this.Data.SwitchFlowValve.CloseContacts();
    }

    public void CloseFlowValve()
    {
        this.Data.SwitchFlowValve.OpenContacts();
    }

    public void LockUnlockFlowValve(boolean lock)
    {
        if (lock)
        {
            this.Data.SwitchFlowValve.Lock();
        }
        else
        {
            this.Data.SwitchFlowValve.Unlock();
        }
    }

    public boolean IsValveOpen()
    {
        return this.Data.SwitchFlowValve.IsClosed();
    }

    public boolean IsValveLocked()
    {
        return this.Data.SwitchFlowValve.IsLocked();
    }

    // </editor-fold>
    //<editor-fold desc="--- Flow Sensor ---">
    public void SetFlowSensor(int factor)
    {
        if (this.Data.Status == ChoperStatusType.Calibration)
        {
            this.Data.FlowSensor.SetCalibrationFactor(factor);
        }
    }

    public void FlowSensorReset()
    {
        if (this.Data.Status == ChoperStatusType.Calibration)
        {
            this.Data.FlowSensor.Reset();

            this.Data.Display.UpdateVolume(0);
            this.Data.Display.UpdatePulses(0);
        }
    }
    //</editor-fold>

    //<editor-fold desc="--- Money ---">
    public Float GetBalance()
    {
        return this.Core.GetBalance();
    }

    public void AddMoney(float amount)
    {
        if (amount > 0)
        {
            ChoperOperation op = new ChoperOperation();
            op.Operation = OperationType.AddMoney;
            op.Amount = amount;

            this.Core.ProcessOperationAsync(op);
        }
    }

    public void SubstractMoney(float amount)
    {
        if (amount < 0)
        {
            amount = -1 * amount;
        }

        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.SubMoney;
        op.Amount = amount;

        this.Core.ProcessOperationAsync(op);
    }

    public void BuyAmount(float amount)
    {
        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.BuyAmount;
        op.Amount = amount;

        this.Core.ProcessOperationAsync(op);
    }

    public void BuyPint(float percentage)
    {
        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.BuyPint;
        op.Volume = this.Data.PintSize * percentage;

        this.Core.ProcessOperationAsync(op);
    }

    public void BuyLiter(float percentage)
    {
        if (percentage <= 0)
        {
            return;
        }

        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.BuyLiter;
        op.Volume = 1000 * percentage;

        this.Core.ProcessOperationAsync(op);
    }

    public void Free()
    {
        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.Free;
        op.Volume = this.Data.FreeSize;

        if (op.Volume <= 0)
        {
            System.out.println("La chopera no da muestras libres");
            return;
        }
        this.Core.ProcessOperationAsync(op);
    }

    public void FreeByVolumen(float vol)
    {
        if (vol <= 0)
        {
            return;
        }

        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.Free;
        op.Volume = vol;

        this.Core.ProcessOperationAsync(op);
    }

    public void FreeByAmount(float amount)
    {
        if (amount <= 0)
        {
            return;
        }

        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.Free;
        op.Amount = amount;

        this.Core.ProcessOperationAsync(op);
    }

    public void CancelBuy()
    {
        ChoperOperation op = new ChoperOperation();
        op.Operation = OperationType.StopSelling;

        this.Core.ProcessOperationAsync(op);
    }

    //</editor-fold>
    //<editor-fold desc="--- Commands ---">
    public void ExecuteCommand(String name)
    {
        ICommand cmd = this.Data.GetCommand(name);
        if (cmd == null)
        {
            System.out.println("El comando " + name + " no existe.");
            System.out.println("-- Comandos disponibles --");
            
            for (ICommand c : this.Data.Commands)
            {
                System.out.println(c.GetName());
            }
            System.out.println("-- Comandos disponibles Fin --");
            
            return;
        }

        cmd.Execute();
    }

    public void ExecuteCommand(ChoperCommand cmd)
    {
        //los comandos basicos los procesa....los custom
        switch (cmd.Command)
        {
            /*Venta libre*/
            case SubMoney:
                this.SubstractMoney(cmd.Parameter);
                break;

            case AddMoney:
                this.AddMoney(cmd.Parameter);
                break;

            /*Precio de la pinta*/
            case Free:
                if (cmd.Parameter > 0)
                {
                    this.FreeByVolumen(cmd.Parameter);
                }
                else
                {
                    this.Free();
                }
                break;

            case BuyAmount:
                this.BuyAmount(cmd.Parameter);
                break;

            case BuyPint:
                this.BuyPint(cmd.Parameter);
                break;

            case BuyLiter:
                this.BuyLiter(cmd.Parameter);
                break;

            case OpenValve:
                this.Data.SwitchFlowValve.CloseContacts();
                break;

            case CloseValve:
                this.Data.SwitchFlowValve.OpenContacts();
                break;

            case LockValve:
                this.Data.SwitchFlowValve.Lock();
                break;

            case UnlockValve:
                this.Data.SwitchFlowValve.Unlock();
                break;

            case Other:
            default:
                this.Core.ExecuteCommand(cmd);
                break;
        }
    }

    private void OnCommandReady(Object source, ChoperCommand cmd)
    {
        this.ExecuteCommand(cmd);
    }
    //</editor-fold>
}
