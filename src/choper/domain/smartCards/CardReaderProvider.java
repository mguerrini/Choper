/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.smartCards;

import choper.platform.ConfigurationProvider;

/**
 *
 * @author max22
 */
public class CardReaderProvider
{
    public static CardReaderProvider Instance = new CardReaderProvider();

    private ICardReader SingleInstance;

    public ICardReader Get()
    {
        if (this.SingleInstance == null)
        {
            String cardType = ConfigurationProvider.Instance.GetString("CardReader", "Type");
            switch (cardType)
            {
                case "MercadoPago":
                    this.SingleInstance = new MercadoPagoCardReader();
                    break;

                case "SmarCardReader":
                    this.SingleInstance = new SmartCardReader();
                    break;

                case "Manual":
                default:
                    this.SingleInstance = new ManualCardReader();
                    break;

            }
        }

        return this.SingleInstance;
    }
}
