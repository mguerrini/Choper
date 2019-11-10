/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.moneyReaders;

import choper.platform.ConfigurationProvider;

/**
 *
 * @author max22
 */
public class MoneyReaderMachineProvider
{
    public static MoneyReaderMachineProvider Instance = new MoneyReaderMachineProvider();

    private IMoneyReaderMachine SingleInstance;

    public IMoneyReaderMachine Get()
    {
        if (this.SingleInstance == null)
        {
            boolean enabled = ConfigurationProvider.Instance.GetBool("MoneyReaderMachine", "Enabled");
            if (enabled)
            {
                this.SingleInstance = new MoneyReaderMachine();
            }
            else
            {
                this.SingleInstance = new NullMoneyReaderMachine();
            }
        }

        return this.SingleInstance;
    }
}
