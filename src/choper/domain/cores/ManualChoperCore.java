/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.cores;

import choper.domain.ChoperCore;
import choper.domain.ChoperData;
import choper.domain.ChoperOperation;
import choper.domain.ChoperStatusType;
import choper.domain.StartSellingData;
import choper.domain.commands.ChoperCommand;
import choper.domain.flowSensors.FlowSensorEventArgs;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mguerrini
 */
public class ManualChoperCore extends ChoperCore
{
    //private float CurrentAvailableAmount = 0;
    private float TotalAmount = 0;

    private Lock Locker = new ReentrantLock();

    //<editor-fold desc="--- Init/ReConfigure/Dispose ---">
    public void Init(ChoperData data) throws Exception
    {
        super.Init(data);
    }

    public void ReConfigure()
    {
        super.ReConfigure();
    }

    public void Dispose()
    {
        super.Dispose();
    }
    //</editor-fold>

    //<editor-fold desc="--- Connect/Disconnect ---">
    public void Connect()
    {
        super.Connect();

        //this.CurrentAvailableAmount = 0;
        this.TotalAmount = 0;
    }

    public void Disconnect()
    {
        super.Disconnect();
    }
    //</editor-fold>

   protected void StartSelling(StartSellingData startData)
   {
        try
        {
            this.Locker.lock();
            if (startData.Amount >= 0)
            {
                this.TotalAmount = startData.Amount;
                //this.CurrentAvailableAmount = startData.Amount;
            }
        }
        finally
        {
            this.Locker.unlock();
        }

        super.StartSelling(startData);
    }

    
    //<editor-fold desc="--- Balance ---">

    @Override
    protected Float DoGetBalance()
    {
        //return this.CurrentAvailableAmount;
        return this.TotalAmount;
    }

    @Override
    protected void DoSetBalance(float value) throws Exception
    {
        try
        {
            this.Locker.lock();
            if (value >= 0)
            {
                this.TotalAmount = value;
                //this.CurrentAvailableAmount = value;
            }
        }
        finally
        {
            this.Locker.unlock();
        }
    }

    @Override
    protected void DoSubstractBalance(float value)
    {
        try
        {
            this.Locker.lock();
            this.TotalAmount -= value;

            if (this.TotalAmount < 0)
            {
                this.TotalAmount = 0;
            }
        }
        finally
        {
            this.Locker.unlock();
        }

    }

    @Override
    protected void DoAddBalance(float value)
    {
        try
        {
            this.Locker.lock();
            this.TotalAmount += value;
        }
        finally
        {
            this.Locker.unlock();
        }
    }

    //</editor-fold>
}
