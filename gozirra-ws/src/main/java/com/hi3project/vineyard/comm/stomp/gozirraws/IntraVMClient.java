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

/**
 * A client that is connected directly to a server.  Messages sent via
 * this client do not go through a network interface, except when being
 * delivered to clients connected via the network... all messages to
 * other IntraVMClients are delivered entirely in memory.
 *
 * (c)2005 Sean Russell
 */
public class IntraVMClient extends Stomp implements Listener, Authenticatable {
  private Server _server;

  protected IntraVMClient( Server server ) {
    _server = server;
    _connected = true;
  }

  public boolean isClosed() { return false; }


  public Object token() {
    return "IntraVMClient";
  }


  /**
   * Transmit a message to clients and listeners.  
   */
  public void transmit( Command c, Map h, String b ) {
    _server.receive( c, h, b, this );
  }


  public void disconnect( Map h ) { 
    _server.receive( Command.DISCONNECT, null, null, this );
    _server = null;
  }

  public void message( Map headers, String body ) {
    receive( Command.MESSAGE, headers, body );
  }

  public void receipt( Map headers ) {
    receive( Command.RECEIPT, headers, null );
  }

  public void error( Map headers, String body ) {
    receive( Command.ERROR, headers, body );
  }
}
