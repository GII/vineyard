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

package com.hi3project.vineyard.registry.server.owls;

import com.hi3project.broccoli.bsdl.api.ISemanticLocator;
import com.hi3project.broccoli.bsdl.api.registry.IBSDLRegistry;
import com.hi3project.broccoli.bsdm.api.IComponent;
import com.hi3project.broccoli.bsdm.api.IServiceDescription;
import com.hi3project.broccoli.bsdf.api.discovery.IServiceRegistry;
import com.hi3project.broccoli.bsdm.api.parsing.IParameterConverter;
import com.hi3project.broccoli.bsdm.api.parsing.IServiceDescriptionLoader;
import com.hi3project.broccoli.bsdm.api.profile.functionality.IRequestedFunctionality;
import com.hi3project.broccoli.bsdl.impl.SemanticIdentifier;
import com.hi3project.broccoli.bsdl.impl.registry.BSDLRegistry;
import com.hi3project.broccoli.owls.grounding.OWLSFunctionalityGroundingJenaGrounding;
import com.hi3project.broccoli.bsdf.impl.parsing.JenaBeanConverter;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdl.impl.exceptions.SemanticModelException;
import com.hi3project.broccoli.io.BSDFLogger;
import com.hi3project.broccoli.bsdf.impl.owls.profile.functionality.OWLSAtomicService;
import com.hi3project.broccoli.bsdf.impl.owls.serviceBuilder.OWLSServiceBuilder;
import com.hi3project.broccoli.owls.OWLSServiceDescription;
import com.hi3project.vineyard.registry.server.SimpleStructuralMatchmaker;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * 
 */
public class OWLSServiceLoader implements IServiceDescriptionLoader
{

    protected IServiceRegistry serviceRegistry;
    private OWLSServiceBuilder owlsServiceBuilder;
    BSDLRegistry bsdlRegistry;
    OWLSRegistryMatchmaker owlsmxRegistryMatchmaker;
    //private List<URI> uriListForModel;

    public OWLSServiceLoader(IServiceRegistry serviceRegistry, BSDLRegistry bsdlRegistry) throws SemanticModelException
    {
        this.serviceRegistry = serviceRegistry;
        this.owlsmxRegistryMatchmaker = new OWLSRegistryMatchmaker();
        this.serviceRegistry.addExternalRegistry(owlsmxRegistryMatchmaker);
        this.bsdlRegistry = bsdlRegistry;
        this.owlsServiceBuilder = new OWLSServiceBuilder(null);
        BSDFLogger.getLogger().info("Instances an OWLSServiceLoader");
    }

    public void setServiceRegistry(IServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public Collection<IServiceDescription> readServicesFrom(ISemanticLocator locator) throws ModelException
    {
        Collection<ISemanticLocator> locators = new ArrayList<ISemanticLocator>();
        locators.add(locator);
        return readServicesFrom(locators);
    }

    @Override
    public Collection<IServiceDescription> readServicesFrom(Collection<ISemanticLocator> locators) throws ModelException
    {
        BSDFLogger.getLogger().info("Reads services");
        Collection<IServiceDescription> services = new ArrayList<IServiceDescription>();
        for (ISemanticLocator locator : locators)
        {
            OWLSAtomicService functionality = owlsServiceBuilder.buildOWLSServiceFrom(locator.getURI());

            IComponent hasService = serviceRegistry.hasComponent(new SemanticIdentifier(functionality.getService().getURI()));
            if (null != hasService)
            {
                serviceRegistry.unregisterComponent(hasService);
            }
            functionality.setMatchmaker(new SimpleStructuralMatchmaker(bsdlRegistry));
            OWLSFunctionalityGroundingJenaGrounding functionalityGrounding = new OWLSFunctionalityGroundingJenaGrounding(new JenaBeanConverter(bsdlRegistry), functionality);
            OWLSServiceDescription service = new OWLSServiceDescription(functionality, functionalityGrounding, bsdlRegistry);
            services.add(service);
            serviceRegistry.registerComponent(service);
            this.owlsmxRegistryMatchmaker.addService(locator, service);
        }
        return services;
    }

    @Override
    public Collection<IRequestedFunctionality> readFunctionalitiesFrom(ISemanticLocator locator) throws ModelException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<IRequestedFunctionality> readFunctionalitiesFrom(Collection<ISemanticLocator> locators) throws ModelException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IParameterConverter getParameterConverter()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IServiceDescriptionLoader addOntology(ISemanticLocator locator) throws ModelException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IServiceDescriptionLoader addOntologyReferences(ISemanticLocator locator) throws SemanticModelException, ModelException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setBSDLRegistry(IBSDLRegistry bsdlRegistry)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IBSDLRegistry getBSDLRegistry()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<IRequestedFunctionality> readFunctionalities(Collection<String> contents) throws ModelException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<IServiceDescription> readServices(Collection<String> serviceDescriptors) throws ModelException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
