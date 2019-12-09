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

    public Object InsertCard(Request req, Response res)
    {
        String amount = req.params("amount");

        if (StringUtil.IsNullOrEmpty(amount))
        {
            return null;
        }

        ICardReader reader = this.Machine.GetCardReader();

        if (!IVirtualCardReader.class.isAssignableFrom(reader.getClass()))
        {
            return null;
        }

        IVirtualCardReader virtualReader = (IVirtualCardReader) reader;

        float valueAdd = Float.parseFloat(amount);

        virtualReader.InsertCard(valueAdd);

        return "Card Inserted with $" + amount;
    }

    public Object Calibrate(Request req, Response res)
    {
        return "Calibration Started";
    }

    public Object CancelCalibration(Request req, Response res)
    {
        return "Calibration Cancelled";
    }

    public Object AcceptCalibration(Request req, Response res)
    {
        return "Calibration Accepted";
    }
}
