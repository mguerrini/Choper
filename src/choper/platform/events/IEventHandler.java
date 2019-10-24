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
public interface IEventHandler<TEventArgs> {

    void invoke(Object source, TEventArgs eventArgs);
}
