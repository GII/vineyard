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

package com.hi3project.vineyard.container.micro;

import com.hi3project.broccoli.bsdl.api.ISemanticIdentifier;
import com.hi3project.broccoli.bsdl.api.ISemanticLocator;
import com.hi3project.broccoli.bsdf.api.deployment.bd.IServicesDB;
import com.hi3project.broccoli.bsdf.api.discovery.IRegistryLoader;
import com.hi3project.broccoli.bsdl.impl.registry.BSDLRegistry;
import com.hi3project.broccoli.bsdm.impl.parsing.BSDLBSDMLoader;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.vineyard.tools.Deployer;
import com.hi3project.vineyard.tools.Unpacker;
import java.util.Collection;

/**
 * <p>
 *  <b>Description:</b></p>
 *
 *
 * <p><b>Creation date:</b> 
 * 27-01-2015 </p>
 *
 * <p><b>Changelog:</b></p>
 * <ul>
 * <li> 1 , 27-01-2015 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class SimpleDeployerForMicroContainer extends Deployer
{
    
    private BSDLRegistry bsdlRegistry;
    
    private IRegistryLoader serviceLoader;
    
    
    public SimpleDeployerForMicroContainer(
            ISemanticLocator deploymentDirLocator,
            IRegistryLoader serviceLoader,
            IServicesDB servicesBD) throws ModelException
    {
        super(new Unpacker(), servicesBD, deploymentDirLocator);
        this.bsdlRegistry = new BSDLRegistry(new BSDLBSDMLoader());
        this.serviceLoader = serviceLoader;
    }
    
    
    public BSDLRegistry getBsdlRegistry()
    {
        return bsdlRegistry;
    }
    
    
    public Collection<ISemanticIdentifier> deployService(ISemanticLocator serviceLocator) throws ModelException
    {     
        return deployService(serviceLocator, bsdlRegistry, serviceLoader);
    }
    
    
    public void undeployService(ISemanticIdentifier serviceIdentifier) throws ModelException
    {
        undeployService(serviceIdentifier, bsdlRegistry, serviceLoader);
    }

}
