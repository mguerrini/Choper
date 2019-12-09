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

        Spark.post("/choper/insert/:amount", this.Controller::InsertCard);
    }
}