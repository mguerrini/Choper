/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.cores;

import choper.domain.ChoperCore;
import choper.domain.ChoperData;
import choper.domain.ChoperOperation;
import choper.domain.flowSensors.FlowSensorEventArgs;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author mguerrini
 */
public class AutomaticChoperCore extends ChoperCore
{
    private float TotalAmount = 0;
    private float CurrentAmount = 0;
    private float TotalVolumen = 0;
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

        this.CurrentAmount = 0;
    }

    public void Disconnect()
    {
        super.Disconnect();
    }
    //</editor-fold>



    protected void StopSelling()
    {
        super.StopSelling();
    }

    @Override
    public void ProcessOperationSync(ChoperOperation op)
    {
        try
        {
            switch (op.Operation)
            {
                case AddMoney:
                    this.DoAddMoney(op.Amount);
                    this.ValidateFinishCondition();
                    break;

                //case Buy:
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

    protected void OnVolumeChange(FlowSensorEventArgs data)
    {

    }

    @Override
    protected Float DoGetBalance()
    {
        return this.CurrentAmount;
    }

    protected void DoAddMoney(float value)
    {
        try
        {
            this.Locker.lock();
           
        }
        finally
        {
            this.Locker.unlock();
        }
    }

    @Override
    protected void DoSetBalance(float value) throws Exception
    {
        try
        {
            this.Locker.lock();
            if (value >= 0)
            {
                this.CurrentAmount = value;
            }
        }
        finally
        {
            this.Locker.unlock();
        }
    }
}
