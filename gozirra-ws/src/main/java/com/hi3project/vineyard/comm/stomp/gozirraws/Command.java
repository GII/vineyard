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

import java.io.OutputStream;
import java.io.IOException;

/**
 * (c)2005 Sean Russell
 */
public class Command {
  public final static String ENCODING = "US-ASCII";
  private String _command;

  private Command( String msg ) { 
    _command = msg;
  }
  public static Command SEND = new Command( "SEND" ),
         SUBSCRIBE = new Command( "SUBSCRIBE" ),
         UNSUBSCRIBE = new Command( "UNSUBSCRIBE" ),
         BEGIN = new Command( "BEGIN" ),
         COMMIT = new Command( "COMMIT" ),
         ABORT = new Command( "ABORT" ),
         DISCONNECT = new Command( "DISCONNECT" ),
         CONNECT = new Command( "CONNECT" );

  public static Command MESSAGE = new Command( "MESSAGE" ),
         RECEIPT = new Command( "RECEIPT" ),
         CONNECTED = new Command( "CONNECTED" ),
         ERROR = new Command( "ERROR" );

  public static Command valueOf( String v ) {
    v = v.trim();
    if (v.equals("SEND")) return SEND;
    else if (v.equals( "SUBSCRIBE" )) return SUBSCRIBE;
    else if (v.equals( "UNSUBSCRIBE" )) return UNSUBSCRIBE;
    else if (v.equals( "BEGIN" )) return BEGIN;
    else if (v.equals( "COMMIT" )) return COMMIT;
    else if (v.equals( "ABORT" )) return ABORT;
    else if (v.equals( "CONNECT" )) return CONNECT;
    else if (v.equals( "MESSAGE" )) return MESSAGE;
    else if (v.equals( "RECEIPT" )) return RECEIPT;
    else if (v.equals( "CONNECTED" )) return CONNECTED;
    else if (v.equals( "DISCONNECT" )) return DISCONNECT;
    else if (v.equals( "ERROR" )) return ERROR;
    throw new Error( "Unrecognised command "+v );
  }

  public String toString() {
    return _command;
  }
}

