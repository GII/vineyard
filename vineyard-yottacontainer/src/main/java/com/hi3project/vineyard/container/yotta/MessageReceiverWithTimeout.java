/*******************************************************************************
 *
 * Copyright (C) 2015 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 * Copyright (C) 2015 Alejandro Paz <alejandropl@lagostelle.com>
 *
 * This file is part of Vineyard.
 *
 * Vineyard is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Vineyard is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Vineyard. If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package com.hi3project.vineyard.container.yotta;

import com.hi3project.broccoli.bsdm.api.asyncronous.IAsyncMessageClient;
import com.hi3project.broccoli.bsdm.api.asyncronous.IMessage;
import com.hi3project.broccoli.bsdm.impl.asyncronous.AbstractMessage;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.io.BSDFLogger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>
 *  <b>Description:</b></p>
 *  General implementation for an IMessage receiver that handles conversationId
 * timestamps. Runnable.
 *
 *
 * <p>
 *  Colaborations:
 *
 * <ul>
 * <li>It is Runnable, so it uses a Thread to perform its loop</li>
 * <li>A map of TimeoutCallback</li>
 * </ul>
 * 
 * <p>
 *  Responsabilities:
 *
 * <ul>
 * <li>adds a TimeoutCallback</li>
 * <li>can receive a message and assign it to its corresponding TimeoutCallback</li>
 * <li>while looping checks whether a TimeoutCallback has reached its timeout</li>
 * </ul>
 * 
 * <p><b>Creation date:</b> 
 * 16-02-2015 </p>
 *
 * <p><b>Changelog:</b></p>
 * <ul>
 * <li> 1 , 16-02-2015 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class MessageReceiverWithTimeout implements IAsyncMessageClient, Runnable
{
    
    private static int CHECK_PERIOD = 250;

    private Map<String, TimeoutCallback> convIdToTimestampMap;
    
    private String nameAsClient;
    
    private volatile boolean isRunning = false;
    
    private Thread threadRef = null;
        
    
    
    public MessageReceiverWithTimeout(String name)
    {
        this.convIdToTimestampMap = new HashMap<String, TimeoutCallback>();
        this.nameAsClient = name;
    }
    
    public void addCalback(String convId, TimeoutCallback callback)
    {
        this.convIdToTimestampMap.put(convId, callback);
    }

    @Override
    public synchronized void receiveMessage(IMessage msg) throws ModelException
    {
        if (msg instanceof AbstractMessage)
        {
            TimeoutCallback callback = this.getCallback((AbstractMessage) msg);
            callback.registerMessage(msg);
        }
    }

    @Override
    public String getName()
    {
        return this.nameAsClient;
    }
    
    @Override
    public void run()
    {
        while (this.isRunning)
        {
            
            long timestampNow = new Date().getTime();
            ArrayList<String> callbacksToRemove = new ArrayList<String>();
            for ( Map.Entry<String, TimeoutCallback> callback : this.convIdToTimestampMap.entrySet())
            {
                if (timestampNow > callback.getValue().getTimeoutInMs())
                {
                    callbacksToRemove.add(callback.getKey());
                    callback.getValue().timeout();
                }
            }
            
            for (String key : callbacksToRemove)
            {
                this.convIdToTimestampMap.remove(key);
            }
            
            try
            {
                Thread.sleep(CHECK_PERIOD);
            } catch (InterruptedException ex)
            {
                BSDFLogger.getLogger().error("Error wih MessageReceiverWithTimeout: " + ex.toString());
            }
        }
    }
    
    
    public void start()
    {
        this.isRunning = true;
        this.threadRef = new Thread(this);
        this.threadRef.start();
    }
    
    public void stop()
    {
        this.isRunning = false;
    }
    
    
    private TimeoutCallback getCallback(AbstractMessage aMsg)
    {
        return this.convIdToTimestampMap.get(aMsg.getConversationId());
    }
    
    
}
