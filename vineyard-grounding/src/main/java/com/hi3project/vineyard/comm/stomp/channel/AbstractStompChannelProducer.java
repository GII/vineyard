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
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelProducer;
import com.hi3project.broccoli.bsdm.api.asyncronous.IMessage;
import com.hi3project.broccoli.bsdl.impl.registry.BSDLRegistry;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdm.api.serializing.IMessageSerializer;
import com.hi3project.broccoli.bsdm.impl.exceptions.SerializingException;
import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceGroundingException;
import com.hi3project.broccoli.io.BSDFLogger;

/**
 * <p>
 *  <b>Description:</b></p>
 *  Implementation of IChannelProducer for STOMP
 *
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
public abstract class AbstractStompChannelProducer extends AbstractStompChannel implements IChannelProducer
{

    
    public AbstractStompChannelProducer(
            IMessageSerializer parameterValuesSerializer,
            String connectionURL,
            String name) throws ServiceGroundingException, ModelException
    {
        super(parameterValuesSerializer, connectionURL, name);
    }
    
    
    public AbstractStompChannelProducer(
            IMessageSerializer parameterValuesSerializer,
            String connectionURL,
            IBSDLRegistry bsdlRegistry,
            String name) throws ServiceGroundingException
    {
        super(parameterValuesSerializer, connectionURL, bsdlRegistry, name);
    }

    @Override
    public boolean send(IMessage data) throws ServiceGroundingException
    {
        try
        {
            BSDFLogger.getLogger().info(
                    "Sending mesage using Stomp throught channel: " + this.getChannelFullName() +
                    " and using client: " + this.stompConnectionClient.toString());
            String serializedMsg = this.parameterValuesSerializer.serializeMessage(data, (BSDLRegistry)this.bsdlRegistry);
            this.stompConnectionClient.send(this.getChannelFullName(), serializedMsg);
        } catch (SerializingException ex)
        {
            throw new ServiceGroundingException("Problems sending msg: " + data.toString(), ex);
        }
        return true;
    }

    @Override
    public void signalReception() throws ModelException {  }

}
