/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.platform.threading;

import choper.platform.events.*;
import choper.platform.strings.StringUtil;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author mguerrini
 */
//public delegate void TaskHandler(Object context, Object data);
public class TaskQueue //: ITaskQueue
{
    public IEvent<EventArgs> TaskFinished = new Event("TaskQueue->TaskFinished");

    public TaskQueue(TaskHandler taskHandler)
    {
        this("", taskHandler);
    }

    public TaskQueue(String name, TaskHandler taskHandler)
    {
        this.Enabled = true;

        if (StringUtil.IsNullOrEmpty(name))
        {
            name = "TaskQueue";
        }

        this.Name = name;

        this.Tasks = new LinkedList<Object>();
        this.TaskHandler = taskHandler;

        this.IsDisposing = false;
        this.IsSingleTask = false;
        this.Counter = 0;
        this.IsWorking = false;
    }

    /// <summary>
    /// Indica si esta habilitada la cola o no. Si esta deshabilitada las tareas que se encolen se descartan.
    /// </summary>
    public boolean Enabled;

    private boolean isPaused;

    private boolean IsWorking;

    protected int Counter;

    protected TaskHandler TaskHandler;

    protected Thread Worker;

    protected LinkedList<Object> Tasks;

    protected boolean IsDisposing;

    public boolean IsSingleTask;

    public String Name;

    public Object Context;

    public int PendingTasks()
    {
        return this.Tasks.size();
    }

    public List<Object> GetPendingTasks()
    {
        return new ArrayList<Object>(this.Tasks);
    }

    protected void RaiseTaskFinished()
    {
        ((Event) this.TaskFinished).Invoke(this, EventArgs.Empty());
    }

    public boolean IsPaused()
    {
        return this.isPaused;
    }

    public void Pause()
    {
        this.isPaused = true;
    }

    public void Resume()
    {
        if (!this.isPaused)
        {
            return;
        }

        this.isPaused = false;

        boolean start = false;

        synchronized (this.Tasks)
        {
            start = this.Tasks.size() > 0;
        }

        if (start)
        {
            this.WorkerStart();
        }
    }

    public void EnqueueRange(List<Object> tasks)
    {
        if (!this.Enabled)
        {
            return;
        }

        if (this.IsDisposing)
        {
            return;
        }

        synchronized (this.Tasks)
        {
            this.Counter += tasks.size();

            for (var t : tasks)
            {
                this.Tasks.addLast(t);
            }

        }

        if (!this.isPaused)
        {
            this.WorkerStart();
        }
    }

    public void Enqueue(Object task)
    {
        if (this.IsDisposing)
        {
            return;
        }

        int count = 0;

        synchronized (this.Tasks)
        {
            this.Counter++;
            this.Tasks.addLast(task);

            count = this.Tasks.size();

        }

        if (!this.isPaused)
        {
            this.WorkerStart();
        }
    }
    
    public void Enqueue(Object task, RemovePredicate remove)
    {
        if (this.IsDisposing)
        {
            return;
        }

        int count = 0;

        synchronized (this.Tasks)
        {
            List<Integer> toRemoveIndexes = new ArrayList<>();
            
            for(int i=0; i<this.Tasks.size(); i++)
            {
                boolean toRemove = remove.MustRemove(this.Tasks.get(i), i);
                if (toRemove)
                    toRemoveIndexes.add(i);
            }
            
            for (int index : toRemoveIndexes)
            {
                this.Tasks.remove(index);
                this.Counter--;
            }
            
            this.Counter++;
            this.Tasks.addLast(task);

            count = this.Tasks.size();
        }

        if (!this.isPaused)
        {
            this.WorkerStart();
        }
    }

    public void Clear()
    {
        this.Pause();

        synchronized (this.Tasks)
        {
            this.Counter = 0;
            this.Tasks.clear();
        }

        this.Resume();
    }

    private void Worker_Finished()
    {
        boolean raise = true;

        //me fijo si tiene que seguir procesando
//        synchronized (this.Tasks) {
//            if (this.Tasks.size() > 0) {
//                raise = false;
//                if (!this.isPaused) {
//                    this.WorkerStart();
//                }
//            }
//        }
        //termino de enviar todos los eventos
        if (raise)
        {
            this.RaiseTaskFinished();
        }
    }

    public int GetPendingCount()
    {

        synchronized (this.Tasks)
        {
            return this.Tasks.size();
        }
    }

    protected void DoWork()
    {

        try
        {
            this.TaskProcess();
        } catch (Exception ex)
        {
            System.out.println(ex);
        } finally
        {
            this.Worker_Finished();
        }
    }

    protected void TaskProcess()
    {

        if (this.IsWorking)
        {
            return;
        }

        this.IsWorking = true;

        Object task;

        while (!this.IsDisposing && !this.isPaused)
        {
            int taskCount = 0;

            synchronized (this.Tasks)
            {
                taskCount = this.Tasks.size();

                if (taskCount > 0)
                {
                    task = this.Tasks.getFirst();
                    this.Tasks.removeFirst();

                    this.Counter--;
                } else
                {
                    this.Worker = null; // el thread muere solo
                    this.IsWorking = false;
                    break;
                }
            }

            try
            {
                this.TaskHandler.Handle(this.Context, task);

            } catch (Exception ex)
            {
                System.out.println(ex);
            }

            if (this.IsSingleTask)
            {
                break;
            }
        }
    }

    private void WorkerStart()
    {
        synchronized (this.Tasks)
        {
            if (this.IsWorking)
            {
                return;
            }

            if (this.Worker == null)
            {
                this.Worker = new Thread(this::DoWork, this.Name);
            }

            if (this.Worker.getState() == Thread.State.NEW)
            {
                this.Worker.start();
            } else
            {
                this.Worker = null;
                this.Worker = new Thread(this::DoWork, this.Name);
                this.Worker.start();
            }
        }
    }

    private void WorkerStop()
    {
        synchronized (this.Tasks)
        {
            if (!this.IsWorking)
            {
                return;
            }

            if (this.Worker != null)
            {
                if (this.Worker.getState() != Thread.State.NEW && this.Worker.getState() != Thread.State.TERMINATED)
                {
                    try
                    {
                        this.Worker.join((long) 3000);
                    } 
                    catch (Exception ex1)
                    {
                        try
                        {
                            this.Worker.interrupt();
                        } 
                        catch (Exception ex2)
                        {
                        }
                    }
                }

                this.Worker = null;
            }
        }
    }

    public void Dispose()
    {
        this.IsDisposing = true;

        this.WorkerStop();

        synchronized (this.Tasks)
        {
            this.Tasks.clear();
        }
    }

    public String ToString()
    {
        return this.Name + " - Count: " + this.Tasks.size();
    }
}
