/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.platform.serialComm;

import choper.platform.events.*;
import choper.platform.threading.TaskQueue;

import java.util.ArrayList;
import java.util.List;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

/**
 *
 * @author max22
 */
public class SerialChannel
{

    public IEvent<byte[]> DataReceived;

    private SerialChannelListener Listener;
    //private Serial Serial;

    public SerialChannel(String portNumber)
    {
        this.PortNumber = portNumber;
        this.WriteTimeout = 0;

        this.WaitingResponse = new ArrayList<>();
        this.CurrentText = new ArrayList<>();

        this.DataReceived = new Event("SerialChannel->DataReceived");
    }
    /*
    public SerialChannel(String portNumber, int writeTimeout)
    {
        this.PortNumber = portNumber;
        this.WriteTimeout = writeTimeout;

        this.Listener.DataReceived.Subscribe((a, b) -> this.OnDataReceived(a, b));
        this.WaitingResponse = new ArrayList<>();
        this.CurrentText = new ArrayList<>();

        this.DataReceived = new Event();
    }

    public SerialChannel(String portNumber, Baud baudRate, StopBits stopBits)
    {
        this.PortNumber = portNumber;
        this.BaudRate = baudRate;
        this.StopBitsCount = stopBits;
        this.WriteTimeout = 0;

        this.Listener.DataReceived.Subscribe((a, b) -> this.OnDataReceived(a, b));
        this.WaitingResponse = new ArrayList<>();
        this.CurrentText = new ArrayList<>();

        this.DataReceived = new Event();
    }

    public SerialChannel(String portNumber, Baud baudRate, StopBits stopBits, int writeTimeout)
    {
        this.PortNumber = portNumber;
        this.BaudRate = baudRate;
        this.StopBitsCount = stopBits;
        this.WriteTimeout = writeTimeout;

        this.Listener.DataReceived.Subscribe((a, b) -> this.OnDataReceived(a, b));
        this.WaitingResponse = new ArrayList<>();
        this.CurrentText = new ArrayList<>();

        this.DataReceived = new Event();
    }
     */

    private int WriteTimeout;
    private String PortNumber;
    private int BaudRate = 9600;
    private int StopBitsCount = 1;

    private SerialPort Serial;
    private List<SerialData> WaitingResponse;
    private List<Byte> CurrentText;
    private Object ResponseLocker = new Object();

    private TaskQueue ProcessTask = new TaskQueue(this::ProcessDataReceived);
    private TaskQueue NotifyTask = new TaskQueue(this::NotifyDataReceived);

    public Boolean Open()
    {
        if (this.Serial != null)
        {
            return true;
        }

        System.out.println("Serial port: " + this.PortNumber + " opening.");

        SerialPort serialPort = new SerialPort(this.PortNumber);
        try
        {
            serialPort.openPort();//Open port
            serialPort.setParams(this.BaudRate, 8, this.StopBitsCount, 0);//Set params
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask

            serialPort.setEventsMask(mask);//Set mask
            this.Listener = new SerialChannelListener(serialPort);
            this.Listener.DataReceived.Subscribe(this::OnDataReceived);

            serialPort.addEventListener(this.Listener);//Add SerialPortEventListener

            this.Serial = serialPort;

            System.out.println("Serial port: " + this.PortNumber + " opened.");

            return true;
        }
        catch (SerialPortException ex)
        {
            if (this.Listener != null)
            {
                this.Listener.DataReceived.UnSubscribe(this::OnDataReceived);
            }

            System.out.println(ex);
            return false;
        }
    }

    public void Close()
    {
        if (this.Serial != null)
        {
            try
            {
                this.Serial.closePort();
            }
            catch (Exception e)
            {
            }

            this.Listener.DataReceived.UnSubscribe(this::OnDataReceived);
            this.Serial = null;
        }
    }

    /// <summary>
    /// Envia el comando y espera el ACK del dispositvo
    /// </summary>
    /// <param name="cmd"></param>
    /// <returns></returns>
    public byte[] SendDataSync(byte[] data)
    {
        return this.SendDataSync(3000, data);
    }

    public byte[] SendDataSync(byte[] data, int responseLen)
    {
        if (data == null || data.length <= 0)
        {
            return null;
        }

        SerialData sc = new SerialData();
        sc.Data = data;
        sc.ResponseLength = responseLen;
        sc.Channel = this.Serial;

        sc = this.DoSendDataSync(sc, 3000);

        if (sc.IsTimeout)
        {
            return null;
        }

        return sc.Response;
    }

    public byte[] SendDataSync(int responseTimeout, byte[] data)
    {
        if (data == null || data.length <= 0)
        {
            return null;
        }

        SerialData sc = new SerialData();
        sc.Data = data;
        sc.ResponseLength = -1;
        sc.Channel = this.Serial;

        sc = this.DoSendDataSync(sc, responseTimeout);

        if (sc.IsTimeout)
        {
            return null;
        }

        return sc.Response;
    }

    public byte[] SendDataSync(int responseTimeout, byte[] data, int responseLen)
    {
        if (data == null || data.length <= 0)
        {
            return null;
        }

        SerialData sc = new SerialData();
        sc.Data = data;
        sc.ResponseLength = responseLen;
        sc.Channel = this.Serial;

        sc = this.DoSendDataSync(sc, responseTimeout);

        if (sc.IsTimeout)
        {
            return null;
        }

        return sc.Response;
    }

    public void SendData(byte[] data)
    {
        try
        {
            if (data != null && data.length > 0)
            {
                this.Serial.writeBytes(data);
            }
        }
        catch (Exception ex)
        {
            System.out.println(ex);

        }
    }

    private SerialData DoSendDataSync(SerialData sc, int responseTimeout)
    {
        synchronized (this.WaitingResponse)
        {
            this.WaitingResponse.add(sc);
        }

        //envio la orden
        try
        {
            this.Serial.writeBytes(sc.Data);
        }
        catch (Exception ex)
        {
            System.out.println(ex);
            sc.Error = ex;
            return sc;
        }

        long lResTime = Integer.toUnsignedLong(responseTimeout);

        //espero la respuesta
        try
        {
            synchronized (this.ResponseLocker)
            {
                this.ResponseLocker.wait(60000);
                if (sc.Response == null || (sc.ResponseLength > 0 && sc.Response.length != sc.ResponseLength))
                {
                    sc.IsTimeout = true;
                }
            }
        }
        catch (InterruptedException e)
        {
            sc.IsTimeout = true;
        }

        synchronized (this.WaitingResponse)
        {
            this.WaitingResponse.remove(sc);
        }

        return sc;
    }

    private void DoSendDataAsync(SerialData sc)
    {
        if (sc == null)
        {
            return;
        }

        try
        {
            if (sc.Data != null && sc.Data.length > 0)
            {
                this.Serial.writeBytes(sc.Data);
            }
        }
        catch (Exception ex)
        {
            System.out.println(ex);
            sc.Error = ex;
        }
    }

    private void OnDataReceived(Object sender, SerialChannelListenerEventArgs args)
    {
        SerialPortEvent event = args.Data;

        if (event == null)
        {
            return;
        }

        if (event.isRXCHAR())
        {
            //If data is available
            int count = event.getEventValue();
            try
            {
                byte buffer[] = args.SerialPort.readBytes(count, 2000);
                this.ProcessTask.Enqueue(buffer);

            }
            catch (SerialPortException ex)
            {
                System.out.println(ex);
            }
            catch (SerialPortTimeoutException tex)
            {
                System.out.println(tex);
            }
        }
        else if (event.isCTS())
        {
            //If CTS line has changed state
            if (event.getEventValue() == 1)
            {//If line is ON
                System.out.println("CTS - ON");
            }
            else
            {
                System.out.println("CTS - OFF");
            }
        }
        else if (event.isDSR())
        {
            ///If DSR line has changed state
            if (event.getEventValue() == 1)
            {//If line is ON
                System.out.println("DSR - ON");
            }
            else
            {
                System.out.println("DSR - OFF");
            }
        }
    }

    private void ProcessDataReceived(Object context, Object data)
    {
        if (data == null)
        {
            return;
        }

        byte[] bytesRead = (byte[]) data;

        if (bytesRead == null || bytesRead.length == 0)
        {
            return;
        }

        SerialData sd = null;

        synchronized (this.WaitingResponse)
        {
            if (!this.WaitingResponse.isEmpty())
            {
                //se supone que la primer respuesta corresponde a la primer consulta y si tardo mucho....no se sabe
                sd = this.WaitingResponse.get(0);
            }
        }

        if (sd != null)
        {
            if (sd.ResponseLength > 0)
            {
                for (int i = 0; i < bytesRead.length; i++)
                {
                    this.CurrentText.add(bytesRead[i]);
                }

                if (this.CurrentText.size() >= sd.ResponseLength)
                {
                    sd.Response = new byte[sd.ResponseLength];

                    for (int i = 0; i < sd.ResponseLength; i++)
                    {
                        sd.Response[i] = this.CurrentText.get(0);
                        this.CurrentText.remove(0);
                    }

                    synchronized (this.WaitingResponse)
                    {
                        //se completo la llamada
                        this.WaitingResponse.remove(0);
                    }

                    synchronized (this.ResponseLocker)
                    {
                        this.ResponseLocker.notify();
                    }

                    return;
                }
                else
                {
                    return;
                }
            }
            else
            {
                this.CurrentText.clear();
                sd.Data = new byte[]
                {
                    bytesRead[0]
                };

                synchronized (this.ResponseLocker)
                {
                    this.ResponseLocker.notify();
                }

                if (bytesRead.length > 1)
                {
                    byte[] newRead = new byte[bytesRead.length - 1];
                    for (int i = 1; i < bytesRead.length; i++)
                    {
                        newRead[i - 1] = bytesRead[i];
                    }

                    this.RaiseDataReceived(bytesRead);
                }
            }
        }
        else
        {
            this.CurrentText.clear();
            for (int i = 0; i < bytesRead.length; i++)
            {
                this.RaiseDataReceived(new byte[]
                {
                    bytesRead[i]
                });
            }
        }

    }

    private void RaiseDataReceived(byte[] receivedDataBytes)
    {
        //el listener es asincronico
        this.NotifyTask.Enqueue(receivedDataBytes);
    }

    private void NotifyDataReceived(Object context, Object data)
    {

        if (data == null)
        {
            return;
        }

        byte[] bytesRead = (byte[]) data;

        if (bytesRead == null || bytesRead.length == 0)
        {
            return;
        }

        //el listener es asincronico
        ((Event) this.DataReceived).Invoke(this, bytesRead);
    }

}
