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
import com.hi3project.broccoli.bsdm.api.asyncronous.IAsyncMessageClient;
import com.hi3project.broccoli.bsdm.impl.grounding.AbstractChannel;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdm.api.serializing.IMessageSerializer;
import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceGroundingException;
import com.hi3project.broccoli.io.BSDFLogger;
import com.hi3project.vineyard.comm.ReceivedMessagesChecker;
import java.util.ArrayList;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * <p>
 *  <b>Description:</b></p>
 *  Basic implementation (meant to be subclassed) for an asyncronous channel
 * based on ActiveMQ/JMS.
 *
 *
 * <p>
 *  Colaborations:
 *
 * <ul>
 * <li>a JMS Connection is used or created.</li>
 * <li>a JMS Session is created for the represented channel.</li>
 * <li>a Collection of IAsyncMessageClient callbacks is handled.</li>
 * </ul>
 * 
 * <p>
 *  Responsabilities:
 *
 * <ul>
 * <li>knows how to initialize a JMS session</li>
 * </ul>
 *
 * <p>
 * <b>Creation date:</b>
 * 23-06-2014 </p>
 *
 * <p>
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 23-06-2014 - Initial release</li>
 * </ul>
 *
 *
 * 
 * @version 1
 */
public abstract class AbstractJMSAMQChannel extends AbstractChannel
{

    protected Connection connection = null;
    protected Session session = null;
    

  
    public AbstractJMSAMQChannel(
            IMessageSerializer messageSerializer,
            String connectionURL,
            String name) throws ServiceGroundingException, ModelException            
    {
        super(messageSerializer, name);
        
        BSDFLogger.getLogger().info("JMS based channel connecting to: " + connectionURL);
        
        this.initializeConnection(connectionURL);
    }

    
    public AbstractJMSAMQChannel(
            IMessageSerializer messageSerializer,
            String connectionURL,
            IBSDLRegistry bsdlRegistry,
            String name) throws ServiceGroundingException
    {
        super(messageSerializer, bsdlRegistry, name);
        
        BSDFLogger.getLogger().info("JMS based channel connecting to: " + connectionURL);
        
        this.initializeConnection(connectionURL);
    }
    
    
    private void initializeConnection(String connectionURL) throws ServiceGroundingException 
    {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(connectionURL);
        try
        {
            this.connection = connectionFactory.createConnection();
            this.initialize(connection);
        } catch (JMSException ex)
        {
            throw new ServiceGroundingException("Cannot open connection to: " + connectionURL, ex);
        } catch (ModelException ex)
        {
            throw new ServiceGroundingException("Cannot open connection to: " + connectionURL, ex);
        }
    }
    
    private void initialize(Connection connection) throws ServiceGroundingException
    {
        try
        {
            this.connection = connection;
            this.connection.start();
            this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException ex)
        {
            throw new ServiceGroundingException("Cannot open connection to: " + connection.toString(), ex);
        }
        this.clientCallbacks = new ArrayList<IAsyncMessageClient>();
    }

    
    public Connection getJMSConnection()
    {
        return this.connection;
    }
      
    
    public void close() throws ServiceGroundingException
    {
        try
        {
            this.session.close();
            this.connection.close();
        } catch (JMSException ex)
        {
            throw new ServiceGroundingException("Error closing JMS session and connection", ex);
        }
    }

}
