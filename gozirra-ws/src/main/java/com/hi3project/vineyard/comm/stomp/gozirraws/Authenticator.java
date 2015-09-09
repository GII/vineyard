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

import javax.security.auth.login.LoginException;

public interface Authenticator {
  /**
   * Validates a user.
   *
   * @param user the user's login
   * @param pass the user's passcode
   * @return a token which will be used for future authorization requests
   */
  public Object connect( String user, String pass ) throws LoginException;
  
  /**
   * Authorizes a send request.
   *
   * @param channel the channel the user is attempting to send to
   * @param token the token returned by a previous call to connect.
   */
  public boolean authorizeSend( Object token, String channel );

  /**
   * Authorizes a Subscribe request.
   *
   * @param channel the channel the user is attempting to subscribe to
   * @param token the token returned by a previous call to connect.
   */
  public boolean authorizeSubscribe( Object token, String channel );
}
