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

import com.hi3project.broccoli.bsdf.impl.serializing.JSONMessageSerializer;
import com.hi3project.broccoli.bsdl.api.registry.IBSDLRegistry;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelConsumer;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelProducer;
import com.hi3project.vineyard.comm.jms.channel.ControlJMSChannelConsumer;
import com.hi3project.vineyard.comm.jms.channel.ControlJMSChannelProducer;
import com.hi3project.vineyard.comm.jms.channel.ResultsJMSChannelConsumer;
import com.hi3project.vineyard.comm.jms.channel.ResultsJMSChannelProducer;
import com.hi3project.vineyard.comm.stomp.channel.ControlStompChannelConsumer;
import com.hi3project.vineyard.comm.stomp.channel.ControlStompChannelProducer;
import com.hi3project.vineyard.comm.stomp.channel.ResultsStompChannelConsumer;
import com.hi3project.vineyard.comm.stomp.channel.ResultsStompChannelProducer;

/**
 *
 * <p><b>Creation date:</b> 
 * 14-04-2015 </p>
 *
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 14-04-2015 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class ChannelFactory 
{
    
    private static ChannelFactory singletonInstance;
    
    
    public ChannelFactory() {}
    
    
    
    public static ChannelFactory getSingleton()
    {
        if (null == singletonInstance)
        {
            singletonInstance = new ChannelFactory();
        }
        return singletonInstance;
    }
    
    
    public IChannelProducer resultsChannelProducerInstanceFor(
            String connectionString,
            String containerRequestChannelName,
            IBSDLRegistry bsdlRegistry) throws ModelException
    {
        
        if (connectionString.contains("tcp://"))
        {
            return new ResultsJMSChannelProducer(
                new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()),
                connectionString,
                containerRequestChannelName,
                bsdlRegistry);
        }
        
        if (connectionString.contains("stomp://") || connectionString.contains("ws://"))
        {
            return new ResultsStompChannelProducer(
               new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()),
                connectionString,
                containerRequestChannelName,
                bsdlRegistry);
        }
        
        return null;
        
    }
    
    
    public IChannelConsumer resultsChannelConsumerInstanceFor(
            String connectionString,
            String containerRequestChannelName,
            IBSDLRegistry bsdlRegistry) throws ModelException
    {
        
        if (connectionString.contains("tcp://"))
        {
            return new ResultsJMSChannelConsumer(
                new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()),
                connectionString,
                containerRequestChannelName,                    
                bsdlRegistry);
        }
        
        if (connectionString.contains("stomp://") || connectionString.contains("ws://"))
        {
            return new ResultsStompChannelConsumer(
                new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()),
                connectionString,
                containerRequestChannelName,                    
                bsdlRegistry);
        }
        
        return null;
        
    }
    
    
    public IChannelProducer controlChannelProducerInstanceFor(
            String connectionString,
            String containerRequestChannelName,
            IBSDLRegistry bsdlRegistry) throws ModelException
    {
        
        if (connectionString.contains("tcp://"))
        {
            return new ControlJMSChannelProducer(
                            new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()),
                            connectionString,
                            containerRequestChannelName,
                            bsdlRegistry);            
        }

        if (connectionString.contains("stomp://") || connectionString.contains("ws://"))
        {
            return new ControlStompChannelProducer(
                            new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()),
                            connectionString,
                            containerRequestChannelName,
                            bsdlRegistry);
        }
        
        return null;
        
    }
    
    
    public IChannelConsumer controlChannelConsumerInstanceFor(
            String connectionString,
            String containerRequestChannelName,
            IBSDLRegistry bsdlRegistry) throws ModelException
    {
        
        if (connectionString.contains("tcp://"))
        {
            return new ControlJMSChannelConsumer(
                            new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()),
                            connectionString,
                            containerRequestChannelName,
                            bsdlRegistry);            
        }

        if (connectionString.contains("stomp://") || connectionString.contains("ws://"))
        {
            return new ControlStompChannelConsumer(
                            new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()),
                            connectionString,
                            containerRequestChannelName,
                            bsdlRegistry);
        }
        
        return null;
        
    }
    
    
    public IChannelConsumer controlChannelConsumerInstanceFor(
            String connectionString,
            String containerRequestChannelName,
            IBSDLRegistry bsdlRegistry,
            ReceivedMessagesChecker messagesCheker) throws ModelException
    {
        
        if (connectionString.contains("tcp://"))
        {
            ControlJMSChannelConsumer consumer = new ControlJMSChannelConsumer(
                            new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()),
                            connectionString,
                            containerRequestChannelName,
                            bsdlRegistry);
            consumer.setMessagesChecker(messagesCheker);
            return consumer;
        }

        if (connectionString.contains("stomp://") || connectionString.contains("ws://"))
        {
            ControlStompChannelConsumer consumer = new ControlStompChannelConsumer(
                            new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()),
                            connectionString,
                            containerRequestChannelName,
                            bsdlRegistry);
            consumer.setMessagesChecker(messagesCheker);
            return consumer;
        }
        
        return null;
        
    }
    

}
