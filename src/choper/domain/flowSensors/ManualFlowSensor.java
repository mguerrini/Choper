/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.flowSensors;

import choper.platform.ConfigurationProvider;

/**
 *
 * @author mguerrini
 */
public class ManualFlowSensor extends FlowSensorBase
{
    private float _pulsesPerLiter = 450;

    private int _eventCounter = 0;
    private long _startTimeMillis;
    private int _flowCounter;
    private int _lastFlowCounterValue;
    private boolean _raiseEventAsync;

    @Override
    public void Init()
    {
        this.UpdateParameters();
    }

    @Override
    public void Connect()
    {
        this.Reset();
        _startTimeMillis = System.currentTimeMillis();
    }

    @Override
    public void Disconnect()
    {
    }

    @Override
    public void SetCalibrationFactor(int factor)
    {
        _pulsesPerLiter = factor;
    }

    @Override
    public void UpdateParameters()
    {
        _pulsesPerLiter = ConfigurationProvider.Instance.GetInt("FlowSensor", "PulsesPerLiter");
        System.out.println("PulsesPerLiter: " + _pulsesPerLiter);
        
        _raiseEventAsync = ConfigurationProvider.Instance.GetBool("FlowSensor", "RaiseEventAsync");
        System.out.println("Raise Event Async: " + _raiseEventAsync);
    }

    @Override
    public float GetVolume()
    {
        return 0f;
    }

    @Override
    public void Reset()
    {
        _eventCounter = 0;
        _flowCounter = 0;
        _lastFlowCounterValue = 0;
    }

    public void UpdatePulses(int pulses)
    {
        _flowCounter = pulses;
        this.OnPulseChanged();
    }

    public void IncrementPulses(int pulses)
    {
        _flowCounter = _flowCounter + pulses;
        this.OnPulseChanged();
    }

    private void OnPulseChanged()
    {
        int currValue = _flowCounter;

        float delta = currValue - _lastFlowCounterValue;
        _lastFlowCounterValue = currValue;
        _eventCounter++;

        FlowSensorEventArgs args = new FlowSensorEventArgs();
        args.Pulses = currValue;
        args.TotalVolume = (currValue * 1000) / _pulsesPerLiter;
        args.DeltaVolume = (delta * 1000) / _pulsesPerLiter;
        args.StartTimeMillis = _startTimeMillis;
        args.CurrentTimeMillis = System.currentTimeMillis();

        args.EventNumber = _eventCounter;

        /*
        Logger.getGlobal().info("Flow Sensor - EventNumber = " + args.EventNumber);
        Logger.getGlobal().info("Flow Sensor - Counter Value = " + currValue);
        Logger.getGlobal().info("Flow Sensor - Total Volume = " + args.TotalVolume);
        Logger.getGlobal().info("Flow Sensor - Delta Volume = " + args.DeltaVolume);
        Logger.getGlobal().info("Flow Sensor - StartTimeMillis = " + args.StartTimeMillis);
        Logger.getGlobal().info("Flow Sensor - CurrentTimeMillis = " + args.CurrentTimeMillis);
         */
        if (_raiseEventAsync)
        {
            this.RaiseVolumeChangedAsync(args);
        }
        else
        {
            this.RaiseVolumeChanged(args);
        }    
    }

    @Override
    public void Dispose()
    {
    }
}
