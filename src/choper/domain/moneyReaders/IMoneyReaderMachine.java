/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.moneyReaders;

import choper.domain.moneyReaders.MoneyReaderMachineDataReceivedEventArgs;
import choper.platform.events.IEvent;

/**
 *
 * @author mguerrini
 */
public interface IMoneyReaderMachine
{
    IEvent<Integer> GetTicketReadyEvent();

    IEvent<Integer> GetTicketAcceptedEvent();

    IEvent<Integer> GetTicketRejectedEvent();

    IEvent<MoneyReaderMachineDataReceivedEventArgs> GetDataReceivedEvent();

    void Init();

    boolean Connect();

    void Disconnect();

    boolean Enabled();

    boolean Disabled();

    boolean EnableEscrow(boolean enabled);

    boolean Accept();

    boolean Reject();

}
