/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.switches;

/**
 *
 * @author mguerrini
 */
public class SwitchProvider
{
    public static SwitchProvider Instance = new SwitchProvider();

    private ISwitch SingleInstance;

    public ISwitch Get()
    {
        if (this.SingleInstance == null)
        {
            this.SingleInstance = new SwitchRelay();
        }

        return this.SingleInstance;
    }
}
