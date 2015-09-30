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
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelConsumer;
import com.hi3project.broccoli.bsdm.api.asyncronous.IMessage;
import com.hi3project.broccoli.bsdl.impl.registry.BSDLRegistry;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdm.api.serializing.IMessageSerializer;
import com.hi3project.broccoli.bsdm.impl.exceptions.SerializingException;
import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceGroundingException;
import com.hi3project.vineyard.comm.ReceivedMessagesChecker;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * <p>
 * <b>Description:</b></p>
 *  Implementation of IChannelConsumer that uses the JMS API
 *
 *
 * <p>
 * <b>Creation date:</b>
 * 25-06-2014 </p>
 *
 * <p>
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 25-06-2014 - Initial release</li>
 * </ul>
 *
 *
 * 
 * @version 1
 */
public abstract class AbstractJMSChannelConsumer extends AbstractJMSAMQChannel implements IChannelConsumer
{

    protected MessageConsumer messageConsumer = null;
    protected Message lastMessage = null;
    protected ReceivedMessagesChecker messagesChecker = null;
    

    public AbstractJMSChannelConsumer(
            IMessageSerializer messageSerializer,
            String connection,
            String name) throws ModelException
    {

        super(messageSerializer, connection, name);
        this.completeInitialization(name);

    }

    public AbstractJMSChannelConsumer(
            IMessageSerializer messageSerializer,
            String connection,
            String name,
            IBSDLRegistry bsdlRegistry) throws ServiceGroundingException
    {

        super(messageSerializer, connection, bsdlRegistry, name);
        this.completeInitialization(name);

    }

    protected abstract MessageConsumer initializeMessageConsumer(String name) throws ServiceGroundingException;

    private void completeInitialization(String name) throws ServiceGroundingException
    {
        this.messageConsumer = initializeMessageConsumer(name);
        try
        {

            this.messageConsumer.setMessageListener(new MessageListener()
            {
                @Override
                public synchronized void onMessage(Message message)
                {
                    if (null != AbstractJMSChannelConsumer.this.messagesChecker)
                    {
                        try
                        {
                            if (AbstractJMSChannelConsumer.this.messagesChecker.isAlready(message.getJMSMessageID()))                            
                            {
                                return;
                            }
                            AbstractJMSChannelConsumer.this.messagesChecker.addId(message.getJMSMessageID());
                        } catch (JMSException ex)
                        {
                            Logger.getLogger(AbstractJMSChannelConsumer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    AbstractJMSChannelConsumer.this.setLastMessage(message);
                    try
                    {
                        AbstractJMSChannelConsumer.this.signalReception();
                    } catch (ModelException ex)
                    {
                        Logger.getLogger(ResultsJMSChannelConsumer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } catch (JMSException ex)
        {
            throw new ServiceGroundingException("Error opening channel: " + name, ex);
        }
    }

    @Override
    public IMessage getLastReceivedMessage() throws ServiceGroundingException
    {
        if (null != getLastMessage()
                && getLastMessage() instanceof TextMessage)
        {
            TextMessage msg = (TextMessage) getLastMessage();
            try
            {
                return this.parameterValuesSerializer.deserializeMessage(msg.getText(), (BSDLRegistry) this.bsdlRegistry);
            } catch(JMSException ex)
            {
                throw new ServiceGroundingException("Cannot build an IMessage from: " + msg.toString(), ex);
            } catch(SerializingException ex)
            {
                throw new ServiceGroundingException("Cannot build an IMessage from: " + msg.toString(), ex);
            }
        }
        return null;
    }

    @Override
    public IMessage receiveOrWait() throws ServiceGroundingException
    {
        try
        {
            this.setLastMessage(messageConsumer.receive());
            return this.getLastReceivedMessage();
        } catch (JMSException ex)
        {
            throw new ServiceGroundingException("Error receiving a message", ex);
        }
    }
    
    @Override
    public IMessage receiveOrWait(long ms) throws ServiceGroundingException
    {
        try
        {
            this.setLastMessage(messageConsumer.receive(ms));
            return this.getLastReceivedMessage();
        } catch (JMSException ex)
        {
            throw new ServiceGroundingException("Error receiving a message", ex);
        }
    }

    @Override
    public void signalReception() throws ModelException
    {
        for (IAsyncMessageClient msgClient : this.clientCallbacks)
        {
            msgClient.receiveMessage(this.getLastReceivedMessage());
        }
    }
    
    public synchronized Message getLastMessage()
    {
        return this.lastMessage;
    }
    
    public synchronized void setLastMessage(Message msg)
    {
        this.lastMessage = msg;
    }
    
    public void setMessagesChecker(ReceivedMessagesChecker messagesChecker)
    {
        this.messagesChecker = messagesChecker;
    } 
    
}
