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

package com.hi3project.vineyard.comm.stomp.channel;

import com.hi3project.broccoli.bsdl.api.registry.IBSDLRegistry;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdm.api.serializing.IMessageSerializer;
import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceGroundingException;

/**
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
public class ControlStompChannelProducer extends AbstractStompChannelProducer
{
    
    public ControlStompChannelProducer(
            IMessageSerializer parameterValuesSerializer,
            String connectionURL,
            String name) throws ServiceGroundingException, ModelException
    {
        super(parameterValuesSerializer, connectionURL, name);
    }

    public ControlStompChannelProducer(
            IMessageSerializer parameterValuesSerializer,
            String connectionURL,
            String name,
            IBSDLRegistry bsdlRegistry) throws ServiceGroundingException
    {
        super(parameterValuesSerializer, connectionURL, bsdlRegistry, name);
    }

    @Override
    protected String getChannelFullName()
    {
        return "/topic/" + this.name;
    }

}
