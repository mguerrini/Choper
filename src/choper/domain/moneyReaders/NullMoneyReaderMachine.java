/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.moneyReaders;

import choper.platform.events.Event;
import choper.platform.events.IEvent;

/**
 *
 * @author mguerrini
 */
public class NullMoneyReaderMachine implements IMoneyReaderMachine
{
    public IEvent<Integer> TicketReady = new Event("MoneyReaderMachine->TicketReady");
    public IEvent<Integer> TicketAccepted = new Event("MoneyReaderMachine->TicketAccepted");
    public IEvent<Integer> TicketRejected = new Event("MoneyReaderMachine->TicketRejected");
    public IEvent<MoneyReaderMachineDataReceivedEventArgs> DataReceived = new Event("MoneyReaderMachine->DataReceived");

    public IEvent<Integer> GetTicketReadyEvent()
    {
        return this.TicketReady;
    }

    public IEvent<Integer> GetTicketAcceptedEvent()
    {
        return this.TicketAccepted;
    }

    public IEvent<Integer> GetTicketRejectedEvent()
    {
        return this.TicketRejected;
    }

    public IEvent<MoneyReaderMachineDataReceivedEventArgs> GetDataReceivedEvent()
    {
        return this.DataReceived;
    }

    @Override
    public void Init()
    {
    }

    @Override
    public boolean Connect()
    {
        return true;
    }

    @Override
    public void Disconnect()
    {
    }

    @Override
    public void UpdateParameters()
    {
    }

    @Override
    public boolean Enabled()
    {
        return true;
    }

    @Override
    public boolean Disabled()
    {
        return true;
    }

    @Override
    public boolean EnableEscrow(boolean enabled)
    {
        return true;
    }

    @Override
    public boolean Accept()
    {
        return true;
    }

    @Override
    public boolean Reject()
    {
        return true;
    }

}
