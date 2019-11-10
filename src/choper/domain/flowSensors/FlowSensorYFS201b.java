/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.flowSensors;

import choper.domain.Environment;
import choper.platform.ConfigurationProvider;
import choper.platform.events.Event;
import choper.platform.events.IEvent;
import com.pi4j.wiringpi.Gpio;
import java.util.logging.Logger;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author mguerrini
 */
public class FlowSensorYFS201b implements IFlowSensor
{
    public IEvent<FlowSensorEventArgs> VolumeChanged = new Event<FlowSensorEventArgs>("FlowSensorYFS201b->VolumeChanged");

    private boolean RaiseEventAsync = true;
    private float PulsesPerLiter = 450;
    private int NotifyFrequency = 500;
    private int Gpio17 = 0;

    private Timer NotifierWorker;

    private int FlowCounter;
    private int LastFlowCounterValue;
    private long StartTimeMillis;

    private int EventCounter = 0;

    private void IncrementFlowCounter(int pin)
    {
        this.FlowCounter++;
    }

    public IEvent<FlowSensorEventArgs> GetVolumeChangedEvent()
    {
        return this.VolumeChanged;
    }

    @Override
    public void Init()
    {
        Environment.Configure();

        this.Gpio17 = ConfigurationProvider.Instance.GetInt("FlowSensor", "GpioNumber");
        System.out.println("Gpio Number: " + this.Gpio17);
        this.RaiseEventAsync = ConfigurationProvider.Instance.GetBool("FlowSensor", "RaiseEventAsync");
        System.out.println("Raise Event Async: " + this.RaiseEventAsync);

        this.UpdateParameters();

        if (Environment.IsRaspberryPiPlatform())
        {
            Gpio.pinMode(Gpio17, Gpio.INPUT);
            Gpio.pullUpDnControl(Gpio17, Gpio.PUD_UP);

            Gpio.wiringPiClearISR(Gpio17);
            Gpio.wiringPiISR(Gpio17, Gpio.INT_EDGE_FALLING, this::IncrementFlowCounter);
        }
    }

    @Override
    public void Connect()
    {
        if (Environment.IsRaspberryPiPlatform())
        {
            //Gpio.wiringPiClearISR(Gpio17);
            //Gpio.wiringPiISR(Gpio17, Gpio.INT_EDGE_FALLING, this::IncrementFlowCounter);
            this.StartTimeMillis = Gpio.millis();
        }
        else
        {
            this.StartTimeMillis = System.currentTimeMillis();
        }

        this.FlowCounter = 0;
        this.LastFlowCounterValue = 0;
        this.EventCounter = 0;

        if (this.NotifierWorker == null)
        {
            this.NotifierWorker = new Timer("FlowSensorNotifier");
            this.NotifierWorker.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    OnTimerTick();
                }

            }, this.NotifyFrequency, this.NotifyFrequency);
        }
    }

    @Override
    public void Disconnect()
    {
        //if (Environment.IsRaspberryPiPlatform())
        //{
        //    Gpio.wiringPiClearISR(Gpio17);
        //}

        if (this.NotifierWorker != null)
        {
            this.NotifierWorker.cancel();
        }

        this.NotifierWorker = null;
    }

    public void Dispose()
    {
        if (Environment.IsRaspberryPiPlatform())
        {
            Gpio.wiringPiClearISR(Gpio17);
        }
    }

    @Override
    public void Reset()
    {
        this.FlowCounter = 0;
        this.LastFlowCounterValue = 0;
        this.EventCounter = 0;
    }

    @Override
    public void UpdateParameters()
    {
        this.PulsesPerLiter = ConfigurationProvider.Instance.GetInt("FlowSensor", "PulsesPerLiter");
        System.out.println("PulsesPerLiter: " + this.PulsesPerLiter);
        this.NotifyFrequency = ConfigurationProvider.Instance.GetInt("FlowSensor", "NotifyFrequency");
        System.out.println("NotifyFrequency: " + this.NotifyFrequency + " ms");
    }

    private void OnTimerTick()
    {
        int currValue = this.FlowCounter;

        if (this.LastFlowCounterValue == currValue)
        {
            return;
        }

        float delta = currValue - this.LastFlowCounterValue;
        this.LastFlowCounterValue = currValue;
        this.EventCounter++;

        FlowSensorEventArgs args = new FlowSensorEventArgs();
        args.Pulses = currValue;
        args.TotalVolume = (currValue * 1000) / this.PulsesPerLiter;
        args.DeltaVolume = (delta * 1000) / this.PulsesPerLiter;
        args.StartTimeMillis = this.StartTimeMillis;
        if (Environment.IsRaspberryPiPlatform())
        {
            args.CurrentTimeMillis = Gpio.millis();
        }
        else
        {
            args.CurrentTimeMillis = System.currentTimeMillis();
        }
        args.EventNumber = this.EventCounter;

        /*
        Logger.getGlobal().info("Flow Sensor - EventNumber = " + args.EventNumber);
        Logger.getGlobal().info("Flow Sensor - Counter Value = " + currValue);
        Logger.getGlobal().info("Flow Sensor - Total Volume = " + args.TotalVolume);
        Logger.getGlobal().info("Flow Sensor - Delta Volume = " + args.DeltaVolume);
        Logger.getGlobal().info("Flow Sensor - StartTimeMillis = " + args.StartTimeMillis);
        Logger.getGlobal().info("Flow Sensor - CurrentTimeMillis = " + args.CurrentTimeMillis);
         */
        if (this.RaiseEventAsync)
        {
            ((Event<FlowSensorEventArgs>) this.VolumeChanged).InvokeAsync(this, args);
        }
        else
        {
            ((Event<FlowSensorEventArgs>) this.VolumeChanged).Invoke(this, args);
        }
    }

    @Override
    public float GetVolume()
    {
        float volumen = 0;
        int currValue = this.FlowCounter;

        volumen = (currValue * 1000) / this.PulsesPerLiter;

        return volumen;
    }

    @Override
    public void SetCalibrationFactor(int factor)
    {
        this.PulsesPerLiter = factor;
    }

    public void SetNotifyChangesTime(int milliseconds)
    {
        this.NotifyFrequency = milliseconds;
    }

}
