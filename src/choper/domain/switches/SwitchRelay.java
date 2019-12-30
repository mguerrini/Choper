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
    private boolean _isOpened = true;
    private boolean Swap = false;
    private boolean _isLocked = false;

    public void Init()
    {
        Environment.Configure();
        this.GpioNumber = ConfigurationProvider.Instance.GetInt("Switch", "Relay", "GpioNumber");
        System.out.println("Gpio Number: " + this.GpioNumber);
        this.Swap = ConfigurationProvider.Instance.GetBool("Switch", "Relay", "Swap");
        System.out.println("Swap: " + this.Swap);

        this.Unlock();

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
                return Gpio.digitalRead(GpioNumber) <= Gpio.LOW;
            }
            else
            {
                return Gpio.digitalRead(GpioNumber) <= Gpio.HIGH;
            }
        }

        return _isOpened;
    }

    @Override
    public boolean IsLocked()
    {
        return _isLocked;
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

        return !this._isOpened;
    }

    @Override
    public void OpenContacts()
    {
        if (this.IsLocked())
        {
            System.out.println("Switch - Locked");
            if (this.IsOpened())
            {
                System.out.println("Switch - Opened");
            }
            else
            {
                System.out.println("Switch - Closed");
            }

            return;
        }

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

        this._isOpened = true;
        System.out.println("Switch - Opened");
    }

    @Override
    public void CloseContacts()
    {
        if (this.IsLocked())
        {
            System.out.println("Switch - Locked");
            if (this.IsOpened())
            {
                System.out.println("Switch - Opened");
            }
            else
            {
                System.out.println("Switch - Closed");
            }

            return;
        }

        if (Environment.IsRaspberryPiPlatform())
        {
            if (this.Swap)
            {
                Gpio.digitalWrite(GpioNumber, Gpio.HIGH);
            }
            else
            {
                Gpio.digitalWrite(GpioNumber, Gpio.LOW);
            }
        }

        this._isOpened = false;
        System.out.println("Switch - Closed");
    }

    @Override
    public void Lock()
    {
        this._isLocked = true;
        System.out.println("Switch - Locked");
    }

    @Override
    public void Unlock()
    {
        _isLocked = false;
        System.out.println("Switch - Unlocked");
    }

}
