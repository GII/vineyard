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

package com.hi3project.vineyard.registry.client;

/**
 * 
 * <p><b>Creation date:</b> 
 * 22-04-2015 </p>
 *
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 22-04-2015 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class BrokerConnectorsConfig 
{

    public static final String stompConnectorURL = getStompURLfromIP("0.0.0.0");
    
    public static final String wsConnectorURL = getWSURLfromIP("0.0.0.0");
    
    public static final String openWireConnectorURL = getOpenWireURLfromIP("0.0.0.0");
    
    
    public static String getStompURLfromIP(String ip)
    {
        return "stomp://" + ip + ":61701";
    }
    
    public static String getWSURLfromIP(String ip)
    {
        return "ws://" + ip + ":61702";
    }
    
    public static String getOpenWireURLfromIP(String ip)
    {
        return "tcp://" + ip + ":61703";
    }
    
}
