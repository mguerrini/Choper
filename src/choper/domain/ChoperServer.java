/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

import spark.Spark;
import spark.servlet.SparkApplication;

/**
 *
 * @author mguerrini
 */
public class ChoperServer implements SparkApplication {

    public ChoperController Controller;
    
    public ChoperServer(ChoperController controller)
    {
        this.Controller = controller;
    }
    
    public void Start()
    {
        Spark.port(8080);
        this.init();
    }
    
    public void Stop()
    {
        Spark.stop();
    }
    
    public void init() {
/*
        Spark.before((request, response) -> {
            request.body();
            request.attribute("request-start", System.currentTimeMillis());
        });
*/
        Spark.post("/choper/calibrate", this.Controller::Calibrate);
        Spark.post("/choper/calibrate/accept", this.Controller::AcceptCalibration);
        Spark.post("/choper/calibrate/cancel", this.Controller::CancelCalibration);

        Spark.post("/choper/addmoney/:amount", this.Controller::AddMoney);
        Spark.post("/choper/submoney/:amount", this.Controller::SubMoney);

        Spark.post("/choper/buy/amount/:amount", this.Controller::BuyAmount);
        Spark.post("/choper/buy/pint", this.Controller::BuyPint);
        Spark.post("/choper/buy/halfpint", this.Controller::BuyHalfPint);

        Spark.post("/choper/buy/free", this.Controller::Free);
        Spark.post("/choper/buy/free/:volumen", this.Controller::FreeByVolumen);
        Spark.post("/choper/buy/free/amount/:amount", this.Controller::FreeByAmount);

        Spark.post("/choper/buy/cancel", this.Controller::CancelBuy);
        Spark.post("/choper/comand/:name", this.Controller::ExecuteCommand);

        
        Spark.post("/choper/valve/open", this.Controller::OpenValve);
        Spark.post("/choper/valve/close", this.Controller::CloseValve);
        Spark.post("/choper/valve/lock", this.Controller::LockValve);
        Spark.post("/choper/valve/unlock", this.Controller::UnlockValve);
        
        Spark.post("/choper/configuration/:key/:value", this.Controller::SetConfiguration);
        Spark.get("/choper/configuration", this.Controller::GetConfiguration);
        Spark.get("/choper/status", this.Controller::GetState);
    }
}