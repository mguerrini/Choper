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
public class FlowSensorProvider
{
    public static FlowSensorProvider Instance = new FlowSensorProvider();

    private IFlowSensor SingleInstance;

    public IFlowSensor Get()
    {
        if (this.SingleInstance == null)
        {
            String cardType = ConfigurationProvider.Instance.GetString("FlowSensor", "Type");
            switch (cardType)
            {
                case "YFS201b":
                    this.SingleInstance = new FlowSensorYFS201b();
                    break;

                case "Manual":
                default:
                    this.SingleInstance = new ManualFlowSensor();
                    break;

            }
        }

        return this.SingleInstance;
    }
}
