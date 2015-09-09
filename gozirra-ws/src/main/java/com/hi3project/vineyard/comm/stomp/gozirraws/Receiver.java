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

import java.io.*;
import java.util.HashMap;

/**
 * (c)2005 Sean Russell
 */
public class Receiver extends Thread {
  private MessageReceiver _receiver;
  private BufferedReader _input;
  private InputStream _stream;

  protected Receiver() {
    super();
  }
  public Receiver( MessageReceiver m, InputStream input ) { 
    super();
    setup( m, input );
  }

  protected void setup( MessageReceiver m, InputStream input ) {
    _receiver = m;
    try {
      _stream = input;
      _input = new BufferedReader(new InputStreamReader(input,Command.ENCODING));
    } catch (UnsupportedEncodingException e) {
      // No, no, no.  Stupid Java.
    }
  }
  
  public static void receive(MessageReceiver _receiver, BufferedReader _input) throws IOException
  {
      String command = _input.readLine();
          if (command.length() > 0) {
            try {
              Command c = Command.valueOf( command );
              // Get headers
              HashMap headers = new HashMap();
              String header;
              while ((header = _input.readLine()).length() > 0) {
                int ind = header.indexOf( ':' );
                String k = header.substring( 0, ind );
                String v = header.substring( ind+1, header.length() );
                headers.put(k.trim(),v.trim());
              }
              // Read body
              StringBuilder body = new StringBuilder();
              int b;
              while ((b = _input.read()) != 0) {
                body.append( (char)b );
              }

              try {
                _receiver.receive( c, headers, body.toString() );
              } catch (Exception e) {
                // We ignore these errors; we don't want client code
                // crashing our listener.
              }
            } catch (Error e) {
              try {
                while (_input.read() != 0);
              } catch (Exception ex) { }
              try {
                _receiver.receive( Command.ERROR, null, e.getMessage()+"\n" );
              } catch (Exception ex) {
                // We ignore these errors; we don't want client code
                // crashing our listener.
              }
            }
          }
  }

  @Override
  public void run() {
    // Loop reading from stream, calling receive()
    try {
      while (!isInterrupted()) {
        // Get command
        if (_input.ready()) {
            receive(_receiver, _input);
        } else {
          if (_receiver.isClosed()) {
            _receiver.disconnect();
            return;
          }
          try {Thread.sleep(200);}catch(InterruptedException e){interrupt();}
        }
      }
    } catch (IOException e) {
      // What do we do with IO Exceptions?  Report it to the receiver, and 
      // exit the thread.
      System.err.println("Stomp exiting because of exception");
      e.printStackTrace( System.err );
      _receiver.receive( Command.ERROR, null, e.getMessage() );
    } catch (Exception e) {
      System.err.println("Stomp exiting because of exception");
      e.printStackTrace( System.err );
      _receiver.receive( Command.ERROR, null, e.getMessage() );
    }
  }
}
