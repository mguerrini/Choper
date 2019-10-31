/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.flowSensors;

/**
 *
 * @author mguerrini
 */
public class FlowSensorProvider
{
    public static FlowSensorProvider Instance = new FlowSensorProvider();

    private IFlowSensor SingleInstance;

    public IFlowSensor Get()
    {
        if (this.SingleInstance == null)
        {
            this.SingleInstance = new FlowSensorYFS201b();
        }

        return this.SingleInstance;
    }
}
