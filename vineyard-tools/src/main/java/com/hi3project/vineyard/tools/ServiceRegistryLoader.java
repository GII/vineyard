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

package com.hi3project.vineyard.tools;


import com.hi3project.broccoli.bsdf.impl.parsing.ServiceDescriptionLoader;
import com.hi3project.broccoli.bsdm.impl.parsing.BSDLBSDMLoader;
import com.hi3project.broccoli.bsdl.api.ISemanticLocator;
import com.hi3project.broccoli.bsdl.api.registry.IServiceRegistryLoader;
import com.hi3project.broccoli.bsdm.api.IComponent;
import com.hi3project.broccoli.bsdm.api.IServiceDescription;
import com.hi3project.broccoli.bsdf.api.discovery.IFunctionalitySearchResult;
import com.hi3project.broccoli.bsdf.api.discovery.IServiceRegistry;
import com.hi3project.broccoli.bsdf.impl.serializing.JSONMessageSerializer;
import com.hi3project.broccoli.bsdl.api.registry.IBSDLRegistry;
import com.hi3project.broccoli.bsdm.api.profile.functionality.IRequestedFunctionality;
import com.hi3project.broccoli.bsdl.impl.registry.BSDLRegistry;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.vineyard.comm.FunctionalityGroundingFactory;
import com.hi3project.vineyard.comm.ServiceGroundingFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;


/**
 *
 * 
 */
public class ServiceRegistryLoader extends ServiceDescriptionLoader implements IServiceRegistryLoader
{

    private IServiceRegistry serviceRegistry;

    public ServiceRegistryLoader(
            IServiceRegistry serviceRegistry) throws ModelException
    {
        this(new BSDLRegistry(new BSDLBSDMLoader()), serviceRegistry);
    }
    
    public ServiceRegistryLoader(
            IBSDLRegistry bsdlRegistry,
            IServiceRegistry serviceRegistry)
    {
        super(
                new ServiceGroundingFactory(),
                new FunctionalityGroundingFactory(),
                bsdlRegistry,
                new JSONMessageSerializer(new ServiceGroundingFactory(), new FunctionalityGroundingFactory()));
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public IServiceRegistry getServiceRegistry()
    {
        return this.serviceRegistry;
    }

    @Override
    public Collection<IServiceDescription> readServicesFrom(Collection<ISemanticLocator> locators) throws ModelException
    {
        Collection<IServiceDescription> readServices = super.readServicesFrom(locators);
        
        for (IServiceDescription service : readServices)
        {

            IComponent hasService = serviceRegistry.hasComponent(service.getIdentifier());
            if (null != hasService)
            {
                serviceRegistry.unregisterComponent(hasService);
            }
            serviceRegistry.registerComponent(service);

        }
        return readServices;
    }
    
    @Override
    public Collection<IFunctionalitySearchResult> searchForFunctionalitiesFrom(ISemanticLocator locator) throws ModelException
    {
        Collection<ISemanticLocator> locators = new ArrayList<ISemanticLocator>();
        locators.add(locator);
        return searchForFunctionalitiesFrom(locators);
    }
    
    @Override
    public Collection<IFunctionalitySearchResult> searchForFunctionalitiesFrom(Collection<ISemanticLocator> locators) throws ModelException
    {
        Collection<IRequestedFunctionality> readFunctionalitiesFrom = this.readFunctionalitiesFrom(locators);
        Collection<IFunctionalitySearchResult> discoveredServices = new HashSet<IFunctionalitySearchResult>();
        for (IRequestedFunctionality requestedFunctionality : readFunctionalitiesFrom)
        {
            Collection<IFunctionalitySearchResult> results = this.search(requestedFunctionality);
            if (null != results) discoveredServices.addAll(results);
        }
        return discoveredServices;
    }
    
    public Collection<IFunctionalitySearchResult> search(IRequestedFunctionality requestedFunctionality) throws ModelException            
    {
        return this.getServiceRegistry().searchFor(requestedFunctionality);
    }
    
    
}
