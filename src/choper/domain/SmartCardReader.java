/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

import choper.platform.events.*;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.*;

/**
 *
 * @author max22
 */
public class SmartCardReader
{
    public IEvent<EventArgs> CardInserted;
    public IEvent<EventArgs> CardRemoved;
    public IEvent<EventArgs> BalanceChanged;

    public SmartCardReader()
    {
        this.CardInserted = new Event("SmartCardReader->CardInserted");
        this.CardRemoved = new Event("SmartCardReader->CardRemoved");
        this.BalanceChanged = new Event("SmartCardReader->BalanceChanged");

        this.Worker = new Thread(() -> this.Run());
    }

    private Thread Worker;

    private boolean IsStarted = false;
    private boolean StopRequest = false;
    private boolean IsCardPresent = false;
    private Card ActiveCard;
    private CardTerminal ActiveTerminal;

    // <editor-fold defaultstate="collapsed" desc="-- Open / Close --">
    public void Start()
    {
        if (this.IsStarted)
        {
            return;
        }

        this.StopRequest = false;
        this.IsStarted = true;
        this.Worker.start();
    }

    public void Stop()
    {
        if (!this.IsStarted)
        {
            return;
        }

        this.StopRequest = true;
        try
        {
            this.Worker.join(15000);
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(SmartCardReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // </editor-fold>
    private void Run()
    {
        TerminalFactory factory = TerminalFactory.getDefault();
        CardTerminals terminals = factory.terminals();

        while (!this.StopRequest)
        {
            try
            {
                boolean changed = terminals.waitForChange(10000);
                if (!changed)
                {
                    continue;
                }

                List<CardTerminal> listTerms = terminals.list();

                if (listTerms.size() == 0)
                {
                    System.out.println("No hay Lectoras de tarjetas conectadas...");
                    continue;
                }

                //obtengo la lectora
                CardTerminal terminal = listTerms.get(0);

                if (this.IsCardPresent == terminal.isCardPresent())
                {
                    continue;
                }

                this.IsCardPresent = terminal.isCardPresent();

                if (this.IsCardPresent)
                {
                    this.OnCardInserted(terminal);
                }
                else
                {
                    this.OnCardRemoved(terminal, this.ActiveCard);
                }
            }
            catch (CardException ex)
            {
                Logger.getLogger(SmartCardReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void OnCardInserted(CardTerminal terminal)
    {
        try
        {
            System.out.println("Tarjeta insertada: " + terminal.getName());

            //me conecto
            this.ActiveCard = terminal.connect("*");
            ATR atr = this.ActiveCard.getATR();
            byte[] atrb = atr.getBytes();

            System.out.print("ATR ");
            for (byte b : atrb)
            {
                System.out.print(String.format("%02X ", b));
            }
            System.out.println();

            this.ActiveTerminal = terminal;

            //CommandAPDU cmd = new CommandAPDU(255, 164, 0, 0, 6);
            //byte[] cmdData = new byte[]{Integer.parseInt("FF", 16), 0xA4, 0x00, 0x00,  0x01, 0x06};
            //CommandAPDU cmd = new CommandAPDU(0xFF, 0xA4, 0x00, 0x00, 0x06, 0x01);
            
            //byte[] b =cmd.getBytes();
            CommandAPDU cmd = new CommandAPDU(
                    Integer.parseUnsignedInt("FF", 16),
                    Integer.parseUnsignedInt("A4", 16),
                    Integer.parseUnsignedInt("00", 16),
                    Integer.parseUnsignedInt("00", 16),
                    new byte[] {Byte.parseByte("06", 16)},
                    Integer.parseUnsignedInt("01", 16)
            );

            System.out.println("Selecting Card Type - Cmd: FF A4 00 00 06");
            ResponseAPDU res = this.ActiveCard.getBasicChannel().transmit(cmd);
            System.out.println("Selecting Card Type - SW1: " + res.getSW1() + ", SW2: " + res.getSW2());
            ((Event<EventArgs>) this.CardInserted).Invoke(this, EventArgs.Empty());
        }
        catch (Exception ex)
        {
            Logger.getLogger(SmartCardReader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void OnCardRemoved(CardTerminal terminal, Card activeCard)
    {
        try
        {
            System.out.println("Tarjeta removida: " + terminal.getName());
            activeCard.disconnect(true);

            ((Event<EventArgs>) this.CardInserted).Invoke(this, EventArgs.Empty());
        }
        catch (Exception ex)
        {
            Logger.getLogger(SmartCardReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Card GetActiveCard()
    {
        return this.ActiveCard;
    }

    public boolean SetBalance(float amount)
    {
        boolean pin = this.VerifyPIN("FF", "FF", "FF");
        if (!pin)
        {
            return false;
        }

        Card card = this.ActiveCard;
        CardTerminal terminal = this.ActiveTerminal;

        try
        {
            if (card == null || terminal == null || !terminal.isCardPresent())
            {
                System.out.println("No es posible grabar el saldo. Card == null || terminal == null || tarjeta no presente");
                return false;
            }

            //POSICION DE MEMORIA 32, 5 BYTES
            byte[] value = this.ConvertToBytes(amount);
            CommandAPDU cmd = new CommandAPDU(
                    Integer.parseUnsignedInt("FF", 16),
                    Integer.parseUnsignedInt("D0", 16),
                    Integer.parseUnsignedInt("00", 16),
                    Integer.parseUnsignedInt("20", 16),
                    value);

            System.out.println("Writing Card - Cmd: FF D0 00 20 " + value);
            ResponseAPDU res = this.ActiveCard.getBasicChannel().transmit(cmd);
            System.out.println("Writing Card  - SW1: " + res.getSW1() + ", SW2: " + res.getSW2());

            return true;
        }
        catch (CardException ex)
        {
            System.out.println("No es posible grabar el saldo - Error: " + ex.getMessage());
            Logger.getLogger(SmartCardReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean AddBalance(float amount)
    {
        try
        {
            float val = this.GetBalance();
            val = val + amount;
            return this.SetBalance(val);
        }
        catch (Exception ex)
        {
            Logger.getLogger(SmartCardReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public float GetBalance() throws Exception
    {
        boolean pin = this.VerifyPIN("FF", "FF", "FF");
        if (!pin)
        {
            return 0;
        }

        try
        {
            //POSICION DE MEMORIA 32, 5 BYTES
            CommandAPDU cmd = new CommandAPDU(
                    Integer.parseUnsignedInt("FF", 16),
                    Integer.parseUnsignedInt("B0", 16),
                    Integer.parseUnsignedInt("00", 16),
                    Integer.parseUnsignedInt("20", 16),
                    5);

            System.out.println("Reading Card - Cmd: FF B0 00 20 05");
            ResponseAPDU res = this.ActiveCard.getBasicChannel().transmit(cmd);
            System.out.println("Reading Card  - SW1: " + res.getSW1() + ", SW2: " + res.getSW2());

            if (res.getSW1() == 144 && res.getSW2() == 0)
            {
                byte[] data = res.getData();
                return this.ConvertToFloat(data);
            }

            throw new Exception("Se produjo un error al leer la tarjeta");
        }
        catch (CardException ex)
        {
            Logger.getLogger(SmartCardReader.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception("Se produjo un error al leer la tarjeta", ex);
        }
    }

    private byte[] ConvertToBytes(float value)
    {
        //4 bytes parte entera 1 bytes parte decimal
        int intPart = (int) value;

        double aux = value - intPart;

        aux = aux * 100;
        int decPart = (int) aux;

        int valueIntPart = intPart;
        byte valueDecPart = (byte) decPart;

        byte[] output = ByteBuffer.allocate(5).putInt(valueIntPart).put(valueDecPart).array();
        return output;
    }

    private float ConvertToFloat(byte[] bytes)
    {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int intPart = buffer.getInt();
        byte decPart = buffer.get();
        long val = intPart * 100;
        val = val + decPart;

        float output = (float) val / 100;

        return output;
    }

    protected boolean VerifyPIN(String p1, String p2, String p3)
    {

        byte[] pins = new byte[3];
        pins[0] = (byte) Integer.parseUnsignedInt(p1, 16);
        pins[1] = (byte) Integer.parseUnsignedInt(p2, 16);
        pins[2] = (byte) Integer.parseUnsignedInt(p2, 16);

        try
        {
            CommandAPDU cmd = new CommandAPDU(
                    Integer.parseUnsignedInt("FF", 16),
                    Integer.parseUnsignedInt("20", 16),
                    Integer.parseUnsignedInt("00", 16),
                    Integer.parseUnsignedInt("00", 16),
                    pins);

            System.out.println("Reading Card - Cmd: FF B0 00 20 05");
            ResponseAPDU res = this.ActiveCard.getBasicChannel().transmit(cmd);
            System.out.println("Reading Card  - SW1: " + res.getSW1() + ", SW2: " + res.getSW2());

            return res.getSW1() == 144 && res.getSW2() == 7;
        }
        catch (CardException ex)
        {
            Logger.getLogger(SmartCardReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
