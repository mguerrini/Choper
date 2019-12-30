/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.commands;

/**
 *
 * @author mguerrini
 */
public enum CommandType
{
    AddMoney, //suma un monto
    SubMoney, //resta un monto
    
    BuyAmount, //compra por una cantidad
    BuyPint, //compra de pinta entera
    BuyLiter,
    Free, //compra por un monto
    
    OpenValve,
    CloseValve,
    LockValve,
    UnlockValve,
    
    Other,
}
