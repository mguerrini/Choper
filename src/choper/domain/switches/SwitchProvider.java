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
public class SwitchProvider
{
    public static SwitchProvider Instance = new SwitchProvider();

    private ISwitch SingleInstance;

    public ISwitch Get()
    {
        if (this.SingleInstance == null)
        {
            String switchType = ConfigurationProvider.Instance.GetString("Switch", "Type");
            switch (switchType)
            {
                case "Relay":
                    this.SingleInstance = new SwitchRelay();
                    break;

                case "None":
                default:
                    this.SingleInstance = new NoneSwitch();
                    break;

            }
        }

        return this.SingleInstance;
    }
}
