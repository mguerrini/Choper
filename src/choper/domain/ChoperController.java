/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

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
        return null;
    }
    
    public Object InsertCard(Request req, Response res)
    {
        return null;
    }
}
