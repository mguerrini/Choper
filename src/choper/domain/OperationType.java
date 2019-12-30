/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

/**
 *
 * @author mguerrini
 */
public enum OperationType 
{
    AddMoney,
    SubMoney,
    ClearMoney,

    BuyPint, //por porcentage de pinta
    BuyLiter,  //por porcentage de litro
    BuyAmount, //compra por dinero
    Free, //muestra gratis
    
    VolumeChanged,
    UpdateDisplayBalance,


    CardInserted, 
    CardRemoved,
    
    StartSelling,
    StopSelling, 
}


