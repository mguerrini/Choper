/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.platform.serialComm;

import choper.platform.events.EventArgs;
import jssc.SerialPort;
import jssc.SerialPortEvent;

/**
 *
 * @author max22
 */
public class SerialChannelListenerEventArgs extends EventArgs
{
    public SerialPortEvent Data;

    public SerialPort SerialPort;
}
