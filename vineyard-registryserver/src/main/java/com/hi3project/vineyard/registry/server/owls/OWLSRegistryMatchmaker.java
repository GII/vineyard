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


import com.hi3project.broccoli.conf.ProjProperties;
import com.hi3project.broccoli.bsdl.api.ISemanticIdentifier;
import com.hi3project.broccoli.bsdl.api.ISemanticLocator;
import com.hi3project.broccoli.bsdl.api.meta.IOntologyLanguage;
import com.hi3project.broccoli.bsdm.api.IServiceDescription;
import com.hi3project.broccoli.bsdf.api.discovery.IExternalServiceRegistryMatchmaker;
import com.hi3project.broccoli.bsdf.api.discovery.IFunctionalitySearchEvaluation;
import com.hi3project.broccoli.bsdf.api.discovery.IFunctionalitySearchResult;
import com.hi3project.broccoli.bsdl.impl.meta.MetaPropertyOntologyLanguage;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdl.impl.exceptions.SemanticModelException;
import com.hi3project.broccoli.io.BSDFLogger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.mindswap.owl.OWLOntology;

/**
 *
 * 
 */
public class OWLSRegistryMatchmaker implements IExternalServiceRegistryMatchmaker
{

    IOntologyLanguage ownOntologyLanguage;
//    MatchMaker matchMaker;

    public OWLSRegistryMatchmaker() throws SemanticModelException
    {
//        this.matchMaker = new MatchMaker();
        this.ownOntologyLanguage = new MetaPropertyOntologyLanguage(ProjProperties.OWL);
        BSDFLogger.getLogger().info("Instances an OWLSRegistryMatchmaker");
    }

    @Override
    public boolean understands(IOntologyLanguage ontologyLanguage)
    {
        return this.ownOntologyLanguage.compatibleWith(ontologyLanguage);
    }

    @Override
    public void addService(ISemanticLocator locator, IServiceDescription service) throws ModelException
    {
////        try
////        {
////            // load owls to OWLSMX
////            OWLOntology parsedOntology = OwlsUtils.parseURI(locator.getURI());
////            matchMaker.registerService(parsedOntology);
////
////            // add ServiceDescription
////            services.put(parsedOntology.getURI(), service);
////        } catch (IOException ex)
////        {
////            throw new OWLIOException("Cannot parse service: " + locator.getURI().toString(), ex);
////        }
    }

    @Override
    public Collection<IFunctionalitySearchResult> searchFor(ISemanticLocator locator) throws SemanticModelException
    {
        Collection<IFunctionalitySearchResult> results = new ArrayList<IFunctionalitySearchResult>();
//        try
//        {
//            OWLOntology parsedRequest = OwlsUtils.parseURI(locator.getURI());
//
//            Collection<Match> matches = matchMaker.match(
//                    parsedRequest, InputMatchers.SUPERCLASSES, OutputMatchers.SUBCLASSES);
//
//
//            Iterator<Match> iterator = matches.iterator();
//            while (iterator.hasNext())
//            {
//                final Match match = iterator.next();
//
//                IFunctionalitySearchResult result = new IFunctionalitySearchResult()
//                {
//                    @Override
//                    public IServiceDescription getServiceDescription()
//                    {
//                        return services.get(match.getService().getURI());
//                    }
//
//                    @Override
//                    public String getAdvertisedFunctionalityName()
//                    {
//                        return match.getService().getURI().getFragment();                        
//                    }
//
//                    @Override
//                    public IFunctionalitySearchEvaluation getEvaluation()
//                    {
//                        return null;
//                    }
//
//                    @Override
//                    public void setServiceDescription(IServiceDescription serviceDescription)
//                    {
//                        throw new UnsupportedOperationException("Not supported here.");
//                    }
//
//                    @Override
//                    public ISemanticIdentifier getServiceIdentifier() throws SemanticModelException
//                    {
//                        return null!=getServiceDescription()?getServiceDescription().getIdentifier():null;
//                    }
//                };
//                results.add(result);
//            }
//        } catch (Exception ex)
//        {
//            throw new SemanticModelException("Cannot read: " + locator.toString(), ex);
//        }
        return results;
    }
}
