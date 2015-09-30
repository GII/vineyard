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

package com.hi3project.vineyard.container.pico;

import com.hi3project.broccoli.bsdf.api.deployment.container.IBSDMServicePicoContainer;
import com.hi3project.broccoli.bsdf.impl.serializing.JSONMessageSerializer;
import com.hi3project.broccoli.bsdl.api.ISemanticIdentifier;
import com.hi3project.broccoli.bsdl.api.ISemanticLocator;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceExecutionException;
import com.hi3project.broccoli.io.BSDFLogger;
import com.hi3project.vineyard.comm.FunctionalityGroundingFactory;
import com.hi3project.vineyard.comm.ServiceGroundingFactory;
import com.hi3project.vineyard.container.AbstractBSDMServiceContainer;
import com.hi3project.vineyard.container.micro.SimpleDeployerForMicroContainer;
import com.hi3project.vineyard.registry.server.SimpleBSDMServiceLoader;

/**
 * <p>
 *  <b>Description:</b></p>
 *  Implementation of IBSDMServicePicoContainer
 *
 *
 * <p><b>Creation date:</b> 
 * 06-03-2015 </p>
 *
 * <p><b>Changelog:</b></p>
 * <ul>
 * <li> 1 , 06-03-2015 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class BSDMServicePicoContainer extends AbstractBSDMServiceContainer implements IBSDMServicePicoContainer
{

    private SimpleBSDMServiceLoader serviceLoader = null;

    private boolean started = false;
    
    
    public BSDMServicePicoContainer(
            ISemanticLocator workingLocation,
            ISemanticIdentifier containerName) throws ModelException
    {
        super(workingLocation, containerName);
        this.serviceLoader = new SimpleBSDMServiceLoader(
                ServiceGroundingFactory.getSingleton(),
                FunctionalityGroundingFactory.getSingleton(),
                new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()));
        this.started = false;
    }
    
    
    @Override
    public synchronized void registerService(ISemanticLocator packedServiceDescriptor) throws ServiceExecutionException
    {
        if (!this.hasStarted())
        {
            throw new ServiceExecutionException("picoContainer must be connected to register a service...");
        }
        try
        {

            BSDFLogger.getLogger().info("Registers service: " + packedServiceDescriptor.toString());
            
            SimpleDeployerForMicroContainer deployer = new SimpleDeployerForMicroContainer(workingLocation, this.serviceLoader, this.servicesDB);
            deployer.deployService(packedServiceDescriptor);           

        } catch (ModelException ex)
        {
            throw new ServiceExecutionException("Error deploying service in micro container: " + packedServiceDescriptor.toString(), ex);
        }

    }
    
    
    @Override
    public String toString()
    {
        return "BSDMServicePicoContainer{" + "workingLocation=" + workingLocation + '}';
    }
    
    @Override
    public void start() throws ServiceExecutionException
    {
        this.started = true;
    }

    @Override
    public void stop() throws ServiceExecutionException
    {
        this.started = false;
    }

    @Override
    public void restart() throws ServiceExecutionException
    {
        this.stop();
        this.start();
    }

    @Override
    public boolean hasStarted()
    {
        return this.started;
    }
}
