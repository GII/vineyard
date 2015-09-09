/*******************************************************************************
 * 
 *   Copyright (C) 2005 Sean Russell
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
import java.util.Iterator;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * (c)2005 Sean Russell
 */
class Transmitter
{

    private static byte [] getTextFormattedAsBytes(Command c, Map h, String b) throws UnsupportedEncodingException
    {
        StringBuilder message = new StringBuilder(c.toString());
        message.append("\n");

        if (h != null)
        {
            for (Iterator keys = h.keySet().iterator(); keys.hasNext();)
            {
                String key = (String) keys.next();
                String value = (String) h.get(key);
                message.append(key);
                message.append(":");
                message.append(value);
                message.append("\n");
            }
        }
        message.append("\n");

        if (b != null)
        {
            message.append(b);
        }

        message.append("\000");
        
        return getTextFormatted(c, h, b).getBytes(Command.ENCODING);
    }
    
    private static String getTextFormatted(Command c, Map h, String b) throws UnsupportedEncodingException
    {
        StringBuilder message = new StringBuilder(c.toString());
        message.append("\n");

        if (h != null)
        {
            for (Iterator keys = h.keySet().iterator(); keys.hasNext();)
            {
                String key = (String) keys.next();
                String value = (String) h.get(key);
                message.append(key);
                message.append(":");
                message.append(value);
                message.append("\n");
            }
        }
        message.append("\n");

        if (b != null)
        {
            message.append(b);
        }

        message.append("\000");
        
        return message.toString();
    }

    
    public static void transmit(Command c, Map h, String b,
            java.io.OutputStream out) throws IOException
    {

        out.write(getTextFormattedAsBytes(c, h, b));
        
    }

    
    public static void transmit(Command c, Map h, String b,
            WebSocketClientForStomp wsClient) throws IOException
    {

        wsClient.send(getTextFormatted(c, h, b));
        
    }
    
}
