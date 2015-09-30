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

package com.hi3project.vineyard.container;


import com.hi3project.broccoli.bsdl.api.ISemanticIdentifier;
import com.hi3project.broccoli.bsdl.api.ISemanticLocator;
import com.hi3project.broccoli.bsdl.api.registry.IBSDLRegistry;
import com.hi3project.broccoli.bsdf.api.deployment.bd.IServicesDB;
import com.hi3project.broccoli.bsdl.impl.registry.BSDLRegistry;
import com.hi3project.vineyard.registry.client.IdGenerator;
import com.hi3project.broccoli.bsdm.impl.parsing.BSDLBSDMLoader;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.vineyard.container.bd.ServicesSimpleDB;

/**
 * <p>
 * <b>Description:</b></p>
 *  General (abstract class) base implementation for a BSDM service container.
 *
 *
 * <p>
 * <b>Creation date:</b>
 * 23-01-2015 </p>
 *
 * <p>
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 23-01-2015 - Initial release</li>
 * </ul>
 *
 *
 * 
 * @version 1
 */
public abstract class AbstractBSDMServiceContainer
{

    protected ISemanticLocator workingLocation;

    protected BSDLRegistry bsdlRegistry;

    protected IServicesDB servicesDB;
    
    protected IdGenerator conversationIdGenerator;
    
    protected ISemanticIdentifier containerName;
    

    public AbstractBSDMServiceContainer(ISemanticLocator workingLocation, ISemanticIdentifier containerName) throws ModelException
    {
        this.setWorkingLocation(workingLocation);
        
        this.containerName = containerName;
        
        this.conversationIdGenerator = new IdGenerator();

        this.bsdlRegistry = new BSDLRegistry(new BSDLBSDMLoader());
        
        this.servicesDB = new ServicesSimpleDB();
    }
    

    public final void setWorkingLocation(ISemanticLocator location)
    {
        this.workingLocation = location;
    }

    public ISemanticLocator getWorkingLocation()
    {
        return this.workingLocation;
    }

    public IServicesDB getServicesDB()
    {
        return this.servicesDB;
    }

    public IBSDLRegistry getBSDLRegistry()
    {
        return bsdlRegistry;
    }
    
    protected static String getIPfromURL(String url)
    {
        String[] split = url.split(":");
        if (split.length < 3) return url;
        String ip = split[1].replaceAll("/", "");
        return ip;
    }
   

}
