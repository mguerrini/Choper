/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

/**
 *
 * @author max22
 */
public class MoneyReaderMachineProvider
{
    public  static MoneyReaderMachineProvider Instance = new   MoneyReaderMachineProvider();
    
    private MoneyReaderMachine SingleInstance;
    
    public MoneyReaderMachine Get()
    {
        if (this.SingleInstance == null)
            this.SingleInstance = new MoneyReaderMachine();
        
        return this.SingleInstance;
    }
}
