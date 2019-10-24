/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.moneyReaders;

import choper.platform.serialComm.SerialChannel;
import choper.platform.events.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author max22
 */
public class MoneyReaderMachine
{
    public IEvent<Integer> TicketReady = new Event("MoneyReaderMachine->TicketReady");
    public IEvent<Integer> TicketAccepted = new Event("MoneyReaderMachine->TicketAccepted");
    public IEvent<Integer> TicketRejected = new Event("MoneyReaderMachine->TicketRejected");
    public IEvent<MoneyReaderMachineDataReceivedEventArgs> DataReceived = new Event("MoneyReaderMachine->DataReceived");

    public MoneyReaderMachine()
    {
        this.TicketCodes = new HashMap<Byte, Integer>();

        this.TicketCodes.put((byte) 1, 2);
        this.TicketCodes.put((byte) 2, 5);
        this.TicketCodes.put((byte) 3, 10);
        this.TicketCodes.put((byte) 4, 20);
        this.TicketCodes.put((byte) 5, 50);
        this.TicketCodes.put((byte) 6, 100);
        this.TicketCodes.put((byte) 7, 200);
        this.TicketCodes.put((byte) 8, 500);
    }

    private HashMap<Byte, Integer> TicketCodes;

    private SerialChannel Channel;

    public boolean IsEscrowEnabled;

    public boolean Connect()
    {
        String port = this.FindPort();
        if (port == null)
        {
            System.out.println("MoneyReaderMachine - No es posible detectar el puerto donde está conectada la BV20.");
            return false;
        }

        try
        {
            this.Channel = new SerialChannel(port);
            this.Channel.Open();

            //this.Channel.DataReceived.Subscribe(this::OnDataReceived);
            return true;
        } catch (Exception ex)
        {
            System.out.println(ex);
            return false;
        }
    }

    public void Disconnect()
    {

    }

    public String FindPort()
    {
        List<String> ports = this.GetPortNames();

        for (String p : ports)
        {
            SerialChannel ch = null;
            try
            {
                ch = new SerialChannel(p); //, 1000);
                boolean b = ch.Open();

                if (!b)
                {
                    continue;
                }

                byte[] res = ch.SendDataSync(5000, new byte[]
                {
                    (byte) 182
                }, 4);

                if (res != null && res.length == 4 && (res[3] == 0 || res[3] == 1))
                {
                    return p;
                }
            } catch (Exception ex)
            {
                System.out.println(ex);
            } finally
            {
                if (ch != null)
                {
                    ch.Close();
                }
            }
        }

        return null;
    }

    public List<String> GetPortNames()
    {
        //File folder = new File("/dev/serial");
        //try (Stream<Path> walk = Files.walk(Paths.get("/dev"))) MAC OS
        try (Stream<Path> walk = Files.walk(Paths.get("/dev/serial/by-id")))
        {
            List<String> result = walk.map(x -> x.toString()).collect(Collectors.toList());
            result.forEach(System.out::println);
            return result;

        } catch (IOException e)
        {
            System.out.println(e.getMessage());
            return new ArrayList<String>();
        }
    }

    protected int CurrentTicket;

    protected boolean IsTicketAccepted;

    public boolean Accept()
    {
        try
        {
            byte[] res = this.Channel.SendDataSync(4000, this.CreateData(172), 1);
            if (res != null && res.length > 0 && res[0] == 172)
            {
                this.IsTicketAccepted = true;
                return true;
            } else
            {
                this.IsTicketAccepted = false;
                return false;
            }
        } catch (Exception ex)
        {
            throw ex;
        }
    }

    public boolean Reject()
    {
        try
        {
            byte[] res = this.Channel.SendDataSync(2000, this.CreateData(173), 1);
            this.IsTicketAccepted = false;

            if (res != null && res.length > 0 && res[0] == 173)
            {
                return true;
            } else
            {
                return false;
            }
        } catch (Exception ex)
        {
            throw ex;
        }
    }

    public boolean EnableEscrow(boolean enabled)
    {
        try
        {
            byte[] res = null;
            if (enabled)
            {
                res = this.Channel.SendDataSync(2000, this.CreateData(170), 1);
            } else
            {
                res = this.Channel.SendDataSync(2000, this.CreateData(171), 1);
            }

            if (res != null && res.length > 0)
            {
                if (res[0] == 170)
                {
                    this.IsEscrowEnabled = true;
                } else
                {
                    this.IsEscrowEnabled = false;
                }
            }

            return this.IsEscrowEnabled;
        } catch (Exception ex)
        {
            throw ex;
        }
    }

    public byte[] GetState()
    {
        try
        {
            byte[] res = null;
            res = this.Channel.SendDataSync(2000, this.CreateData(182), 4);

            if (res != null && res.length == 4 && res[0] == 182)
            {
                return res;
            } else
            {
                return null;
            }
        } catch (Exception ex)
        {
            throw ex;
        }
    }

    private void OnDataReceived(Object source, byte[] data)
    {
        this.ProcessByteReceived(data);
    }

    private void ProcessByteReceived(byte[] dataRead)
    {
        //DataReceivedEventArgs args = new DataReceivedEventArgs() { Data = data };
        byte data = dataRead[0];

        switch (data)
        {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
                //Billete
                this.OnReceivedBill(data);
                break;
            case 20:
                //billete no reconocido
                //this.Logger.Debug("Código {0} - Billete no reconido.", data);
                this.RaiseDataReceived(dataRead, "Código: " + data + " - Billete no reconocido.");
                break;
            case 30:
                //Mecanismo en funcionamiento lento
                //this.Logger.Debug("Código {0} - Mecanismo en funcionamiento lento.", data);
                this.RaiseDataReceived(dataRead, "Código: " + data + " - Mecanismo en funcionamiento lento.");
                break;
            case 40:
                //Intento de pesca
                //this.Logger.Debug("Código {0} - Intento de pesca.", data);
                this.RaiseDataReceived(dataRead, "Código: " + data + " - Intento de pesca.");
                break;
            case 50:
                //Billete rechazado
                //this.Logger.Debug("Código {0} - Billete rechazado.", data);
                this.RaiseDataReceived(dataRead, "Código: " + data + " - Billete rechazado.");
                break;
            case 60:
                //APILADOR lleno o atascado
                //this.Logger.Debug("Código {0} - APILADOR lleno o atascado.", data);
                this.RaiseDataReceived(dataRead, "Código: " + data + " - APILADOR lleno o atascado.");
                break;
            case 70:
                //Cancelación durante escrow
                //this.Logger.Debug("Código {0} - Cancelación durante escrow.", data);
                this.RaiseDataReceived(dataRead, "Código: " + data + " - Cancelación durante escrow.");
                break;
            case 80:
                //el billete pudo haberse retirado para solucionar atasco
                //this.Logger.Debug("Código {0} - El billete pudo haberse retirado para solucionar atasco.", data);
                this.RaiseDataReceived(dataRead, "Código: " + data + " - El billete pudo haberse retirado para solucionar atasco.");
                break;
            case 120:
                //validador ocupado

                //reseteo todas las variables temporales
                //this.IsTicketAccepted = false;
                //this.Logger.Debug("Código {0} - Validador ocupado.", data);
                this.RaiseDataReceived(dataRead, "Código: " + data + " - Validador ocupado.");
                break;
            case 121:
                //validador no ocupado
                this.RaiseDataReceived(dataRead, "Código: " + data + " - Validador no ocupado.");
                //this.Logger.Debug("Código {0} - Validador no ocupado.", data);

                break;
            case (byte) 170:
                //escrow enabled
                //this.IsEscrowEnabled = true;
                //this.ResponseSync.Set();

                //this.Logger.Debug("Código {0} - Modo escrow en serie activado.", data);
                this.RaiseDataReceived(dataRead, "Código: " + data + " - Activar modo escrow en serie.");
                break;
            case (byte) 171:
                //escrow disabled
                //this.IsEscrowEnabled = false;
                //this.ResponseSync.Set();

                //this.Logger.Debug("Código {0} - Modo escrow en serie desactivado.", data);
                this.RaiseDataReceived(dataRead, "Código: " + data + " - Desactivar modo escrow en serie.");
                break;
            case (byte) 172:
                //escrow accepted
                this.IsTicketAccepted = true;
                //this.Logger.Debug("Código {0} - Escrow aceptado.", data);
                this.RaiseDataReceived(data, "Código: " + data + " - Escrow aceptado.");
                break;
            case (byte) 173:
                //escrow rechazado
                this.IsTicketAccepted = false;
                //this.Logger.Debug("Código {0} - Escrow rechazado.", data);
                this.RaiseDataReceived(dataRead, "Código: " + data + " - Escrow rechazo.");
                break;
            case (byte) 182:
                //resultado de la consulta del estado, son 4 bytes
                //estado....los siguientes 3 bytes son del estado.
                //this.ProcessState(data);
                this.RaiseDataReceived(dataRead, "Código: " + data + " - Status:" + dataRead[1] + " " + dataRead[2] + " " + dataRead[2]);
                break;
            case (byte) 255:
                //error de comando
                //this.Logger.Debug("Código {0} - Error de comando.", data);
                this.RaiseDataReceived(dataRead, "Código: " + data + " - Error de comando.");
                break;
            default:
                //this.Logger.Debug("Código {0} - Código desconocido.", data);
                this.RaiseDataReceived(dataRead, "Código: " + data + " - Código desconocido.");
                break;
        }
    }

    private void OnReceivedBill(byte value)
    {
        //this.Logger.Debug("Código de billete: {0}", value);
        if (!this.TicketCodes.containsKey(value))
        {
            //lo rechazo
            //this.Logger.Debug("Código de billete: {0} - Rechazado", value);
            this.Reject();

            this.RaiseDataReceived(value, "Código: " + value + " - Billete rechazado. No existe valor asociado al código.");
            return;
        }

        this.CurrentTicket = this.TicketCodes.get(value);

        //this.Logger.Debug("Billete leído: $ {0}", this.CurrentTicket);
        if (!this.IsEscrowEnabled)
        {
            this.RaiseTicketReady(this.CurrentTicket);
            this.RaiseTicketAccepted(this.CurrentTicket);
        } else
        {
            if (this.IsTicketAccepted)
            {
                this.IsTicketAccepted = false;
                this.RaiseTicketAccepted(this.CurrentTicket);
            } else
            {
                this.RaiseTicketReady(this.CurrentTicket);
            }
        }

        this.RaiseDataReceived(value, "Código: " + value + " - Billete ingresado $ " + this.CurrentTicket);
    }

    protected void RaiseTicketRejected(int number)
    {
        ((Event) this.TicketRejected).Invoke(this, number);
    }

    protected void RaiseTicketReady(int number)
    {
        ((Event) this.TicketReady).Invoke(this, number);
    }

    protected void RaiseTicketAccepted(int number)
    {
        ((Event) this.TicketAccepted).Invoke(this, number);
    }

    private void RaiseDataReceived(byte data, String description)
    {
        byte[] d = new byte[] { data };
        
        this.RaiseDataReceived(d, description);
    }

    private void RaiseDataReceived(byte[] data, String description)
    {
        MoneyReaderMachineDataReceivedEventArgs args = new MoneyReaderMachineDataReceivedEventArgs();
        args.Data = data;
        args.Description = description;
        ((Event) this.DataReceived).Invoke(this, args);
    }

    private byte[] CreateData(int... data)
    {
        byte[] output = new byte[data.length];

        for (int i = 0; i < data.length; i++)
        {
            output[i] = (byte) data[i];
        }
        return output;
    }
}
