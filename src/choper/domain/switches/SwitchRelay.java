/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.switches;

import choper.platform.ConfigurationProvider;

/**
 *
 * @author mguerrini
 */
public class SwitchRelay implements ISwitch
{
    private int Gpio_27 = 1;
    
    public void Init()
    {
        this.Gpio_27 = ConfigurationProvider.Instance.GetInt(this.getClass(), "GpioNumber");
    }
    
    @Override
    public boolean IsOpened()
    {
        return false;
    }
    
    @Override
    public boolean IsClosed()
    {
        return false;        
    }
    
    @Override
    public void Open()
    {
        
    }
    
    @Override
    public void Close()
    {
        
    }
}
