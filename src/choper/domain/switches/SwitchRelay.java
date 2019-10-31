/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.switches;

import choper.domain.Environment;
import choper.platform.ConfigurationProvider;
import com.pi4j.wiringpi.*;
/**
 *
 * @author mguerrini
 */
public class SwitchRelay implements ISwitch
{
    private int GpioNumber = 1;
    
    public void Init()
    {
        Environment.Configure();
        this.GpioNumber = ConfigurationProvider.Instance.GetInt(this.getClass(), "GpioNumber");
        Gpio.pinMode(GpioNumber, Gpio.OUTPUT);
        Gpio.pullUpDnControl(GpioNumber, Gpio.PUD_UP);
    }
    
    @Override
    public boolean IsOpened()
    {
        return Gpio.digitalRead(GpioNumber) <= Gpio.LOW;
    }
    
    @Override
    public boolean IsClosed()
    {
        return Gpio.digitalRead(GpioNumber) >= Gpio.HIGH;
    }
    
    @Override
    public void Open()
    {
        Gpio.digitalWrite(GpioNumber, Gpio.LOW);
    }
    
    @Override
    public void Close()
    {
        Gpio.digitalWrite(GpioNumber, Gpio.HIGH);
    }
}
