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
    private boolean IsOpened = true;
    private boolean Swap = false;

    public void Init()
    {
        Environment.Configure();
        this.GpioNumber = ConfigurationProvider.Instance.GetInt(this.getClass(), "GpioNumber");
        System.out.println("Gpio Number: " + this.GpioNumber);
        this.Swap = ConfigurationProvider.Instance.GetBool(this.getClass(), "Swap");
        System.out.println("Swap: " + this.Swap);

        if (Environment.IsRaspberryPiPlatform())
        {
            Gpio.pinMode(GpioNumber, Gpio.OUTPUT);
            Gpio.pullUpDnControl(GpioNumber, Gpio.PUD_UP);
        }
    }

    @Override
    public void UpdateParameters()
    {
    }

    @Override
    public boolean IsOpened()
    {
        if (Environment.IsRaspberryPiPlatform())
        {
            if (this.Swap)
            {
                return Gpio.digitalRead(GpioNumber) <= Gpio.LOW ;
            }
            else
            {
                return Gpio.digitalRead(GpioNumber) <= Gpio.HIGH;
            }
        }

        return IsOpened;
    }

    @Override
    public boolean IsClosed()
    {
        if (Environment.IsRaspberryPiPlatform())
        {
            if (this.Swap)
            {
                return Gpio.digitalRead(GpioNumber) >= Gpio.HIGH;
            }
            else
            {
                return Gpio.digitalRead(GpioNumber) >= Gpio.LOW;
            }
        }

        return !this.IsOpened;
    }

    @Override
    public void OpenContacts()
    {
        if (Environment.IsRaspberryPiPlatform())
        {
            if (this.Swap)
            {
                Gpio.digitalWrite(GpioNumber, Gpio.LOW);
            }
            else
            {
                Gpio.digitalWrite(GpioNumber, Gpio.HIGH);
            }
            return;
        }

        this.IsOpened = true;
    }

    @Override
    public void CloseContacts()
    {
        if (Environment.IsRaspberryPiPlatform())
        {
            if (this.Swap)
                Gpio.digitalWrite(GpioNumber, Gpio.HIGH);
            else
                Gpio.digitalWrite(GpioNumber, Gpio.LOW);
        }

        this.IsOpened = false;
    }

}
