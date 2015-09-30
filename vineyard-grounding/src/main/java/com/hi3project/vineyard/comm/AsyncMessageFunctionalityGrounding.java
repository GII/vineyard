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
import com.hi3project.broccoli.bsdl.impl.exceptions.SemanticModelException;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelConsumer;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelProducer;
import com.hi3project.broccoli.bsdm.api.serializing.IMessageSerializer;
import com.hi3project.broccoli.bsdm.impl.asyncronous.ChannelManager;
import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceGroundingException;
import com.hi3project.broccoli.bsdm.impl.grounding.AbstractFunctionalityGrounding;
import com.hi3project.broccoli.bsdm.impl.grounding.AsyncMessageServiceGrounding;


/**
 *
 * <p> Extends AbstractFunctionalityGrounding and uses ChannelFactory to
 * build the channels for async messaging </p>
 *
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 29-04-2015 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class AsyncMessageFunctionalityGrounding extends AbstractFunctionalityGrounding
{

    public AsyncMessageFunctionalityGrounding(
            IMessageSerializer messageSerializer,
            Instance instance,
            AsyncMessageServiceGrounding serviceGrounding,
            ChannelManager channelHelper)
            throws SemanticModelException
    {
        super(instance, serviceGrounding, channelHelper, messageSerializer);
    }
    
    
    @Override
    protected IChannelConsumer createClientChannelFor(
            String functionalityName,
            String client,
            String conversationID) throws ServiceGroundingException
    {
        
        try
        {
            
            return ChannelFactory.getSingleton().resultsChannelConsumerInstanceFor(
                    ((AsyncMessageServiceGrounding)this.serviceGrounding).getConnectorURL(),
                    functionalityName + "_" + client + "_" + conversationID,
                    this.serviceGrounding.getServiceDescription().getBSDLRegistry());
            
        } catch (ModelException ex)
        {
            throw new ServiceGroundingException(ex.getMessage(), ex);
        }
        
    }
    
    
    @Override
    protected IChannelProducer createProviderChannelFor(
            String functionalityName,
            String client,
            String conversationID) throws ServiceGroundingException
    {
        
        try
        {
            
            return ChannelFactory.getSingleton().resultsChannelProducerInstanceFor(
                    ((AsyncMessageServiceGrounding)this.serviceGrounding).getConnectorURL(),
                    functionalityName + "_" + client + "_" + conversationID,
                    this.serviceGrounding.getServiceDescription().getBSDLRegistry());
            
        } catch (ModelException ex)
        {
            throw new ServiceGroundingException(ex.getMessage(), ex);
        }
        
    }

    @Override
    protected IChannelConsumer createClientSubscriptionChannelFor(String functionalityName) throws ServiceGroundingException
    {
        try
        {
            return ChannelFactory.getSingleton().controlChannelConsumerInstanceFor(
                    ((AsyncMessageServiceGrounding)this.serviceGrounding).getConnectorURL(),
                    functionalityName + "_subscription",
                    this.serviceGrounding.getServiceDescription().getBSDLRegistry());
        } catch (ModelException ex)
        {
            throw new ServiceGroundingException(ex.getMessage(), ex);
        }
    }

    @Override
    protected IChannelProducer createProviderSubscriptionChannelFor(String functionalityName) throws ServiceGroundingException
    {
        
        try
        {
            
            return ChannelFactory.getSingleton().controlChannelProducerInstanceFor(
                    ((AsyncMessageServiceGrounding)this.serviceGrounding).getConnectorURL(),
                    functionalityName + "_subscription",
                    this.serviceGrounding.getServiceDescription().getBSDLRegistry());
            
        } catch (ModelException ex)
        {
            throw new ServiceGroundingException(ex.getMessage(), ex);
        }
        
    }
    
}
