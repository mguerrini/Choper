/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.cardReaders;

import choper.platform.events.Event;
import choper.platform.events.EventArgs;
import choper.platform.events.IEvent;

/**
 *
 * @author mguerrini
 */
public abstract class CardReaderBase implements ICardReader
{
    public IEvent<EventArgs> CardInserted;
    public IEvent<EventArgs> CardRemoved;
    public IEvent<EventArgs> BalanceChanged;

    public CardReaderBase()
    {
        this.CardInserted = new Event("SmartCardReader->CardInserted");
        this.CardRemoved = new Event("SmartCardReader->CardRemoved");
        this.BalanceChanged = new Event("SmartCardReader->BalanceChanged");
    }

    
    @Override
    public IEvent<EventArgs> GetCardInsertedEvent()
    {
        return this.CardInserted;
    }

    @Override
    public IEvent<EventArgs> GetCardRemovedEvent()
    {
        return this.CardRemoved;
    }

    @Override
    public IEvent<EventArgs> GetBalanceChangedEvent()
    {
        return this.BalanceChanged;
    }
    
    protected void RaiseCardInserted()
    {
        ((Event<EventArgs>) this.CardInserted).Invoke(this, EventArgs.Empty());
    }

    protected void RaiseCardRemoved()
    {
        ((Event<EventArgs>) this.CardRemoved).Invoke(this, EventArgs.Empty());
    }

    protected void RaiseBalanceChanged()
    {
        ((Event<EventArgs>) this.BalanceChanged).Invoke(this, EventArgs.Empty());
    }

}
