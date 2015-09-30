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
import com.hi3project.broccoli.bsdm.impl.grounding.AbstractChannel;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdm.api.serializing.IMessageSerializer;
import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceGroundingException;
import com.hi3project.broccoli.io.BSDFLogger;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.security.auth.login.LoginException;
import com.hi3project.vineyard.comm.stomp.gozirraws.Client;
import com.hi3project.vineyard.comm.stomp.gozirraws.Stomp;
import com.hi3project.vineyard.comm.stomp.gozirraws.WebSocketStomp;

/**
 * <p>
 * <b>Description:</b></p>
 * An extension of AbstractChannel that handles STOMP specific behaviour
 *
 *
 * <p>
 * Colaborations:
 *
 * <ul>
 * <li>with Client of the Gozirra library (a simple STOMP implementation)</li>
 * </ul>
 *
 * <p>
 * Responsabilities:
 *
 * <ul>
 * <li>it knows how to initialize a STOMP Client</li>
 * </ul>
 *
 * <p>
 * <b>Creation date:</b>
 * 09-01-2015 </p>
 *
 * <p>
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 09-01-2015 - Initial release</li>
 * </ul>
 *
 *
 * 
 * @version 1
 */
public abstract class AbstractStompChannel extends AbstractChannel
{

    protected static final String DEFAULT_USER = "";

    protected static final String DEFAULT_PASSWORD = "";

    protected Stomp stompConnectionClient = null;

    public AbstractStompChannel(
            IMessageSerializer parameterValuesSerializer,
            String connectionURL,
            String name) throws ServiceGroundingException, ModelException
    {

        super(parameterValuesSerializer, name);

        BSDFLogger.getLogger().info("STOMP channel connecting to: " + connectionURL);
        
        initializeClient(connectionURL);

    }

    public AbstractStompChannel(
            IMessageSerializer parameterValuesSerializer,
            String connectionURL,
            IBSDLRegistry bsdlRegistry, String name) throws ServiceGroundingException
    {

        super(parameterValuesSerializer, bsdlRegistry, name);

        initializeClient(connectionURL);

    }

    private void initializeClient(String connectionURL) throws ServiceGroundingException
    {

        String[] urlSplit = connectionURL.split("://");
        if (urlSplit.length == 2)
        {
            if (urlSplit[0].equals("stomp"))
            {
                String[] ipAndPortSplit = urlSplit[1].split(":");
                if (ipAndPortSplit.length == 2)
                {
                    String ip = ipAndPortSplit[0];
                    int port = Integer.parseInt(ipAndPortSplit[1]);

                    try
                    {
                        this.stompConnectionClient = new Client(ip, port, DEFAULT_USER, DEFAULT_PASSWORD);
                    } catch (IOException ex)
                    {
                        throw new ServiceGroundingException("Opening Stomp client for: " + connectionURL, ex);
                    } catch (LoginException ex)
                    {
                        throw new ServiceGroundingException("Opening Stomp client for: " + connectionURL, ex);
                    } // try

                } // if

            } else if (urlSplit[0].equals("ws"))
            {
                try
                {
                    this.stompConnectionClient = new WebSocketStomp(new URI(connectionURL), DEFAULT_USER, DEFAULT_PASSWORD);
                } catch (IOException ex)
                {
                    throw new ServiceGroundingException("Opening Stomp client for: " + connectionURL, ex);
                } catch (LoginException ex)
                {
                    throw new ServiceGroundingException("Opening Stomp client for: " + connectionURL, ex);
                } catch (URISyntaxException ex)
                {
                    throw new ServiceGroundingException("Opening Stomp client for: " + connectionURL, ex);
                }
            }

        } // if

        if (null == this.stompConnectionClient) 
            throw new ServiceGroundingException("Cannot open Stomp client for: " + connectionURL);
    }

    public Stomp getStompConnectionClient()
    {
        return this.stompConnectionClient;
    }
    
    public void close() throws ServiceGroundingException
    {
        this.stompConnectionClient.disconnect();
        this.stompConnectionClient = null;
    }

    protected abstract String getChannelFullName();
    

}
