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

import com.hi3project.vineyard.comm.FunctionalitySearchResult;
import com.hi3project.broccoli.bsdf.exceptions.ServiceDiscoveryException;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdl.impl.exceptions.SemanticModelException;
import com.hi3project.broccoli.bsdl.api.ISemanticLocator;
import com.hi3project.broccoli.bsdl.api.meta.IOntologyLanguage;
import com.hi3project.broccoli.bsdm.api.IServiceDescription;
import com.hi3project.broccoli.bsdf.api.discovery.IExternalServiceRegistryMatchmaker;
import com.hi3project.broccoli.bsdf.api.discovery.IFunctionalitySearchResult;
import com.hi3project.broccoli.bsdf.api.discovery.IMatchmaker;
import com.hi3project.broccoli.bsdf.api.discovery.IServiceRegistry;
import com.hi3project.broccoli.bsdm.api.profile.functionality.IServiceFunctionality;
import com.hi3project.broccoli.bsdm.api.profile.functionality.IParameter;
import com.hi3project.broccoli.bsdm.api.profile.functionality.IRequestedFunctionality;
import com.hi3project.broccoli.bsdm.api.profile.nonFunctionalProperties.INonFunctionalProperty;
import com.hi3project.broccoli.io.BSDFLogger;
import com.hi3project.broccoli.bsdf.exceptions.MatchingException;
import com.hi3project.broccoli.bsdm.api.grounding.IFunctionalityGroundingFactory;
import com.hi3project.broccoli.bsdm.api.grounding.IServiceGroundingFactory;
import com.hi3project.broccoli.bsdm.api.implementation.IFunctionalityImplementation;
import com.hi3project.broccoli.bsdm.api.implementation.IServiceImplementation;
import com.hi3project.broccoli.bsdm.api.profile.functionality.IInput;
import com.hi3project.broccoli.bsdm.api.profile.functionality.IOutput;
import com.hi3project.broccoli.bsdm.api.serializing.IMessageSerializer;
import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceExecutionException;
import com.hi3project.vineyard.comm.BasicRanker;
import java.util.ArrayList;
import java.util.Collection;

/**
 * First working implementation of IServiceRegistry
 *
 * 
 */
public class BSDMServiceRegistry extends SimpleBSDMServiceLoader implements IServiceRegistry
{

    private final Collection<IExternalServiceRegistryMatchmaker> externalRegistries = new ArrayList<IExternalServiceRegistryMatchmaker>();

    private IMatchmaker matchmaker = null;

    public BSDMServiceRegistry(
            IServiceGroundingFactory serviceGroundingFactory,
            IFunctionalityGroundingFactory functionalityGroundingFactory,
            IMessageSerializer messageSerializer)
    {
        super(serviceGroundingFactory, functionalityGroundingFactory, messageSerializer);
    }

    public Collection<IServiceFunctionality> getAdvertisedFunctionalities()
    {
        Collection<IServiceFunctionality> advertisedFunctionalities = new ArrayList<IServiceFunctionality>();
        for (IServiceDescription service : getServices())
        {
            advertisedFunctionalities.addAll(service.getProfile().advertisedFunctionalities());
        }
        return advertisedFunctionalities;
    }

    @Override
    public IMatchmaker getMatchmaker()
    {
        return matchmaker;
    }

    @Override
    public void setMatchmaker(IMatchmaker matchmaker)
    {
        this.matchmaker = matchmaker;
    }

    @Override
    public Collection<IFunctionalitySearchResult> searchFor(IRequestedFunctionality requestedFunctionality) throws ModelException
    {
        BSDFLogger.getLogger().info("Searches for a requested functionality: " + requestedFunctionality);
        if (null == getMatchmaker())
        {
            throw new ServiceDiscoveryException("A matchmaker is needed in order to search for functionalities", null);
        }

        Collection<IFunctionalitySearchResult> results = new ArrayList<IFunctionalitySearchResult>();
        for (IServiceFunctionality advertisedFunctionality : getAdvertisedFunctionalities())
        {

            try
            {
                if (parametersRelated(requestedFunctionality.inputs(), advertisedFunctionality.inputs())
                        && parametersRelated(requestedFunctionality.outputs(), advertisedFunctionality.outputs())
                        && nonFPContained(requestedFunctionality.nonFunctionalProperties(), advertisedFunctionality.nonFunctionalProperties()))
                {
                    BasicRanker ranker = new BasicRanker(requestedFunctionality);

                    // ranker: required IOPEs and nFPs...
                    for (IInput iinput : requestedFunctionality.inputs())
                    {
                        ranker.inputRequiredIOPE(this.parameterRelated(iinput, advertisedFunctionality.inputs()));
                    }
                    for (IOutput ioutput : requestedFunctionality.outputs())
                    {
                        ranker.inputRequiredIOPE(this.parameterRelated(ioutput, advertisedFunctionality.outputs()));
                    }
                    for (INonFunctionalProperty npf : requestedFunctionality.nonFunctionalProperties())
                    {
                        ranker.inputRequiredNFP(this.nonFPRelated(npf, advertisedFunctionality.nonFunctionalProperties()));
                    }

                    // ranker: optional IOPEs and nFPs...
                    if (null != requestedFunctionality.getOptionalProperties())
                    {
                        for (IInput iinput : requestedFunctionality.getOptionalProperties().inputs())
                        {
                            ranker.inputOptionalIOPE(this.parameterRelated(iinput, advertisedFunctionality.inputs()));
                        }
                        for (IOutput ioutput : requestedFunctionality.getOptionalProperties().outputs())
                        {
                            ranker.inputOptionalIOPE(this.parameterRelated(ioutput, advertisedFunctionality.outputs()));
                        }
                        for (INonFunctionalProperty npf : requestedFunctionality.getOptionalProperties().nonFunctionalProperties())
                        {
                            ranker.inputOptionalNFP(this.nonFPRelated(npf, advertisedFunctionality.nonFunctionalProperties()));
                        }
                    }

                    if (null != requestedFunctionality.getPreferredProperties())
                    {
                        // ranker: preferred IOPEs and nFPs...
                        for (IInput iinput : requestedFunctionality.getPreferredProperties().inputs())
                        {
                            ranker.inputPreferredIOPE(this.parameterRelated(iinput, advertisedFunctionality.inputs()));
                        }
                        for (IOutput ioutput : requestedFunctionality.getPreferredProperties().outputs())
                        {
                            ranker.inputPreferredIOPE(this.parameterRelated(ioutput, advertisedFunctionality.outputs()));
                        }
                        for (INonFunctionalProperty npf : requestedFunctionality.getPreferredProperties().nonFunctionalProperties())
                        {
                            ranker.inputPreferredNFP(this.nonFPRelated(npf, advertisedFunctionality.nonFunctionalProperties()));
                        }
                    }

                    results.add(
                            new FunctionalitySearchResult(advertisedFunctionality.getServiceDescription(), advertisedFunctionality.name(),
                                    ranker.getComputedEvaluation()));
                }
            } catch (ModelException ex)
            {
                throw new MatchingException("Problem matching: " + requestedFunctionality.toString(), ex);
            }
        }
        return results;
    }

    private boolean nonFPContained(Collection<INonFunctionalProperty> properties1, Collection<INonFunctionalProperty> properties2) throws SemanticModelException
    {
        if (properties1.size() <= 0)
        {
            return true;
        }
        for (INonFunctionalProperty property : properties1)
        {
            if (-1 == nonFPRelated(property, properties2))
            {
                return false;
            }
        }
        return true;
    }

    private int nonFPRelated(INonFunctionalProperty property, Collection<INonFunctionalProperty> properties2) throws SemanticModelException
    {
        for (INonFunctionalProperty property0 : properties2)
        {
            if (getMatchmaker().same(
                    property.instance().getConcept().semanticAxiom().getSemanticIdentifier(),
                    property0.instance().getConcept().semanticAxiom().getSemanticIdentifier()))
            {
                return 0;
            }
            int subsumes = getMatchmaker().subsumes(
                    property.instance().getConcept().semanticAxiom().getSemanticIdentifier(),
                    property0.instance().getConcept().semanticAxiom().getSemanticIdentifier());
            if (-1 < subsumes)
            {
                return subsumes;
            }
        }
        return -1;
    }

    private boolean parametersRelated(Collection parameters1, Collection parameters2) throws SemanticModelException
    {
        if (parameters1.size() <= 0)
        {
            return true;
        }
        if (parameters1.size() != parameters2.size())
        {
            return false;
        }
        for (Object parameter : parameters1)
        {
            if (parameter instanceof IParameter)
            {
                if (-1 == parameterRelated((IParameter) parameter, parameters2))
                {
                    return false;
                }
            } else
            {
                return false;
            }
        }
        return true;
    }

    private int parameterRelated(IParameter parameter, Collection parameters) throws SemanticModelException
    {
        for (Object parameterO : parameters)
        {
            if (getMatchmaker().same(parameter.getSemanticAnnotation(), ((IParameter) parameterO).getSemanticAnnotation()))
            {
                return 0;
            }
            int subsumes = getMatchmaker().subsumes(parameter.getSemanticAnnotation(), ((IParameter) parameterO).getSemanticAnnotation());
            if (-1 < subsumes)
            {
                return subsumes;
            }

        }
        return -1;
    }

    @Override
    public void addExternalRegistry(IExternalServiceRegistryMatchmaker externalRegistry)
    {
        synchronized (this.externalRegistries)
        {
            this.externalRegistries.add(externalRegistry);
        }
    }

    @Override
    public Collection<IFunctionalitySearchResult> searchFor(IOntologyLanguage ontologyLanguage, ISemanticLocator requestedFunctionality) throws ModelException
    {
        BSDFLogger.getLogger().info("Searches for a requested functionality: " + requestedFunctionality
                + " expressed in language: " + ontologyLanguage.toString());
        Collection<IFunctionalitySearchResult> searchResults = new ArrayList<IFunctionalitySearchResult>();
        synchronized (this.externalRegistries)
        {
            for (IExternalServiceRegistryMatchmaker eRegistry : this.externalRegistries)
            {
                if (eRegistry.understands(ontologyLanguage))
                {
                    Collection<IFunctionalitySearchResult> searchSubResults = eRegistry.searchFor(requestedFunctionality);
                    searchResults.addAll(searchSubResults);
                }
            }
        }
        return searchResults;
    }

    @Override
    public void initServiceImplementations() throws ServiceExecutionException
    {
        for (IServiceDescription serviceDescription : 
                this.getServices())
        {
            for (IServiceImplementation serviceImplementation : 
                    serviceDescription.getImplementations())
            {
                for (IFunctionalityImplementation functionalityImplementation :
                    serviceImplementation.getFunctionalityImplementations())
                {
                    functionalityImplementation.loadFunctionalityImplementationObject();
                }
            }
        }
    }

}
