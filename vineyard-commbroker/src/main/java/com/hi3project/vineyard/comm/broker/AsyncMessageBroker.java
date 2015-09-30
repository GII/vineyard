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

package com.hi3project.vineyard.comm.broker;

import com.hi3project.broccoli.bsdm.api.IServiceDescription;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelConsumer;
import com.hi3project.broccoli.bsdm.api.asyncronous.IMessageBroker;
import com.hi3project.broccoli.bsdf.api.deployment.container.IBSDMServiceContainer;
import com.hi3project.broccoli.bsdm.api.grounding.IServiceGrounding;
import com.hi3project.broccoli.bsdm.impl.grounding.AsyncMessageServiceGrounding;
import com.hi3project.vineyard.comm.jms.channel.AbstractJMSChannelConsumer;
import com.hi3project.vineyard.comm.stomp.channel.AbstractStompChannelConsumer;
import com.hi3project.broccoli.bsdf.exceptions.MessageBrokerException;
import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceGroundingException;
import com.hi3project.broccoli.io.BSDFLogger;
import com.hi3project.vineyard.registry.client.BrokerConnectorsConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;

/**
 * <p>
 * <b>Description: </b>
 * A message broker for BSDF services based on ActiveMQ BrokerService, and
 * AsyncMessageServiceGrounding. It holds reference to the registered service
 * groundings , and for each one knows an ActiveMQ TransportConnector opened for
 * that service grounding.
 *
 * <p>
 * It also knows all the IChannelConsumer related to the registered service
 * groundings, used to receive messages syncronously.
 *
 *
 * <p>
 * <b>Creation date:</b>
 * 24-06-2014 </p>
 *
 * <p>
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 24-06-2014 - Initial release</li>
 * </ul>
 *
 *
 * 
 * @version 1
 */
public class AsyncMessageBroker implements IMessageBroker
{

    private BrokerService broker = null;

    private String name = null;

    private Collection<AsyncMessageServiceGrounding> serviceGroundings = new ArrayList<AsyncMessageServiceGrounding>();
    private Collection<IChannelConsumer> serviceChannelConsumers = new ArrayList<IChannelConsumer>();

    private IBSDMServiceContainer serviceContainer;
    

    public AsyncMessageBroker(String name)
    {
        BSDFLogger.getLogger().info("Instances an AsyncMessageBroker: " + name);
        this.name = name;
    }

    @Override
    public synchronized void setMainControlChannel(IBSDMServiceContainer serviceContainer) throws MessageBrokerException
    {
        BSDFLogger.getLogger().info("Sets control channer for: " + serviceContainer.toString());
        try
        {
            this.serviceContainer = serviceContainer;
        } catch (Exception ex)
        {
            throw new MessageBrokerException(
                    "Cannot register container connector for: " + serviceContainer.toString(),
                    ex);
        }
    }

    @Override
    public synchronized void start() throws MessageBrokerException
    {
        BSDFLogger.getLogger().info("Starting broker");

        try
        {

            if (null == this.broker)
            {
                this.broker = initBroker(name);
            }

            this.broker.start();

            // each service grounding has a consumer channel that is activated AFTER
            //ServiceBroker has started            
            for (AsyncMessageServiceGrounding serviceGrounding : this.serviceGroundings)
            {
                this.serviceChannelConsumers.add(serviceGrounding.activateChannelConsumer());
            }

            // the container channel is also activated
            if (null != this.serviceContainer)
            {
                this.serviceContainer.activateControlChannels();
            }

        } catch (Exception ex)
        {
            throw new MessageBrokerException("Error starting broker: " + this.name, ex);
        }
        
    }

    @Override
    public synchronized void stop() throws MessageBrokerException
    {
        BSDFLogger.getLogger().info("Stoping broker");

        try
        {
            if (null != this.broker)
            {

                List<TransportConnector> transportConnectors = this.broker.getTransportConnectors();
                for (TransportConnector transportConnector : transportConnectors)
                {
                    transportConnector.stop();
                }

                this.broker.stop();
                this.broker = null;
                
                serviceGroundings = new ArrayList<AsyncMessageServiceGrounding>();
                serviceChannelConsumers = new ArrayList<IChannelConsumer>();
            }
        } catch (Exception ex)
        {
            throw new MessageBrokerException("Error stoping broker: " + this.name, ex);
        }
    }

    @Override
    public synchronized void restart() throws MessageBrokerException
    {
        this.stop();
        this.setMainControlChannel(this.serviceContainer);
        this.start();
    }
   

    @Override
    public synchronized void registerService(IServiceDescription serviceDescription) throws MessageBrokerException, ServiceGroundingException
    {
        for (IServiceGrounding grounding : serviceDescription.getGroundings())
        {
            if (grounding instanceof AsyncMessageServiceGrounding)
            {
                AsyncMessageServiceGrounding amsGrounding = (AsyncMessageServiceGrounding) grounding;
                this.serviceGroundings.add(amsGrounding);
            }
        }

    }
    

    /*
     Set this broker object to receive messages during a given period of time, blocking execution.
     */
    public synchronized void receiveMessagesSyncronously(long ms) throws ServiceGroundingException
    {
        for (IChannelConsumer chConsumer : this.serviceChannelConsumers)
        {
            if (chConsumer instanceof AbstractJMSChannelConsumer)
            {
                AbstractJMSChannelConsumer achConsumer = (AbstractJMSChannelConsumer) chConsumer;
                achConsumer.receiveOrWait(ms);
            }
            if (chConsumer instanceof AbstractStompChannelConsumer)
            {
                AbstractStompChannelConsumer achConsumer = (AbstractStompChannelConsumer) chConsumer;
                achConsumer.receiveOrWait(ms);
            }
        }
    }

    private static BrokerService initBroker(String name) throws Exception
    {
        BrokerService broker = new BrokerService();
        
        broker.setBrokerName(name);
        broker.setPersistent(false);
        broker.setUseJmx(false);
        broker.setUseShutdownHook(false);
        
        broker.addConnector(BrokerConnectorsConfig.openWireConnectorURL);
        broker.addConnector(BrokerConnectorsConfig.stompConnectorURL);
        broker.addConnector(BrokerConnectorsConfig.wsConnectorURL);

        return broker;
    }

}
