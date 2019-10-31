/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.smartCards;

import choper.platform.events.EventArgs;
import choper.platform.events.IEvent;

/**
 *
 * @author mguerrini
 */
public interface ISmartCardReader
{
    IEvent<EventArgs> GetCardInsertedEvent();

    IEvent<EventArgs> GetCardRemovedEvent();

    IEvent<EventArgs> GetBalanceChangedEvent();

    void Init();

    void Connect();

    void Disconnect();

    boolean IsCardPresent();

    boolean AddBalance(float amount);

    float GetBalance();

    boolean SetBalance(float amount);

    boolean SubtractBalance(float amount);
}
