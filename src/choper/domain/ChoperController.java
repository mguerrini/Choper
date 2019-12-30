/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

import choper.domain.cardReaders.ICardReader;
import choper.domain.cardReaders.IVirtualCardReader;
import choper.platform.strings.StringUtil;
import spark.Request;
import spark.Response;

/**
 *
 * @author mguerrini
 */
public class ChoperController
{
    private ChoperMachine Machine;

    public ChoperController(ChoperMachine machine)
    {
        this.Machine = machine;
    }

    public Object PaymentReady(Request req, Response res)
    {
        String amount = req.params("amount");

        if (StringUtil.IsNullOrEmpty(amount))
        {
            return null;
        }

        try
        {
            float valueAdd = Float.parseFloat(amount);

            this.Machine.AddMoney(valueAdd);

            return "Amount added: $" + amount;
        }
        catch (Exception ex)
        {
            return "Invalid amount: " + amount;
        }
    }

    public Object Calibrate(Request req, Response res)
    {
        this.Machine.StartCalibration();
        if (this.Machine.GetStatus() == ChoperStatusType.Calibration)
        {
            return "Calibration Started";
        }
        else
        {
            return "No es posible iniciar la calibración. Estado actual: " + this.Machine.GetStatus();
        }
    }

    public Object CancelCalibration(Request req, Response res)
    {
        this.Machine.CancelCalibration();
        return "Calibration Cancelled";
    }

    public Object AcceptCalibration(Request req, Response res)
    {
        this.Machine.FinishCalibration();
        return "Calibration Accepted";
    }

    public Object AddMoney(Request req, Response res)
    {
        String amount = req.params("amount");

        if (StringUtil.IsNullOrEmpty(amount))
        {
            return null;
        }

        try
        {
            float valueAdd = Float.parseFloat(amount);

            this.Machine.AddMoney(valueAdd);

            return "Amount added: $" + amount;
        }
        catch (Exception ex)
        {
            return "Invalid amount: " + amount;
        }
    }

    public Object SubMoney(Request req, Response res)
    {
        String amount = req.params("amount");

        if (StringUtil.IsNullOrEmpty(amount))
        {
            return null;
        }

        try
        {
            float valueAdd = Float.parseFloat(amount);

            this.Machine.SubstractMoney(valueAdd);

            return "Amount subtracted: $" + amount;
        }
        catch (Exception ex)
        {
            return "Invalid subtracted: " + amount;
        }
    }

    public Object BuyAmount(Request req, Response res)
    {
        String amount = req.params("amount");

        if (StringUtil.IsNullOrEmpty(amount))
        {
            return null;
        }

        try
        {
            float valueAdd = Float.parseFloat(amount);

            this.Machine.BuyAmount(valueAdd);

            return "Buy Amount : $" + amount;
        }
        catch (Exception ex)
        {
            return "Invalid amount: " + amount;
        }
    }

    public Object BuyPint(Request req, Response res)
    {
        this.Machine.BuyPint(1);
        return "Pinta comprada";
    }

    public Object BuyHalfPint(Request req, Response res)
    {
        this.Machine.BuyPint(0.5f);
        return "Media pinta comprada";
    }

    public Object Free(Request req, Response res)
    {
        this.Machine.Free();
        return "Muestra libre lista";
    }

    public Object FreeByVolumen(Request req, Response res)
    {
        String amount = req.params("volumen");

        if (StringUtil.IsNullOrEmpty(amount))
        {
            return null;
        }

        try
        {
            float valueAdd = Float.parseFloat(amount);

            this.Machine.FreeByVolumen(valueAdd);

            return "Free by Volumen: " + amount;
        }
        catch (Exception ex)
        {
            return "Invalid volumen: " + amount;
        }
    }

    public Object FreeByAmount(Request req, Response res)
    {
        String amount = req.params("amount");

        if (StringUtil.IsNullOrEmpty(amount))
        {
            return null;
        }

        try
        {
            float valueAdd = Float.parseFloat(amount);

            this.Machine.FreeByAmount(valueAdd);

            return "Free by Amount : $" + amount;
        }
        catch (Exception ex)
        {
            return "Invalid amount: " + amount;
        }
    }

    public Object CancelBuy(Request arg0, Response arg1)
    {
        this.Machine.CancelBuy();
        return "Venta finalizada";
    }

    public Object OpenValve(Request req, Response res)
    {
        this.Machine.OpenFlowValve();
        return "Válvula abierta";
    }

    public Object CloseValve(Request req, Response res)
    {
        this.Machine.CloseFlowValve();
        return "Válvula cerrada";
    }

    public Object LockValve(Request req, Response res)
    {
        this.Machine.LockUnlockFlowValve(true);
        return "Válvula bloqueada";
    }

    public Object UnlockValve(Request req, Response res)
    {
        this.Machine.LockUnlockFlowValve(false);
        return "Válvula desbloqueada";
    }

    public Object ExecuteCommand(Request req, Response res)
    {
        String name = req.params("name");

        if (StringUtil.IsNullOrEmpty(name))
        {
            return null;
        }
        this.Machine.ExecuteCommand(name);
        return "Command Executed";
    }

    public Object SetConfiguration(Request req, Response res)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Object GetConfiguration(Request req, Response res)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Object GetState(Request req, Response res)
    {

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
