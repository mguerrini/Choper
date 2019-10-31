/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.moneyReaders;

/**
 *
 * @author max22
 */
public class MoneyReaderMachineProvider
{
    public  static MoneyReaderMachineProvider Instance = new   MoneyReaderMachineProvider();
    
    private IMoneyReaderMachine SingleInstance;
    
    public IMoneyReaderMachine Get()
    {
        if (this.SingleInstance == null)
            this.SingleInstance = new MoneyReaderMachine();
        
        return this.SingleInstance;
    }
}
