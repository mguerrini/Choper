/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.moneyReaders;

import choper.platform.events.EventArgs;

/**
 *
 * @author mguerrini
 */
public class MoneyReaderMachineDataReceivedEventArgs extends EventArgs
{
    public String Description;
    
    public byte[] Data;
}
