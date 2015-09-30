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

package com.hi3project.vineyard.comm.jms.channel;

import com.hi3project.broccoli.bsdl.api.registry.IBSDLRegistry;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelProducer;
import com.hi3project.broccoli.bsdm.api.asyncronous.IMessage;
import com.hi3project.broccoli.bsdl.impl.registry.BSDLRegistry;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdm.api.serializing.IMessageSerializer;
import com.hi3project.broccoli.bsdm.impl.exceptions.SerializingException;
import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceGroundingException;
import com.hi3project.broccoli.io.BSDFLogger;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

/**
 * <p><b>Description:</b></p>
 *  Implementation of IChannelProducer that uses the JMS API
 *
 *
 * <p><b>Creation date:</b> 
 * 01-07-2014 </p>
 *
 * <p><b>Changelog:</b></p>
 * <ul>
 * <li> 1 , 01-07-2014 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public abstract class AbstractJMSChannelProducer extends AbstractJMSAMQChannel implements IChannelProducer
{
    
    MessageProducer messageProducer = null;
    

    public AbstractJMSChannelProducer(
            IMessageSerializer messageSerializer,
            String connection,
            String name) throws ModelException
    {

        super(messageSerializer, connection, name);
        this.completeInitialization(name);

    }

    public AbstractJMSChannelProducer(
            IMessageSerializer messageSerializer,
            String connection,
            String name,
            IBSDLRegistry bsdlRegistry) throws ServiceGroundingException
    {

        super(messageSerializer, connection, bsdlRegistry, name);
        this.completeInitialization(name);

    }
    
    protected abstract MessageProducer initializeMessageProducer(String name) throws ServiceGroundingException;

    private void completeInitialization(String name) throws ServiceGroundingException
    {
        this.messageProducer = initializeMessageProducer(name);        
    }
    
    @Override
    public boolean send(IMessage data) throws ServiceGroundingException
    {
        try
        {
            BSDFLogger.getLogger().info(
                    "Sending mesage using JMS API throught channel: " + this.name +
                    " and using connection: " + this.getJMSConnection().toString());
            String serializedMsg = this.parameterValuesSerializer.serializeMessage(data, (BSDLRegistry)this.bsdlRegistry);
            TextMessage jmsTextMessage = this.session.createTextMessage(serializedMsg);
            this.messageProducer.send(jmsTextMessage);
        } catch (JMSException ex)
        {
            throw new ServiceGroundingException("Problems sending msg: " + data.toString(), ex);
        } catch (SerializingException ex)
        {
            throw new ServiceGroundingException("Problems sending msg: " + data.toString(), ex);
        }
        return true;
    }  

    @Override
    public void signalReception() throws ModelException   {    }

}
