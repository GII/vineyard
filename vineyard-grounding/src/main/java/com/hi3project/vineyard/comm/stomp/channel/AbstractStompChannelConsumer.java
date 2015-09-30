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
import com.hi3project.broccoli.bsdm.api.asyncronous.IAsyncMessageClient;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelConsumer;
import com.hi3project.broccoli.bsdm.api.asyncronous.IMessage;
import com.hi3project.broccoli.bsdl.impl.registry.BSDLRegistry;
import com.hi3project.vineyard.comm.stomp.StompMessageWrapper;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdm.api.serializing.IMessageSerializer;
import com.hi3project.broccoli.bsdm.impl.exceptions.SerializingException;
import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceGroundingException;
import com.hi3project.vineyard.comm.ReceivedMessagesChecker;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.hi3project.vineyard.comm.stomp.gozirraws.Listener;

/**
 * <p>
 * <b>Description:</b></p>
 * Implementation of IChannelCosumer for STOMP.
 *
 *
 * <p>
 * <b>Creation date:</b>
 * 09-01-2015 </p>
 *
 * <p>
 * <b>Changelog:</b></p>
 * <ul>
 * <li> 1 , 09-01-2015 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public abstract class AbstractStompChannelConsumer extends AbstractStompChannel implements IChannelConsumer
{

    protected StompMessageWrapper lastReceivedMessage = null;
    protected ReceivedMessagesChecker messagesChecker = null;

    public AbstractStompChannelConsumer(
            IMessageSerializer parameterValuesSerializer,
            String connectionURL,
            String name) throws ServiceGroundingException, ModelException
    {
        super(parameterValuesSerializer, connectionURL, name);
        this.initializeStompClientSubscription();
    }

    public AbstractStompChannelConsumer(
            IMessageSerializer parameterValuesSerializer,
            String connectionURL,
            String name,
            IBSDLRegistry bsdlRegistry) throws ServiceGroundingException
    {
        super(parameterValuesSerializer, connectionURL, bsdlRegistry, name);
        this.initializeStompClientSubscription();
    }

    private void initializeStompClientSubscription()
    {

        this.stompConnectionClient.subscribe(getChannelFullName(), new Listener()

        {

            @Override
            public synchronized void message(Map map, String string)
            {
                if (null != AbstractStompChannelConsumer.this.messagesChecker)
                {
                    Object messageId = map.get("message-id");
                    if (null != messageId && messageId instanceof String)
                    {
                        if (AbstractStompChannelConsumer.this.messagesChecker.isAlready((String) messageId))
                        {
                            return;
                        }
                        AbstractStompChannelConsumer.this.messagesChecker.addId((String) messageId);
                    }
                }
                AbstractStompChannelConsumer.this.lastReceivedMessage = new StompMessageWrapper(map, string);
                try
                {
                    AbstractStompChannelConsumer.this.signalReception();
                } catch (ModelException ex)
                {
                    Logger.getLogger(AbstractStompChannelConsumer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

    }

    @Override
    public IMessage getLastReceivedMessage() throws ServiceGroundingException
    {
        if (null != this.lastReceivedMessage)
        {
            try
            {
                return this.parameterValuesSerializer.deserializeMessage(this.lastReceivedMessage.getBody(), (BSDLRegistry) this.bsdlRegistry);
            } catch (SerializingException ex)
            {
                throw new ServiceGroundingException("Cannot build an IMessage from: " + this.lastReceivedMessage.getBody(), ex);
            }
        }
        return null;
    }

    @Override
    public IMessage receiveOrWait() throws ServiceGroundingException
    {
        throw new UnsupportedOperationException("Synchronous blocking reception not supported with Stomp.");
    }

    @Override
    public IMessage receiveOrWait(long ms) throws ServiceGroundingException
    {
        throw new UnsupportedOperationException("Synchronous blocking reception not supported with Stomp.");
    }

    @Override
    public void signalReception() throws ModelException
    {
        for (IAsyncMessageClient msgClient : this.clientCallbacks)
        {
            msgClient.receiveMessage(this.getLastReceivedMessage());
        }
    }

    public void setMessagesChecker(ReceivedMessagesChecker messagesChecker)
    {
        this.messagesChecker = messagesChecker;
    }

}
