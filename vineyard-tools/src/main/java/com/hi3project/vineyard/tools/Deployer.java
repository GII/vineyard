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

import com.hi3project.broccoli.bsdf.impl.deployment.ServiceLocatorsVO;
import com.mytechia.commons.util.classloading.ClassLoaders;
import com.hi3project.broccoli.bsdl.api.IAxiom;
import com.hi3project.broccoli.bsdl.api.IOntology;
import com.hi3project.broccoli.bsdl.api.ISemanticIdentifier;
import com.hi3project.broccoli.bsdl.api.ISemanticLocator;
import com.hi3project.broccoli.bsdl.api.registry.IBSDLRegistry;
import com.hi3project.broccoli.bsdm.api.IComponent;
import com.hi3project.broccoli.bsdm.api.IServiceDescription;
import com.hi3project.broccoli.bsdf.api.deployment.IDeployer;
import com.hi3project.broccoli.bsdf.api.deployment.IServiceDescriptors;
import com.hi3project.broccoli.bsdf.api.deployment.IUnpacker;
import com.hi3project.broccoli.bsdl.impl.SemanticAxiom;
import com.hi3project.broccoli.bsdl.impl.SemanticLocator;
import com.hi3project.broccoli.bsdl.impl.parsing.BSDLDocumentLoader;
import com.hi3project.broccoli.bsdl.impl.registry.BSDLRegistry;
import com.hi3project.broccoli.bsdm.impl.parsing.BSDLBSDMLoader;
import com.hi3project.broccoli.bsdf.impl.owls.parsing.OWLConverter;
import com.hi3project.broccoli.bsdf.api.deployment.bd.IServicesDB;
import com.hi3project.broccoli.bsdf.api.discovery.IRegistryLoader;
import com.hi3project.broccoli.bsdm.impl.asyncronous.DescriptorData;
import com.hi3project.broccoli.bsdf.impl.deployment.bd.LoadedServiceVO;
import com.hi3project.vineyard.container.bd.ServicesSimpleDB;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdl.impl.exceptions.ParsingException;
import com.hi3project.broccoli.bsdf.exceptions.ServiceDeployException;
import com.hi3project.broccoli.io.BSDFLogger;
import com.hi3project.broccoli.io.DescriptorWriter;
import com.hi3project.broccoli.bsdf.impl.owl.ObjectOWLSTranslator;
import com.hi3project.broccoli.bsdl.api.parsing.IDocumentParser;
import com.hi3project.broccoli.bsdl.api.parsing.PreParsingRule;
import com.hi3project.broccoli.bsdl.impl.parsing.xml.dom.DocumentPreParser;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * 
 */
public class Deployer implements IDeployer
{

    private IUnpacker unpacker = null;
    private ISemanticLocator deploymentDirLocator = null;
    private IServicesDB servicesDB = null;
    private IDynamicJarLoader dynamicJarLoader = null;

    private Collection<PreParsingRule> preParsingRules = new ArrayList<PreParsingRule>();
    

    public Deployer(IUnpacker unpacker, IServicesDB servicesBD, ISemanticLocator deploymentDirLocator)
    {
        this.unpacker = unpacker;
        this.deploymentDirLocator = deploymentDirLocator;
        this.servicesDB = servicesBD;
    }

    public Deployer(ISemanticLocator deploymentDirLocator)
    {
        this(new Unpacker(), new ServicesSimpleDB(), deploymentDirLocator);
    }

    public Deployer(IServicesDB servicesBD, ISemanticLocator deploymentDirLocator)
    {
        this(new Unpacker(), servicesBD, deploymentDirLocator);
    }

    public void addPreParsingRule(PreParsingRule rule)
    {
        this.preParsingRules.add(rule);
    }

    @Override
    public Collection<ISemanticIdentifier> deployService(
            ISemanticLocator serviceLocator,
            IBSDLRegistry bsdlRegistry,
            IRegistryLoader serviceRegistry) throws ModelException
    {
        BSDFLogger.getLogger().info("Deploys service from: " + serviceLocator.toString());

        // request a new ServiceVO object to the database
        LoadedServiceVO newServiceVO = this.servicesDB.getNewServiceVO();

        // unpack the service in the specified deployment location
        ServiceLocatorsVO unpackedServiceLocators
                = this.unpacker.unpackService(
                        serviceLocator,
                        new SemanticLocator(composeDeploymentLocatorWithSufix(newServiceVO.getIdSt()))
                );
        newServiceVO.setServiceLocators(unpackedServiceLocators);

        // create a documentLoader over the given bsdlRegistry
        BSDLDocumentLoader documentLoader = new BSDLDocumentLoader(bsdlRegistry);

        // add to bsdlRegistry a bsdlConverter for OWL
        bsdlRegistry.registerConverter(new OWLConverter((BSDLRegistry) bsdlRegistry));

        // create and add a BSDL documentParser do the documentLoader
        IDocumentParser documentParser = new BSDLBSDMLoader((BSDLRegistry) bsdlRegistry).createBSDLDocumentParser();
        documentLoader.registerDocumentParser(documentParser);        

        // load the BSDM descriptors
        documentLoader.readFrom(BSDLBSDMLoader.descriptorLocationsForBSDMCore());

        BSDFLogger.getLogger().info("Loading and adding " + unpackedServiceLocators.getOntologiesDescriptorLocators().size() + " ontology descriptors");

        // read and load the ontologies
        for (ISemanticLocator ontologyLocator : unpackedServiceLocators.getOntologiesDescriptorLocators())
        {
            newServiceVO.getOntologies().addAll(bsdlRegistry.addOntology(ontologyLocator));
        }

        // read and the service descriptor and store it, both as an ontology and as a service
        Collection<ISemanticIdentifier> readServicesIdentifiers = new ArrayList<ISemanticIdentifier>();

//        newServiceVO.getOntologies().addAll(readServicesIdentifiers);
        Collection<IServiceDescription> readServices = null;
        if (this.preParsingRules.size() > 0)
        {
            DocumentPreParser preParser = 
                    new DocumentPreParser(
                            unpackedServiceLocators.getServiceDescriptorLocator(),
                            this.preParsingRules);
            documentParser.registerPreParser(preParser);
            
            readServices = serviceRegistry.readServicesFrom(unpackedServiceLocators.getServiceDescriptorLocator(), documentLoader);
            
            preParser.writeModel();
        } else
        {
            readServices = serviceRegistry.readServicesFrom(unpackedServiceLocators.getServiceDescriptorLocator(), documentLoader);
        }
                

        BSDFLogger.getLogger().info("Loading and adding " + readServices.size() + " services");

        for (IServiceDescription service : readServices)
        {
            readServicesIdentifiers.add(service.getIdentifier());
        }
        newServiceVO.getServices().addAll(readServicesIdentifiers);

        // read and load the libraries
        for (ISemanticLocator libraryLocator : unpackedServiceLocators.getLibrariesLocators())
        {
            BSDFLogger.getLogger().info("Load library: " + libraryLocator.toString());
            // load JAR for ontology implementation
            try
            {
                List<Class<?>> classes = this.loadClassesFromJar(libraryLocator.getURI().getPath());
                BSDFLogger.getLogger().info("Loading and adding " + classes.size() + " classes");
                newServiceVO.getLoadedClasses().addAll(classes);
            } catch (IOException ex)
            {
                ex.printStackTrace();
                throw new ServiceDeployException(
                        "IOException (" + ex.toString() + ") Cannot load library implementation from: " + libraryLocator.toString(), ex);
            } catch (ClassNotFoundException ex)
            {
                ex.printStackTrace();
                throw new ServiceDeployException(
                        "ClassNotFoundException (" + ex.toString() + ") Cannot load library implementation from: " + libraryLocator.toString(), ex);
            } catch (URISyntaxException ex)
            {
                ex.printStackTrace();
                throw new ServiceDeployException(
                        "URISyntaxException (" + ex.toString() + ") Cannot load library implementation from: " + libraryLocator.toString(), ex);
            } catch (NoSuchMethodException ex)
            {
                ex.printStackTrace();
                throw new ServiceDeployException(
                        "NoSuchMethodException (" + ex.toString() + ") Cannot load library implementation from: " + libraryLocator.toString(), ex);
            } catch (IllegalAccessException ex)
            {
                ex.printStackTrace();
                throw new ServiceDeployException(
                        "IllegalAccessException (" + ex.toString() + ") Cannot load library implementation from: " + libraryLocator.toString(), ex);
            } catch (IllegalArgumentException ex)
            {
                ex.printStackTrace();
                throw new ServiceDeployException(
                        "IllegalArgumentException (" + ex.toString() + ") Cannot load library implementation from: " + libraryLocator.toString(), ex);
            } catch (InvocationTargetException ex)
            {
                ex.printStackTrace();
                throw new ServiceDeployException(
                        "InvocationTargetException (" + ex.toString() + ") Cannot load library implementation from: " + libraryLocator.toString(), ex);
            }
        }

        // read and load the ontology implementations
        for (ISemanticLocator ontologyImplementationLocator : unpackedServiceLocators.getOntologiesImplementationLocators())
        {
            BSDFLogger.getLogger().info("Load implementation: " + ontologyImplementationLocator.getSemanticIdentifier().toString());
            // load JAR for ontology implementation
            try
            {
                List<Class<?>> classes = this.loadClassesFromJar(ontologyImplementationLocator.getURI().getPath());
                BSDFLogger.getLogger().info("Loading and adding " + classes.size() + " classes");
                newServiceVO.getLoadedClasses().addAll(classes);
            } catch (IOException ex)
            {
                ex.printStackTrace();
                throw new ServiceDeployException(
                        "IOException (" + ex.toString() + ") Cannot load ontology implementation from: " + ontologyImplementationLocator.toString(), ex);
            } catch (ClassNotFoundException ex)
            {
                ex.printStackTrace();
                throw new ServiceDeployException(
                        "ClassNotFoundException (" + ex.toString() + ") Cannot load ontology implementation from: " + ontologyImplementationLocator.toString(), ex);
            } catch (URISyntaxException ex)
            {
                ex.printStackTrace();
                throw new ServiceDeployException(
                        "URISyntaxException (" + ex.toString() + ") Cannot load ontology implementation from: " + ontologyImplementationLocator.toString(), ex);
            } catch (NoSuchMethodException ex)
            {
                ex.printStackTrace();
                throw new ServiceDeployException(
                        "NoSuchMethodException (" + ex.toString() + ") Cannot load ontology implementation from: " + ontologyImplementationLocator.toString(), ex);
            } catch (IllegalAccessException ex)
            {
                ex.printStackTrace();
                throw new ServiceDeployException(
                        "IllegalAccessException (" + ex.toString() + ") Cannot load ontology implementation from: " + ontologyImplementationLocator.toString(), ex);
            } catch (IllegalArgumentException ex)
            {
                ex.printStackTrace();
                throw new ServiceDeployException(
                        "IllegalArgumentException (" + ex.toString() + ") Cannot load ontology implementation from: " + ontologyImplementationLocator.toString(), ex);
            } catch (InvocationTargetException ex)
            {
                ex.printStackTrace();
                throw new ServiceDeployException(
                        "InvocationTargetException (" + ex.toString() + ") Cannot load ontology implementation from: " + ontologyImplementationLocator.toString(), ex);
            }
        }

        // read and load the service implementations        
        try
        {
            BSDFLogger.getLogger().info("Load implementation: " + unpackedServiceLocators.getServiceImplementationLocator().getURI().getPath());
            List<Class<?>> classes = this.loadClassesFromJar(unpackedServiceLocators.getServiceImplementationLocator().getURI().getPath());
            BSDFLogger.getLogger().info("Loading and adding " + classes.size() + " classes");
            newServiceVO.getLoadedClasses().addAll(classes);

        } catch (IOException ex)
        {
            ex.printStackTrace();
            throw new ServiceDeployException(
                    "IOException (" + ex.toString() + ") Cannot load service implementation from: " + unpackedServiceLocators.getServiceImplementationLocator().toString(), ex);
        } catch (ClassNotFoundException ex)
        {
            ex.printStackTrace();
            throw new ServiceDeployException(
                    "ClassNotFoundException (" + ex.toString() + ") Cannot load service implementation from: " + unpackedServiceLocators.getServiceImplementationLocator().toString(), ex);
        } catch (URISyntaxException ex)
        {
            ex.printStackTrace();
            throw new ServiceDeployException(
                    "URISyntaxException (" + ex.toString() + ") Cannot load service implementation from: " + unpackedServiceLocators.getServiceImplementationLocator().toString(), ex);
        } catch (NoSuchMethodException ex)
        {
            ex.printStackTrace();
            throw new ServiceDeployException(
                    "NoSuchMethodException (" + ex.toString() + ") Cannot load service implementation from: " + unpackedServiceLocators.getServiceImplementationLocator().toString(), ex);
        } catch (IllegalAccessException ex)
        {
            ex.printStackTrace();
            throw new ServiceDeployException(
                    "IllegalAccessException (" + ex.toString() + ") Cannot load service implementation from: " + unpackedServiceLocators.getServiceImplementationLocator().toString(), ex);
        } catch (IllegalArgumentException ex)
        {
            ex.printStackTrace();
            throw new ServiceDeployException(
                    "IllegalArgumentException (" + ex.toString() + ") Cannot load service implementation from: " + unpackedServiceLocators.getServiceImplementationLocator().toString(), ex);
        } catch (InvocationTargetException ex)
        {
            ex.printStackTrace();
            throw new ServiceDeployException(
                    "InvocationTargetException (" + ex.toString() + ") Cannot load service implementation from: " + unpackedServiceLocators.getServiceImplementationLocator().toString(), ex);
        }

        loadJenaNamespaces(newServiceVO.getLoadedClasses());            

        this.servicesDB.updateServiceVO(newServiceVO);

        return newServiceVO.getServices();
    }

    @Override
    public Collection<ISemanticIdentifier> deployService(
            IServiceDescriptors serviceDescriptors,
            IBSDLRegistry bsdlRegistry,
            IRegistryLoader serviceRegistry) throws ModelException
    {
        BSDFLogger.getLogger().info("Deploys service(s) from already loaded service descriptor(s)");

        DescriptorWriter serviceDescriptorWriter = new DescriptorWriter(
                this.deploymentDirLocator,
                serviceDescriptors.getName(),
                serviceDescriptors.getServiceDescriptorContents().getName(),
                serviceDescriptors.getServiceDescriptorContents().getContents());

        serviceDescriptorWriter.write();

        String pathForServiceDescriptor = serviceDescriptorWriter.getPath();

        for (DescriptorData ontology : serviceDescriptors.getOntologiesDescriptorsContents())
        {
            DescriptorWriter ontologyDescriptorWriter = new DescriptorWriter(
                    this.deploymentDirLocator,
                    serviceDescriptors.getName(),
                    ontology.getName(),
                    ontology.getContents());
            ontologyDescriptorWriter.write();
        }

        // create a documentLoader over the given bsdlRegistry
        BSDLDocumentLoader documentLoader = new BSDLDocumentLoader(bsdlRegistry);

        // add to bsdlRegistry a bsdlConverter for OWL
        bsdlRegistry.registerConverter(new OWLConverter((BSDLRegistry) bsdlRegistry));
        // create and add a BSDL documentParser do the documentLoader
        documentLoader.registerDocumentParser(new BSDLBSDMLoader((BSDLRegistry) bsdlRegistry).createBSDLDocumentParser());

        // load the BSDM descriptors
        documentLoader.readFrom(BSDLBSDMLoader.descriptorLocationsForBSDMCore());

        // load the service
        Collection<IServiceDescription> readServices
                = serviceRegistry.readServicesFrom(
                        new SemanticLocator(
                                pathForServiceDescriptor
                                + serviceDescriptors.getServiceDescriptorContents().getName()),
                        documentLoader);

        Collection<ISemanticIdentifier> loadedServices = new ArrayList<ISemanticIdentifier>();

        for (IServiceDescription readService : readServices)
        {
            // eliminate the implementations if there is any, because this service has been received from an external source
            readService.disposeImplementations();

            LoadedServiceVO newServiceVO = this.getServicesDB().getNewServiceVO();

            // add service identifier to newServiceVO
            newServiceVO.getServices().add(readService.getIdentifier());

            // add service descriptor semantic locator to newServiceVO
            newServiceVO.getServiceLocators().setServiceDescriptorLocator(
                    new SemanticLocator(
                            pathForServiceDescriptor
                            + serviceDescriptors.getServiceDescriptorContents().getName()));

            // add ontologies descriptors to newServiceVO, and load each one to registry
            for (DescriptorData ontology : serviceDescriptors.getOntologiesDescriptorsContents())
            {
                Collection<ISemanticIdentifier> addedOntologies = bsdlRegistry.addOntology(
                        new SemanticLocator(
                                pathForServiceDescriptor
                                + ontology.getName()));
                newServiceVO.getOntologies().addAll(addedOntologies);

                newServiceVO.getServiceLocators().addOntologyDescriptorLocator(
                        new SemanticLocator(
                                pathForServiceDescriptor
                                + ontology.getName()));
            }

            this.getServicesDB().updateServiceVO(newServiceVO);

            loadedServices.addAll(newServiceVO.getServices());
        }

        return loadedServices;
    }

    @Override
    public void undeployService(
            ISemanticIdentifier serviceIdentifier,
            IBSDLRegistry bsdlRegistry,
            IRegistryLoader serviceRegistry) throws ModelException
    {
        BSDFLogger.getLogger().info("Undeploys service: " + serviceIdentifier);

        Collection<LoadedServiceVO> serviceVOs = this.servicesDB.getServiceVObyServiceIdentifier(serviceIdentifier);

        if (null == serviceVOs || serviceVOs.isEmpty())
        {
            return;
        }

        Iterator<LoadedServiceVO> serviceVOiter = serviceVOs.iterator();

        while (serviceVOiter.hasNext())
        {
            LoadedServiceVO serviceVO = serviceVOiter.next();

            if (!serviceVO.hasBeenUnregistered() && serviceVO.removeService(serviceIdentifier))
            {
                // de-register the axioms for the service
                IAxiom serviceAxiom = bsdlRegistry.axiomFor(serviceIdentifier);
                IOntology serviceOntology = null;
                if ((serviceAxiom instanceof SemanticAxiom)
                        && null != (serviceOntology = ((SemanticAxiom) serviceAxiom).getOntology().semanticAxiom()))
                {
                    bsdlRegistry.removeOntology(serviceOntology.identifier());
                }
                IComponent service = serviceRegistry.hasComponent(serviceIdentifier);
                if (null != service)
                {
                    serviceRegistry.unregisterComponent(service);
                }

                if (serviceVO.getServices().isEmpty())
                {
                    // de-register the axioms for the service associated ontologies
                    for (ISemanticIdentifier ontologyIdentifier : serviceVO.getOntologies())
                    {
                        bsdlRegistry.removeOntology(ontologyIdentifier);
                    }

                    // delete de temporary files of the unpacked service
                    try
                    {
                        Unpacker.deleteDirRecursively(composeDeploymentLocatorWithSufix(serviceVO.getIdSt()));
                    } catch (IOException ex)
                    {
                        throw new ServiceDeployException("Undeploy failed for: " + serviceIdentifier.toString(), ex);
                    }
                }

                return;
            } // if
        } // while        
    }

    public IServicesDB getServicesDB()
    {
        return this.servicesDB;
    }

    public void setDynamicJarLoader(IDynamicJarLoader dynamicJarLoader)
    {
        this.dynamicJarLoader = dynamicJarLoader;
    }

    private List<Class<?>> loadClassesFromJar(String filePathToJar) throws IOException, ClassNotFoundException, URISyntaxException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        if (null == this.dynamicJarLoader)
        {
            return ClassLoaders.loadAndGetClassFromJar(filePathToJar);
        } else
        {
            return this.dynamicJarLoader.loadClassesFromJar(filePathToJar);
        }
    }

    private String composeDeploymentLocatorWithSufix(String sufix) throws ParsingException
    {
        return deploymentDirLocator.getURI().getPath() + File.separator + sufix;
    }

    private void loadJenaNamespaces(List<Class<?>> classes)
    {
        for (Class clas : classes)
        {
            if (ObjectOWLSTranslator.isJenaBean(clas))
            {
                BSDFLogger.getLogger().info("Register JenaNamespace for " + clas.getSimpleName() + ": " + ObjectOWLSTranslator.getNamespaceFromJenaBean(clas) + " - " + clas.getPackage().getName());
                ObjectOWLSTranslator.registerNamespace(ObjectOWLSTranslator.getNamespaceFromJenaBean(clas), clas.getPackage().getName());
            }
        }
    }

}
