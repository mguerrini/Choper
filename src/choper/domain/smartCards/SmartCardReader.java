/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 0* and open the template in the editor.
 */
package choper.domain.smartCards;

import choper.platform.events.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.*;

/**
 *
 * @author max22
 */
public class SmartCardReader implements ISmartCardReader
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
    private Float CurrentBalance;

    public void Init()
    {
        
    }
    
    // <editor-fold defaultstate="collapsed" desc="-- Open / Close --">
    @Override
    public void Connect()
    {
        if (this.IsStarted)
        {
            return;
        }

        this.StopRequest = false;
        this.IsStarted = true;
        this.Worker.start();
    }

    @Override
    public void Disconnect()
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
        boolean isFirstTime = true;
        boolean changed = false;

        while (!this.StopRequest)
        {
            try
            {
                if (!isFirstTime)
                {
                    Thread.sleep(500);
                }

                List<CardTerminal> listTerms = terminals.list();

                if (listTerms.size() == 0)
                {
                    System.out.println("No hay Lectoras de tarjetas conectadas...");
                }
                else
                {
                    //obtengo la lectora
                    CardTerminal terminal = listTerms.get(0);

                    if (this.IsCardPresent != terminal.isCardPresent() || isFirstTime)
                    {
                        this.IsCardPresent = terminal.isCardPresent();

                        if (this.IsCardPresent)
                        {
                            this.OnCardInserted(terminal);
                        }
                        else
                        {
                            if (!isFirstTime)
                            {
                                this.OnCardRemoved(terminal, this.ActiveCard);
                            }
                        }
                    }

                    isFirstTime = false;
                }
            }
            catch (CardException ex)
            {
                Logger.getLogger(SmartCardReader.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (InterruptedException ex)
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

            String atrStr = "";
            for (byte b : atrb)
            {
                atrStr += String.format("%02X ", b);
            }

            System.out.println("ATR " + atrStr);

            this.ActiveTerminal = terminal;

            //Comando de seleccion de tarjeta
            CommandAPDU cmd = new CommandAPDU(
                    Integer.parseUnsignedInt("FF", 16),
                    Integer.parseUnsignedInt("A4", 16),
                    Integer.parseUnsignedInt("00", 16),
                    Integer.parseUnsignedInt("00", 16),
                    new byte[]
                    {
                        Byte.parseByte("06", 16)
                    },
                    Integer.parseUnsignedInt("01", 16)
            );

            System.out.println("Selecting Card Type - Cmd: FF A4 00 00 06");
            ResponseAPDU res = this.ActiveCard.getBasicChannel().transmit(cmd);
            System.out.println("Selecting Card Type - SW1: " + res.getSW1() + ", SW2: " + res.getSW2());

            boolean pin = this.VerifyPIN("FF", "FF", "FF");
            if (pin)
            {
                this.CurrentBalance = this.DoGetBalance();

                ((Event<EventArgs>) this.CardInserted).Invoke(this, EventArgs.Empty());
            }
            else
            {
                System.out.println("Verify Pin Failed: Disconnecting...");
                this.ActiveCard.disconnect(true);
            }
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

            ((Event<EventArgs>) this.CardRemoved).Invoke(this, EventArgs.Empty());
        }
        catch (Exception ex)
        {
            Logger.getLogger(SmartCardReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    
    public IEvent<EventArgs> GetCardInsertedEvent()
    {
        return this.CardInserted;
    }

    public IEvent<EventArgs> GetCardRemovedEvent()
    {
        return this.CardRemoved;
    }

    public IEvent<EventArgs> GetBalanceChangedEvent()
    {
        return this.BalanceChanged;
    }

    public Card GetActiveCard()
    {
        return this.ActiveCard;
    }

    @Override
    public boolean IsCardPresent()
    {
        return this.IsCardPresent;
    }

    @Override
    public boolean SetBalance(float amount)
    {
        if (this.ActiveCard == null)
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

    @Override
    public boolean AddBalance(float amount)
    {
        if (this.ActiveCard == null)
        {
            return false;
        }

        try
        {
            float val = this.GetBalance();
            val = val + amount;
            boolean output = this.SetBalance(val);

            if (output)
            {
                this.CurrentBalance = val;
            }

            return output;
        }
        catch (Exception ex)
        {
            Logger.getLogger(SmartCardReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public boolean SubtractBalance(float amount)
    {
        if (this.ActiveCard == null)
        {
            return false;
        }

        try
        {
            float val = this.GetBalance();
            val = val - amount;
            boolean output = this.SetBalance(val);

            if (output)
            {
                this.CurrentBalance = val;
            }

            return output;
        }
        catch (Exception ex)
        {
            Logger.getLogger(SmartCardReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public float GetBalance()
    {
        return this.CurrentBalance;
    }

    private float DoGetBalance() throws Exception
    {
        if (this.ActiveCard == null)
        {
            throw new Exception("Operacón inválida - No hay SmartCard conectada.");
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

            System.out.println("VerifyPIN - Cmd: FF 20 00 00 CODE");
            ResponseAPDU res = this.ActiveCard.getBasicChannel().transmit(cmd);
            System.out.println("VerifyPIN  - SW1: " + res.getSW1() + ", SW2: " + res.getSW2());

            return res.getSW1() == 144 && res.getSW2() == 7;
        }
        catch (CardException ex)
        {
            Logger.getLogger(SmartCardReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
