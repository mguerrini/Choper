/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.platform.threading;

/**
 *
 * @author mguerrini
 */
public interface RemovePredicate
{
    boolean MustRemove(Object task, int taskIndex);
}
