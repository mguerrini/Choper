/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.platform.serialComm;

import choper.platform.events.*;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;

/**
 *
 * @author max22
 */
public class SerialChannelListener implements SerialPortEventListener
{
    public IEvent<SerialChannelListenerEventArgs> DataReceived;
    private SerialPort Serial;

    public SerialChannelListener()
    {
        this.DataReceived = new Event("SerialChannelListener->DataReceived");
    }

    public SerialChannelListener(SerialPort port)
    {
        this.DataReceived = new Event("SerialChannelListener->DataReceived");
        this.Serial = port;
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent)
    {
        SerialChannelListenerEventArgs args = new SerialChannelListenerEventArgs();
        args.Data = serialPortEvent;
        args.SerialPort = this.Serial;
        ((Event) this.DataReceived).Invoke(this, args);
    }
}
