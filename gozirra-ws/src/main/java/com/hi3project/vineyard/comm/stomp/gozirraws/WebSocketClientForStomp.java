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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

/**
 *
 * <p>
 * <b>Creation date:</b>
 * 19-01-2015 </p>
 *
 * <p>
 * <b>Changelog:</b>
 *
 * <ul>
 * <li> 1 , 19-01-2015 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class WebSocketClientForStomp extends WebSocketClient
{

    private boolean open = false;

    private MessageReceiver _receiver;

    public WebSocketClientForStomp(URI serverURI, MessageReceiver m)
    {
        super(serverURI, new Draft_17());
        _receiver = m;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata)
    {
        this.open = true;
        System.out.println("new connection opened: " + handshakedata.getHttpStatusMessage());
    }

    @Override
    public void onClose(int code, String reason, boolean remote)
    {
        System.out.println("closed with exit code " + code + " additional info: " + reason);
        this.open = false;
    }

    @Override
    public void onMessage(String message)
    {
        
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(message.getBytes())));
        try
        {
            Receiver.receive(_receiver, br);
        } catch (IOException ex)
        {
            Logger.getLogger(WebSocketClientForStomp.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void onError(Exception ex)
    {
        System.err.println("an error occured:" + ex);
    }

    public boolean isOpen()
    {
        return this.open;
    }

}
