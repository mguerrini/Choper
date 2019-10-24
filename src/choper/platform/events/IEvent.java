/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.platform.events;

/**
 *
 * @author max22
 */
public interface IEvent<TEventArgs>
{
    void Subscribe(IEventHandler<TEventArgs> methodReference);

    void UnSubscribe(IEventHandler<TEventArgs> methodReference);
}
