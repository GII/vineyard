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

package com.hi3project.vineyard.comm;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 *  <b>Description:</b>
 *  An instance of this class will keep record of already received message IDs.
 *  This will be used to avoid processing of the same message by different consumers
 * subscribed to the same channel.
 * </p>
 *
 * <p><b>Creation date:</b> 
 * 15-06-2015 </p>
 *
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 15-06-2015 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class ReceivedMessagesChecker 
{
    
    private Set<String> receivedIds = new HashSet<String>();
    
    public ReceivedMessagesChecker() {}
    
    
    public synchronized void addId(String id)
    {
        this.receivedIds.add(id);
    }
    
    public synchronized boolean isAlready(String id)
    {
        return this.receivedIds.contains(id);
    }

}
