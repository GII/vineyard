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

import com.hi3project.broccoli.bsdl.api.ISemanticLocator;
import com.hi3project.broccoli.bsdm.api.asyncronous.IAsyncMessageClient;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelConsumer;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelProducer;
import com.hi3project.broccoli.bsdf.api.discovery.IClientRequester;
import com.hi3project.broccoli.bsdf.impl.asyncronous.SearchFunctionalityRequestMessage;
import com.hi3project.broccoli.bsdl.api.registry.IBSDLRegistry;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdl.impl.registry.BSDLRegistry;
import com.hi3project.broccoli.bsdm.impl.parsing.BSDLBSDMLoader;
import com.hi3project.broccoli.io.BSDFLogger;
import com.hi3project.broccoli.io.DescriptorReader;
import com.hi3project.vineyard.comm.ChannelFactory;


/**
 * <p>
 * <b>Description:</b></p>
 * An object that knows how to communicate with a remote BSDF container
 *
 *
 * <p>
 * Colaborations:
 *
 * <ul>
 * <li>an IChannelConsumer to receive responses coming from the container</li>
 * <li>an IChannelProducer to send requests or submit information to the
 * container</li>
 * </ul>
 *
 * <p>
 * Responsabilities:
 *
 * <ul>
 * <li>can ask the container about a functionality request</li>
 * </ul>
 *
 * <p>
 * <b>Creation date:</b>
 * 23-09-2014 </p>
 *
 * <p>
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 23-09-2014 - Initial release</li>
 * </ul>
 *
 *
 * 
 * @version 1
 */
public class ClientRequester implements IClientRequester
{
 
    public static final String ContainerRequestChannelName = "Container control channel - request";
    public static final String ContainerResponseChannelName = "Container control channel - response";

    private String serviceProviderURL = null;
    private String clientName = null;

    private IChannelConsumer responseChannelConsumer;
    private IChannelProducer requestChannelProducer;

    private IdGenerator conversationIdGenerator;

    
    public ClientRequester(String clientName) throws ModelException
    {
        this(clientName, BrokerConnectorsConfig.openWireConnectorURL, new BSDLRegistry(new BSDLBSDMLoader()));
    }

    public ClientRequester(String clientName, String containerURL, IBSDLRegistry bsdlRegistry) throws ModelException
    {
        this.conversationIdGenerator = new IdGenerator();
        this.serviceProviderURL = containerURL;
        this.clientName = clientName;
        responseChannelConsumer = ChannelFactory.getSingleton().controlChannelConsumerInstanceFor(this.serviceProviderURL, ContainerResponseChannelName, bsdlRegistry);
        requestChannelProducer = ChannelFactory.getSingleton().controlChannelProducerInstanceFor(this.serviceProviderURL, ContainerRequestChannelName, bsdlRegistry);
        BSDFLogger.getLogger().info("Instances a ClientRequester for: " + clientName + " connecting to: " + containerURL);
    }
    
    public ClientRequester(String clientName, String containerURL) throws ModelException
    {
        this(clientName, containerURL, null);
    }

    @Override
    public void askForFunctionality(ISemanticLocator requestedFunctionalityLocator, IAsyncMessageClient callback) throws ModelException
    {

        this.responseChannelConsumer.addClientCallback(callback);
        String requestedFunctionalityContents = new DescriptorReader(requestedFunctionalityLocator).readAsString();
        SearchFunctionalityRequestMessage sfRequestMessage
                = new SearchFunctionalityRequestMessage(this.clientName, this.conversationIdGenerator.getNextIdS());
        sfRequestMessage.addRequestedFunctionality(requestedFunctionalityContents);
        BSDFLogger.getLogger().debug("Sends SearchFunctionalityRequestMessage for client: " + this.clientName
                        + "using Id: " + sfRequestMessage.getConversationId());
        this.requestChannelProducer.send(sfRequestMessage);

    }

    
    public String getClientName()
    {
        return clientName;
    }

}
