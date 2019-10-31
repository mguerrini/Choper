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
        this.RaiseEventAsync = false;
    }

    public IEvent<FlowSensorEventArgs> GetVolumeChangedEvent()
    {
        return this.VolumeChanged;
    }

    @Override
    public void Init()
    {
        Environment.Configure();

        this.Gpio17 = ConfigurationProvider.Instance.GetInt(this.getClass(), "GpioNumber");
        this.PulsesPerLiter = ConfigurationProvider.Instance.GetInt(this.getClass(), "GpioNumber");
        this.NotifyFrequency = ConfigurationProvider.Instance.GetInt(this.getClass(), "GpioNumber");
        this.RaiseEventAsync = ConfigurationProvider.Instance.GetBool(this.getClass(), "RaiseEventAsync");

        Gpio.pinMode(Gpio17, Gpio.INPUT);
        Gpio.pullUpDnControl(Gpio17, Gpio.PUD_UP);

        this.NotifierWorker = new Timer("FlowSensorNotifier");
    }

    @Override
    public void Connect()
    {
        Gpio.wiringPiISR(Gpio17, Gpio.INT_EDGE_FALLING, this::IncrementFlowCounter);
        this.FlowCounter = 0;
        this.LastFlowCounterValue = 0;
        this.StartTimeMillis = Gpio.millis();
        this.EventCounter = 0;

        this.NotifierWorker.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                OnTimerTick();
            }

        }, this.NotifyFrequency, this.NotifyFrequency);

        /*
        long tStart = Gpio.millis();
        long tStop = Gpio.millis();
        long dif;
        
        for (int i=0; i< 60; i++)
        {
            Thread.sleep(1000l);
            tStop = Gpio.millis();
            dif = tStop - tStart;
            
            System.out.println("Cantidad de pulsos: " + this.FlowCounter + ", Tiempo ms: " + dif);
            tStart = tStop;
        }
         */
    }

    private void OnTimerTick()
    {
        int currValue = this.FlowCounter;

        if (this.LastFlowCounterValue == currValue)
        {
            return;
        }

        float delta = this.LastFlowCounterValue - currValue;
        this.LastFlowCounterValue = currValue;
        this.EventCounter++;

        FlowSensorEventArgs args = new FlowSensorEventArgs();
        args.TotalVolume = (currValue * 1000) / this.PulsesPerLiter;
        args.DeltaVolume = (delta * 1000) / this.PulsesPerLiter;
        args.StartTimeMillis = this.StartTimeMillis;
        args.CurrentTimeMillis = Gpio.millis();
        args.EventNumber = this.EventCounter;

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
    public void Disconnect()
    {
        Gpio.wiringPiClearISR(Gpio17);
        this.NotifierWorker.cancel();
    }

    @Override
    public float GetVolume()
    {
        float volumen = 0;
        int currValue = this.FlowCounter;

        volumen = (currValue * 1000) / this.PulsesPerLiter;

        return volumen;
    }
}
