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

/**
 * (c)2005 Sean Russell
 */
public interface MessageReceiver {
  public void receive( Command c, Map h, String b );
  public void disconnect();
  public boolean isClosed();
}
