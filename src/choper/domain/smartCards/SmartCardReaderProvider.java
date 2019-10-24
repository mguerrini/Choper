/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.smartCards;

/**
 *
 * @author max22
 */
public class SmartCardReaderProvider
{
    public  static SmartCardReaderProvider Instance = new   SmartCardReaderProvider();
    
    private SmartCardReader SingleInstance;
    
    public SmartCardReader Get()
    {
        if (this.SingleInstance == null)
            this.SingleInstance = new SmartCardReader();
        
        return this.SingleInstance;
    }
}
