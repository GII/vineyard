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

package com.hi3project.vineyard.container.yotta;

import com.hi3project.broccoli.bsdl.api.ISemanticIdentifier;
import com.hi3project.broccoli.bsdf.api.deployment.bd.IServicesDB;
import com.hi3project.broccoli.bsdl.api.ISemanticLocator;
import com.hi3project.broccoli.bsdf.api.deployment.IUnpacker;
import com.hi3project.broccoli.bsdf.api.discovery.IServiceRegistry;
import com.hi3project.broccoli.bsdf.impl.serializing.JSONMessageSerializer;
import com.hi3project.broccoli.bsdl.api.registry.IBSDLRegistry;
import com.hi3project.broccoli.bsdl.impl.registry.BSDLRegistry;
import com.hi3project.broccoli.bsdm.impl.parsing.BSDLBSDMLoader;
import com.hi3project.vineyard.registry.server.BSDMServiceRegistry;
import com.hi3project.vineyard.registry.server.SimpleStructuralMatchmaker;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.vineyard.comm.FunctionalityGroundingFactory;
import com.hi3project.vineyard.comm.ServiceGroundingFactory;
import com.hi3project.vineyard.tools.Deployer;
import java.util.Collection;

/**
 * <p><b>Description:</b></p>
 *
 *
 * <p><b>Creation date:</b> 
 * 15-05-2014 </p>
 *
 * <p><b>Changelog:</b></p>
 * <ul>
 * <li> 1 , 15-05-2014 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class SimpleDeployerForContainerWithServiceRegistry extends Deployer 
{
    
    private BSDLRegistry bsdlRegistry;
    
    private IServiceRegistry bsdmServiceRegistry;
    

    public SimpleDeployerForContainerWithServiceRegistry(IUnpacker unpacker, IServicesDB servicesBD, ISemanticLocator deploymentDirLocator) throws ModelException
    {
        super(unpacker, servicesBD, deploymentDirLocator);
        initialize(new BSDMServiceRegistry(
                ServiceGroundingFactory.getSingleton(),
                FunctionalityGroundingFactory.getSingleton(),
                new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton())));
    }
    
    public SimpleDeployerForContainerWithServiceRegistry(ISemanticLocator deploymentDirLocator) throws ModelException
    {
        super(deploymentDirLocator);
        initialize(new BSDMServiceRegistry(
                ServiceGroundingFactory.getSingleton(),
                FunctionalityGroundingFactory.getSingleton(),
                new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton())));
    }

    
    public BSDLRegistry getBsdlRegistry()
    {
        return bsdlRegistry;
    }
    
    public SimpleDeployerForContainerWithServiceRegistry(IUnpacker unpacker, 
            IServicesDB servicesBD, 
            ISemanticLocator deploymentDirLocator,
            IServiceRegistry bsdmServiceRegistry) throws ModelException
    {
        super(unpacker, servicesBD, deploymentDirLocator);
        initialize(bsdmServiceRegistry);
    }
    
    public SimpleDeployerForContainerWithServiceRegistry(IUnpacker unpacker, 
            IServicesDB servicesBD, 
            ISemanticLocator deploymentDirLocator,
            IServiceRegistry bsdmServiceRegistry,
            BSDLRegistry bsdlRegistry) throws ModelException
    {
        super(unpacker, servicesBD, deploymentDirLocator);        
        initialize(bsdmServiceRegistry, bsdlRegistry);
    }

    
    public IServiceRegistry getBsdmServiceRegistry()
    {
        return bsdmServiceRegistry;
    }
    
    private void initialize(IServiceRegistry bsdmServiceRegistry, BSDLRegistry bsdlRegistry) throws ModelException
    {
        this.bsdlRegistry = bsdlRegistry;
        this.bsdmServiceRegistry = bsdmServiceRegistry;
        this.bsdmServiceRegistry.setMatchmaker(new SimpleStructuralMatchmaker(bsdlRegistry));
    }
    
    private void initialize(IServiceRegistry bsdmServiceRegistry) throws ModelException
    {
        this.initialize(bsdmServiceRegistry, new BSDLRegistry(new BSDLBSDMLoader()));
    }
    
    
    public Collection<ISemanticIdentifier> deployService(ISemanticLocator serviceLocator) throws ModelException
    {     
        return deployService(serviceLocator, bsdlRegistry, bsdmServiceRegistry);
    }
    
    
    public void undeployService(ISemanticIdentifier serviceIdentifier) throws ModelException
    {
        undeployService(serviceIdentifier, bsdlRegistry, bsdmServiceRegistry);
    }
    
            
    @Override
    public IServicesDB getServicesDB()
    {
        return super.getServicesDB();
    }

}
