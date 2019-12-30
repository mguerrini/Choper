/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.commands;

import choper.domain.Environment;
import choper.platform.ConfigurationProvider;
import com.pi4j.wiringpi.Gpio;

/**
 *
 * @author mguerrini
 */
public class I2CCommand extends CommandBase
{
    private int GpioNumber = 1;
    private boolean Swap = false;
    
    public I2CCommand(String commandName)
    {
        super(commandName);
    }
    
    @Override
    public void Init(ICommandChannel channel)
    {
        Environment.Configure();
        this.GpioNumber = ConfigurationProvider.Instance.GetInt("Switch", "Relay", "GpioNumber");
        System.out.println("Gpio Number: " + this.GpioNumber);
        this.Swap = ConfigurationProvider.Instance.GetBool("Switch", "Relay", "Swap");
        System.out.println("Swap: " + this.Swap);

        if (Environment.IsRaspberryPiPlatform())
        {
            Gpio.pinMode(GpioNumber, Gpio.OUTPUT);
            Gpio.pullUpDnControl(GpioNumber, Gpio.PUD_UP);
        }
    }
        
    @Override
    public void Execute()
    {
    }


    @Override
    public void Reconfigure()
    {
    }

}
