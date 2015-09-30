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

package com.hi3project.vineyard.comm.stomp;

import java.util.Map;

/**
 * <p>
 *  <b>Description:</b></p>
 *  A simple wrapper that contains a Stomp message
 * as it is handle by the Gozirra API.
 *
 *
 * <p><b>Creation date:</b> 
 * 12-01-2015 </p>
 *
 * <p><b>Changelog:</b></p>
 * <ul>
 * <li> 1 , 12-01-2015 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class StompMessageWrapper 
{
    
    private Map header;
    
    private String body;
    
    
    public StompMessageWrapper(Map header, String body)
    {
        this.header = header;
        this.body = body;
    }
    
    
    public Map getHeader()
    {
        return this.header;
    }
    
    
    public String getBody()
    {
        return this.body;
    }
    
}
