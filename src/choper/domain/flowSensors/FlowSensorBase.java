/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.flowSensors;

import choper.platform.events.Event;
import choper.platform.events.IEvent;

/**
 *
 * @author mguerrini
 */
public abstract class FlowSensorBase implements IFlowSensor
{
    public IEvent<FlowSensorEventArgs> VolumeChanged = new Event<FlowSensorEventArgs>("FlowSensorYFS201b->VolumeChanged");

    @Override
    public IEvent<FlowSensorEventArgs> GetVolumeChangedEvent()
    {
        return this.VolumeChanged;
    }

    protected void RaiseVolumeChanged(FlowSensorEventArgs args)
    {
        ((Event<FlowSensorEventArgs>) this.VolumeChanged).Invoke(this, args);
    }

    protected void RaiseVolumeChangedAsync(FlowSensorEventArgs args)
    {
        ((Event<FlowSensorEventArgs>) this.VolumeChanged).InvokeAsync(this, args);
    }
}
