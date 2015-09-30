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


import com.hi3project.broccoli.bsdl.api.ISemanticIdentifier;
import com.hi3project.broccoli.bsdl.api.registry.IBSDLRegistry;
import com.hi3project.broccoli.bsdm.api.asyncronous.IAsyncMessageClient;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelConsumer;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelProducer;
import com.hi3project.vineyard.registry.client.ClientRequester;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.vineyard.comm.ChannelFactory;


/**
 * <p>
 *  <b>Description:</b></p>
 *  Entity that holds the relevant information about a connection with
 * an external container. Meant for IBSDMServiceContainer implementations.
 *
 *
 * <p><b>Creation date:</b> 
 * 02-02-2015 </p>
 *
 * <p><b>Changelog:</b></p>
 * <ul>
 * <li> 1 , 02-02-2015 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class ConnectionWithExternalContainer 
{
    
    private volatile boolean connectionStarted;    
    
    private ISemanticIdentifier containerControlChannelIdentifier;
    
    private IChannelConsumer responseChannelConsumer = null;
    
    private IChannelProducer requestChannelProducer = null;
    
    private IBSDLRegistry bsdlRegistry = null;
    
    
    public ConnectionWithExternalContainer(
            ISemanticIdentifier containerControlChannelIdentifier,
            IBSDLRegistry bsdlRegistry)
    {
        this.containerControlChannelIdentifier = containerControlChannelIdentifier;
        this.connectionStarted = false;
        this.bsdlRegistry = bsdlRegistry;
    }
    

    public ISemanticIdentifier getContainerControlChannelIdentifier()
    {
        return containerControlChannelIdentifier;
    }

    public IChannelConsumer getResponseChannelConsumer()
    {
        return responseChannelConsumer;
    }

    public IChannelProducer getRequestChannelProducer()
    {
        return requestChannelProducer;
    }

    public boolean isConnectionStarted()
    {
        return connectionStarted;
    }
    
    
    public void connect() throws ModelException
    {   
        
        responseChannelConsumer =
                ChannelFactory.getSingleton().controlChannelConsumerInstanceFor(
                        this.getContainerControlChannelIdentifier().toString(),
                        ClientRequester.ContainerResponseChannelName,
                        this.bsdlRegistry);
        
        requestChannelProducer = 
                ChannelFactory.getSingleton().controlChannelProducerInstanceFor(
                        this.getContainerControlChannelIdentifier().toString(),
                        ClientRequester.ContainerRequestChannelName,
                        this.bsdlRegistry);
        
        this.connectionStarted = (null != requestChannelProducer) && (null != responseChannelConsumer);
        
    }
    
    
    public boolean addResponseCallback(IAsyncMessageClient callback)
    {
        if (!this.connectionStarted)
            return false;
        this.responseChannelConsumer.addClientCallback(callback);
        return true;
    }
        

}
