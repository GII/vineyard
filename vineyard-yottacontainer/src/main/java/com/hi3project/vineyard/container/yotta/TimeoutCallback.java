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

import com.hi3project.broccoli.bsdm.api.asyncronous.IMessage;
import com.hi3project.broccoli.bsdm.api.asyncronous.IMessageTimeoutCallback;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
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
public class TimeoutCallback 
{
    
    private long timeoutInMs;
    
    private IMessageTimeoutCallback callback;
    
    private Collection<IMessage> messages;
    
    
    public TimeoutCallback(long timeoutInMs, IMessageTimeoutCallback callback)
    {     
        this.timeoutInMs = new Date().getTime() + timeoutInMs;
        this.callback = callback;
        this.messages = new ArrayList<IMessage>();
    }

    public long getTimeoutInMs()
    {
        return timeoutInMs;
    }

    public void setTimeoutInMs(long timeoutInMs)
    {
        this.timeoutInMs = timeoutInMs;
    }
    
    public void registerMessage(IMessage msg)
    {
        this.messages.add(msg);
    }
    
    
    public void timeout()
    {
        this.callback.timeout(this.messages);
    }
    

}
