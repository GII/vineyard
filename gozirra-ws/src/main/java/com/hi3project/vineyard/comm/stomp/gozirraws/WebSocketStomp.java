/*******************************************************************************
 * 
 *   Copyright (C) 2015 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright (C) 2015 Alejandro Paz <alejandropl@lagostelle.com>
 *   Copyright (C) 2015 Victor Sonora <victor@vsonora.com>
 * 
 *   This file is part of Broccoli.
 *
 *   Broccoli is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Broccoli is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Broccoli.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ******************************************************************************/

package com.hi3project.vineyard.comm.stomp.gozirraws;

import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.net.URI;
import javax.security.auth.login.LoginException;

/**
 * Implements a Stomp client connection to a Stomp server via the network. This
 * implementation uses WebSockets
 *
 * Example:
 * <pre>
 *   Client c = new Client( "localhost", 61626, "ser", "ser" );
 *   c.subscribe( "/my/channel", new Listener() { ... } );
 *   c.subscribe( "/my/other/channel", new Listener() { ... } );
 *   c.send( "/other/channel", "Some message" );
 *   // ...
 *   c.disconnect();
 * </pre>
 *
 * @see Stomp
 *
 * (c)2005 Sean Russell
 */
public class WebSocketStomp extends Stomp implements MessageReceiver
{

    private WebSocketClientForStomp _socket;


    public WebSocketStomp(URI serverURI, String login, String pass)
            throws IOException, LoginException
    {
        try
        {

            _socket = new WebSocketClientForStomp(serverURI, this);
            _socket.connectBlocking();

            // Connect to the server
            HashMap header = new HashMap();
            header.put("login", login);
            header.put("passcode", pass);
            transmit(Command.CONNECT, header, null);

            this._connected = true;

            String error = null;
            while (!isConnected() && ((error = nextError()) == null))
            {
                Thread.sleep(100);
            }
            if (error != null)
            {
                throw new LoginException(error);
            }
        } catch (InterruptedException e)
        {
        }
    }

    public boolean isClosed()
    {
        return !_socket.isOpen();
    }

    public void disconnect(Map header)
    {
        if (!isConnected())
        {
            return;
        }
        transmit(Command.DISCONNECT, header, null);
        _socket.close();
        Thread.yield();
        _connected = false;
    }

    /**
     * Transmit a message to the server
     */
    public void transmit(Command c, Map h, String b)
    {
        try
        {
            Transmitter.transmit(c, h, b, _socket);
        } catch (Exception e)
        {
            receive(Command.ERROR, null, e.getMessage());
        }
    }
    

    @Override
    public String toString()
    {
        return "WSClient{" + "_socket=" + _socket + '}';
    }
            
    
}
