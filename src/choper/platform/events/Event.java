/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.platform.events;

import choper.platform.ConfigurationProvider;
import choper.platform.threading.TaskQueue;
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author max22
 */
public class Event<TEventArgs> implements IEvent<TEventArgs>
{
    public Event()
    {
        this("Event");
    }

    public Event(String name)
    {
        if (name == null)
        {
            this.Name = "";
        } 
        else
        {
            this.Name = name;
        }
    }

    private String Name = "";

    private ArrayList<IEventHandler<TEventArgs>> eventDelegateArray = new ArrayList<>();

    private TaskQueue Worker;

    /*----- Methods ------*/
    public void Subscribe(IEventHandler<TEventArgs> methodReference)
    {
        eventDelegateArray.add(methodReference);
    }

    public void UnSubscribe(IEventHandler<TEventArgs> methodReference)
    {
        eventDelegateArray.remove(methodReference);
    }

    public void Invoke(Object source, TEventArgs eventArgs)
    {
        if (eventDelegateArray.size() > 0)
        {
            try
            {
                eventDelegateArray.forEach(p -> p.invoke(source, eventArgs));
            } 
            catch (Exception ex)
            {
                java.util.logging.Logger.getLogger(Event.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class AsyncEventData
    {
        public Object Source;

        public Object Args;
    }

    public void InvokeAsync(Object source, TEventArgs eventArgs)
    {
        AsyncEventData item = new AsyncEventData();
        item.Args = eventArgs;
        item.Source = source;

        this.GetWorker().Enqueue(item);
    }

    private void DoInvokeAsync(Object context, Object item)
    {
        AsyncEventData data = (AsyncEventData) item;

        try
        {
            this.Invoke(data.Source, (TEventArgs) data.Args);
        } catch (Exception ex)
        {
            System.out.println(ex);
        }
    }

    private TaskQueue GetWorker()
    {
        if (this.Worker == null)
        {
            this.Worker = new TaskQueue(this.Name, this::DoInvokeAsync);
        }

        return this.Worker;
    }

    public void Close()
    {
        if (eventDelegateArray.size() > 0)
        {
            eventDelegateArray.clear();
        }

        if (this.Worker != null)
        {
            this.Worker.Dispose();
        }

        this.Worker = null;
    }
}
