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

package com.hi3project.vineyard.registry.server;

import com.hi3project.broccoli.bsdl.api.IAxiom;
import com.hi3project.broccoli.bsdl.api.ISemanticIdentifier;
import com.hi3project.broccoli.bsdl.api.ISemanticLocator;
import com.hi3project.broccoli.bsdl.api.registry.IBSDLRegistry;
import com.hi3project.broccoli.bsdm.api.IComponent;
import com.hi3project.broccoli.bsdm.api.IServiceDescription;
import com.hi3project.broccoli.bsdf.api.discovery.IRegistryLoader;
import com.hi3project.broccoli.bsdl.api.parsing.IDocumentLoader;
import com.hi3project.broccoli.bsdl.impl.Instance;
import com.hi3project.broccoli.bsdm.impl.ServiceDescription;
import com.hi3project.broccoli.bsdm.impl.exceptions.BSDMComponentException;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdm.api.grounding.IFunctionalityGroundingFactory;
import com.hi3project.broccoli.bsdm.api.grounding.IServiceGroundingFactory;
import com.hi3project.broccoli.bsdm.api.serializing.IMessageSerializer;
import com.hi3project.broccoli.io.BSDFLogger;
import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>
 *  <b>Description:</b></p>
 *  Implementation of IRegistryLoader. An entity that knows how to load BSDM services
 * and handle them in memory.
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
public class SimpleBSDMServiceLoader implements IRegistryLoader
{

    protected final Collection<IServiceDescription> services = new ArrayList<IServiceDescription>();
    
    private IServiceGroundingFactory serviceGroundingFactory;
    
    private IFunctionalityGroundingFactory functionalityGroundingFactory;
    
    private IMessageSerializer messageSerializer;
    
    
    public SimpleBSDMServiceLoader(
            IServiceGroundingFactory serviceGroundingFactory,
            IFunctionalityGroundingFactory functionalityGroundingFactory,
            IMessageSerializer messageSerializer)
    {
        this.serviceGroundingFactory = serviceGroundingFactory;
        this.functionalityGroundingFactory = functionalityGroundingFactory;
        this.messageSerializer = messageSerializer;
    }
    
    
    @Override
    public synchronized void clean()
    {
        this.services.clear();
    }
    
    @Override
    public IComponent hasComponent(ISemanticIdentifier semanticIdentifier) throws ModelException
    {
        if (null == semanticIdentifier)
        {
            return null;
        }
        for (IServiceDescription service : getServices())
        {
            if (service.getIdentifier().equals(semanticIdentifier))
            {
                return service;
            }
        }
        return null;
    }

    @Override
    public synchronized boolean registerComponent(IComponent component) throws BSDMComponentException
    {
        if (component instanceof IServiceDescription)
        {
            BSDFLogger.getLogger().info("Adds component: " + component.name());
            if (!services.contains((IServiceDescription) component))
            {
                services.add((IServiceDescription) component);
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean unregisterComponent(IComponent component) throws BSDMComponentException
    {
        if (component instanceof IServiceDescription)
        {
            BSDFLogger.getLogger().info("Removes component: " + component.name());
            if (services.contains((IServiceDescription) component))
            {
                return services.remove((IServiceDescription) component);
            }
        }
        return false;
    }

    @Override
    public Collection<IServiceDescription> readServicesFrom(Collection<ISemanticLocator> locatorCollection,
            IDocumentLoader bsdlDocumentLoader) throws ModelException
    {
        Collection<IAxiom> readAxioms = bsdlDocumentLoader.readFrom(locatorCollection);
        return readAndLoadServicesFrom(readAxioms, bsdlDocumentLoader.getRegistry());
    }

    @Override
    public Collection<IServiceDescription> readServicesFrom(ISemanticLocator locator,
            IDocumentLoader bsdlDocumentLoader) throws ModelException
    {
        Collection<IAxiom> readAxioms = bsdlDocumentLoader.readFrom(locator);
        return readAndLoadServicesFrom(readAxioms, bsdlDocumentLoader.getRegistry());
    }

    private Collection<IServiceDescription> readAndLoadServicesFrom(Collection<IAxiom> readAxioms,
            IBSDLRegistry bsdlRegistry) throws ModelException
    {
        Collection<IServiceDescription> readServices = new ArrayList<IServiceDescription>();
        for (IAxiom axiom : readAxioms)
        {
            if (axiom instanceof Instance)
            {
                if (((Instance) axiom).getConcept().semanticAxiom().getSemanticIdentifier().equals(ServiceDescription.sconceptIdentifier()))
                {
                    ServiceDescription readService = 
                        new ServiceDescription(
                                this.messageSerializer,
                                this.serviceGroundingFactory,
                                this.functionalityGroundingFactory,
                                bsdlRegistry,
                                (Instance) axiom);
                    readServices.add(readService);
                    this.registerComponent(readService);
                }
            }
        }
        return readServices;
    }
    
    public IServiceDescription getService(ISemanticIdentifier serviceIdentifier)
    {
        for (IServiceDescription serviceDescription : this.getServices())
        {
            if (serviceDescription.getIdentifier().equals(serviceIdentifier))
            {
                return serviceDescription;
            }
        }
        return null;
    }
    
    public synchronized Collection<IServiceDescription> getServices()
    {
        return services;
    }

}
