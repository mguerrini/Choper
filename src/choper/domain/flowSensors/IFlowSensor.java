/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.flowSensors;

import choper.domain.flowSensors.FlowSensorEventArgs;
import choper.platform.events.IEvent;

/**
 *
 * @author mguerrini
 */
public interface IFlowSensor
{
    IEvent<FlowSensorEventArgs> GetVolumeChangedEvent();

    void Init();

    void Connect();

    void Disconnect();

    void UpdateParameters();

    float GetVolume();

}
