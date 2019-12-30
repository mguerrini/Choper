/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.cores;

import choper.domain.ChoperCore;
import choper.platform.ConfigurationProvider;

/**
 *
 * @author mguerrini
 */
public class ChoperCoreFactory
{
    public static ChoperCoreFactory Instance = new ChoperCoreFactory();

    private ChoperCore SingleInstance;

    public ChoperCore Get()
    {
        if (this.SingleInstance == null)
        {
            String cardType = ConfigurationProvider.Instance.GetString("ChoperCore", "Type");
            switch (cardType)
            {
                case "Manual":
                    this.SingleInstance = new ManualChoperCore();
                    break;

                case "SmartCard":
                    this.SingleInstance = new SmartCardChoperCore();
                    break;

                case "Automatic":
                    this.SingleInstance = new AutomaticChoperCore();
                    break;

                default:
                    this.SingleInstance = new ManualChoperCore();
                    break;

            }
        }

        return this.SingleInstance;

    }
}
