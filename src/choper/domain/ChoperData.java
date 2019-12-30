/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

import choper.domain.commands.*;
import choper.domain.flowSensors.*;
import choper.domain.switches.*;
import java.util.*;

/**
 *
 * @author mguerrini
 */
public class ChoperData
{
    public ChoperStatusType Status;

    public Display Display;
    public IFlowSensor FlowSensor;
    public ISwitch SwitchFlowValve;
    
    public List<ICommand> Commands = new ArrayList<>();

    public float LiterPrice = 0; //precio del litro de cerveza
    public float PintLiterPrice = 0; //precio del litro de pinta
    public float PintSize = 0;
    public float PintPrice = 0;
    public float FreeSize = 0;
    
    public int UpdateVolumeWhenGreaterThan = 10; //cm3
    public long WriteBalanceFrequency = 1000; //milisegundos
    
    public StartSellingData StartSellingData;

    public ChoperCommandBroker CommandChannel = new ChoperCommandBroker();
    
    private Map<String, Object> Components = new HashMap<String, Object>();

    public ChoperData()
    {
        this.Status = ChoperStatusType.Initial;
    }

    public ICommand GetCommand(String name)
    {
        for (ICommand cmd : this.Commands)
        {
            if (cmd.GetName().equalsIgnoreCase(name))
            {
                return cmd;
            }
        }
        
        return null;
    }
    
    public Object GetComponent(String name)
    {
        return this.Components.get(name);
    }

    public void AddComponent(String name, Object c)
    {
        if (choper.domain.Display.class.isAssignableFrom(c.getClass()))
        {
            this.Display = (Display)c;
            return;
        }

        if (IFlowSensor.class.isAssignableFrom(c.getClass()))
        {
            this.FlowSensor = (IFlowSensor) c;
            return;
        }

        if (ISwitch.class.isAssignableFrom(c.getClass()))
        {
            this.SwitchFlowValve = (ISwitch) c;
            return;
        }
        
        this.Components.put(name, c);
    }

    public Object GetComponent(Class componentType)
    {
        for (Map.Entry<String, Object> entry : this.Components.entrySet())
        {
            if (entry.getValue().getClass().isAssignableFrom(componentType))
            {
                return entry.getValue();
            }
        }

        if (this.Display.getClass().isAssignableFrom(componentType))
        {
            return this.Display;
        }

        if (this.FlowSensor.getClass().isAssignableFrom(componentType))
        {
            return this.FlowSensor;
        }

        if (this.SwitchFlowValve.getClass().isAssignableFrom(componentType))
        {
            return this.SwitchFlowValve;
        }

        return null;
    }
}
