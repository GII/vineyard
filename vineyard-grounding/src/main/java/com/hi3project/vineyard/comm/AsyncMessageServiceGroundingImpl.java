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

import com.hi3project.broccoli.bsdl.impl.Instance;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdm.api.IServiceDescription;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelConsumer;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelProducer;
import com.hi3project.broccoli.bsdm.api.grounding.IFunctionalityGroundingFactory;
import com.hi3project.broccoli.bsdm.api.grounding.IServiceGrounding;
import com.hi3project.broccoli.bsdm.api.serializing.IMessageSerializer;
import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceGroundingException;
import com.hi3project.broccoli.bsdm.impl.grounding.AsyncMessageServiceGrounding;

/**
 * <p> Extends AsyncMessageServiceGrounding and uses ChannelFactory to
 * build the channels for async messaging </p>
 *
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 08-05-2015 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class AsyncMessageServiceGroundingImpl extends AsyncMessageServiceGrounding
{
    
    public AsyncMessageServiceGroundingImpl(
            IMessageSerializer messageSerializer,
            IFunctionalityGroundingFactory funcGroundingFactory,
            Instance instance,
            IServiceDescription serviceDescription) throws ModelException
    {
        super(funcGroundingFactory, messageSerializer, instance, serviceDescription);
    }
    

    @Override
    protected IChannelProducer newChannelProducer() throws ServiceGroundingException
    {
        try
        {
            
            return ChannelFactory.getSingleton().controlChannelProducerInstanceFor(
                    this.getConnectorURL(),
                    this.controlChannelName,
                    this.getServiceDescription().getBSDLRegistry());
            
        } catch (ModelException ex)
        {
            throw new ServiceGroundingException(ex.getMessage(), ex);
        }
    }

    @Override
    protected IChannelConsumer newChannelConsumer() throws ServiceGroundingException
    {
        
        try
        {
            
            return ChannelFactory.getSingleton().controlChannelConsumerInstanceFor(
                    this.getConnectorURL(),
                    this.controlChannelName,
                    this.getServiceDescription().getBSDLRegistry());
            
        } catch (ModelException ex)
        {
            throw new ServiceGroundingException(ex.getMessage(), ex);
        }
        
    }

    @Override
    protected IServiceGrounding newInstance() throws ModelException
    {
        IServiceGrounding newInstance = 
                ServiceGroundingFactory.getSingleton().instanceFor(
                        FunctionalityGroundingFactory.getSingleton(),
                        this.connectorURL,
                        (Instance)this.instance,
                        service, 
                        messageSerializer);
        
        return newInstance;
    }

}
