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

package com.hi3project.vineyard.test.tasks.deploy;

import com.hi3project.broccoli.bsdf.api.deployment.IUnpacker;
import com.hi3project.broccoli.bsdf.api.discovery.IFunctionalitySearchResult;
import com.hi3project.broccoli.bsdl.api.ISemanticIdentifier;
import com.hi3project.broccoli.bsdm.api.asyncronous.IAsyncMessageClient;
import com.hi3project.broccoli.bsdm.api.asyncronous.IMessage;
import com.hi3project.broccoli.bsdm.api.profile.functionality.IAnnotatedValue;
import com.hi3project.broccoli.bsdm.api.profile.functionality.IOutputValue;
import com.hi3project.broccoli.bsdm.api.profile.functionality.IRequestedFunctionality;
import com.hi3project.broccoli.bsdm.api.profile.functionality.IResult;
import com.hi3project.broccoli.bsdl.impl.SemanticIdentifier;
import com.hi3project.broccoli.bsdl.impl.SemanticLocator;
import com.hi3project.broccoli.bsdf.impl.asyncronous.SearchFunctionalityResultMessage;
import com.hi3project.broccoli.bsdf.impl.discovery.BasicFunctionalitySearchEvaluation;
import com.hi3project.vineyard.container.yotta.BSDMServiceContainer;
import com.hi3project.vineyard.container.yotta.SimpleDeployerForContainerWithServiceRegistry;
import com.hi3project.vineyard.tools.Unpacker;
import com.hi3project.vineyard.container.micro.BSDMServiceMicroContainer;
import com.hi3project.vineyard.registry.client.ClientRequester;
import com.hi3project.broccoli.bsdf.impl.parsing.ServiceDescriptionLoader;
import com.hi3project.broccoli.bsdf.impl.serializing.JSONMessageSerializer;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.vineyard.comm.FunctionalityGroundingFactory;
import com.hi3project.vineyard.comm.ServiceGroundingFactory;
import com.hi3project.broccoli.test.tareasmodel.Proyecto;
import com.hi3project.broccoli.test.tareasmodel.Tarea;
import com.hi3project.vineyard.registry.client.BrokerConnectorsConfig;
import com.hi3project.vineyard.test.tasks.Config;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * 
 */
public class UnpackAndDeployBSDMServicesTest
{

    public static final String unpackedServicePath = Config.deployFilesDir() + "unpackedServiceExample";
    public static final String packedServicePath1 = Config.deployFilesDir() + "packedServiceExample" + File.separator + "service.bsd";
    public static final String packedServicePath2 = Config.deployFilesDir() + "anotherPackedServiceExample" + File.separator + "anotherService.bsd";
    
    public static final String whereToUnpackServicePath = Config.deployFilesDir() + "tempUnpackedServiceExample";
    
    public static final String requestedFunctionalityWithComplexInputAsListAndComplexOutputDescriptorPath = 
            Config.deployFilesDir() + "requestedFunctionalityWithComplexInputAsListAndComplexOutput.xml";
    public static final String requestedFunctionalityWithComplexInputOptionalAndComplexOutputAsListDescriptorPath = 
            Config.deployFilesDir() + "requestedFunctionalityWithComplexInputOptionalAndComplexOutputAsList.xml";
    
    private static final String classesOntologyInBSDL = Config.testImplDir() + "tasksOntology.xml";
    public static final String owlToJavaReferencesDescriptorPath = Config.testImplDir() + "clientOntologyToJavaReference.xml";

    private boolean deleteUnpackDir = true;
    

    public UnpackAndDeployBSDMServicesTest() {}

    @BeforeClass
    public static void setUpClass() throws IOException {}

    @AfterClass
    public static void tearDownClass() throws IOException {}

    @Before
    public void setUp() {}

    @After
    public void tearDown(){}

    @Test
    public void packServiceExample() throws Exception
    {
        File fileForPackedServicePath1 = new File(packedServicePath1);
        if (fileForPackedServicePath1.exists())
        {
            fileForPackedServicePath1.delete();
        }
        fileForPackedServicePath1 = new File(packedServicePath1);
        assertFalse(fileForPackedServicePath1.exists());
        IUnpacker unpacker = new Unpacker();
        unpacker.packService(new SemanticLocator(unpackedServicePath), new SemanticLocator(packedServicePath1));
        fileForPackedServicePath1 = new File(packedServicePath1);
        assertTrue(fileForPackedServicePath1.exists());
    }       
    
    @Test
    public void unpackServiceExample() throws Exception
    {
        if (new File(whereToUnpackServicePath).exists())
        {
            Unpacker.deleteDirRecursively(whereToUnpackServicePath);
        }

        // first we pack the example service
        IUnpacker unpacker = new Unpacker();
        unpacker.packService(new SemanticLocator(unpackedServicePath), new SemanticLocator(packedServicePath1));

        // now we try to unpack it
        unpacker.unpackService(new SemanticLocator(packedServicePath1), new SemanticLocator(whereToUnpackServicePath));

        File unpackedServiceDescriptorFile = new File(whereToUnpackServicePath + File.separator + "serviceExample.bsdm");
        assertTrue(unpackedServiceDescriptorFile.exists());
        File unpackedServiceFullImplementationFile = new File(whereToUnpackServicePath
                + File.separator + Unpacker.SERVICE_IMPLEMENTATION_SUBDIRECTORY
                + File.separator + "serviceImplAndOntologyToo.jar");
        assertTrue(unpackedServiceFullImplementationFile.exists());
        File unpackedServiceOntologyDescriptorFile = new File(whereToUnpackServicePath
                + File.separator + Unpacker.SERVICE_ONTOLOGIES_SUBDIRECTORY
                + File.separator + "ontology.owl");
        assertTrue(unpackedServiceOntologyDescriptorFile.exists());
    }

    @Test
    public void deployServiceExample1() throws Exception
    {
        if (new File(whereToUnpackServicePath).exists() && this.deleteUnpackDir)
        {
            Unpacker.deleteDirRecursively(whereToUnpackServicePath);
        }

        // we pack the example service
        new Unpacker().packService(new SemanticLocator(unpackedServicePath), new SemanticLocator(packedServicePath1));

        // we deploy the service into a BSDLRegistry and BSDMServiceRegistry        
        SimpleDeployerForContainerWithServiceRegistry deployer = new SimpleDeployerForContainerWithServiceRegistry(new SemanticLocator(whereToUnpackServicePath));
        deployer.deployService(new SemanticLocator(packedServicePath1));

        assertTrue(deployer.getBsdlRegistry().ontologies().size() >= 3);

        // we load a client required functionality
        ServiceDescriptionLoader clientLoader
                = new ServiceDescriptionLoader(
                        ServiceGroundingFactory.getSingleton(),
                        FunctionalityGroundingFactory.getSingleton(),
                        new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()));
        Collection<IRequestedFunctionality> readFunctionalities
                = clientLoader
                .addOntology(new SemanticLocator(classesOntologyInBSDL))
                .addOntologyReferences(new SemanticLocator(owlToJavaReferencesDescriptorPath))
                .readFunctionalitiesFrom(new SemanticLocator(requestedFunctionalityWithComplexInputAsListAndComplexOutputDescriptorPath));

        assertFalse(readFunctionalities.isEmpty());

        // we build and convert a domain object for the client side to be used as input so we can invoke the found functionality
        Collection<IAnnotatedValue> inputs = new ArrayList<>();
        inputs.add(
                clientLoader
                .getParameterConverter()
                .createAnnotatedValue(Arrays.asList(new Tarea("una tarea"), new Tarea("otra tarea"))));

        // we search for this functionality using the BSDMServiceRegistry previously created
        Collection<IFunctionalitySearchResult> searchResults = deployer.getBsdmServiceRegistry().searchFor(readFunctionalities.iterator().next());

        assertFalse(searchResults.isEmpty());

        IFunctionalitySearchResult searchResult = searchResults.iterator().next();
        // finally the found functionality is invoked, and we check the result
        Collection<IResult> result = searchResult.getServiceDescription().executeSingleResultFunctionalitySyncronously(searchResult.getAdvertisedFunctionalityName(), inputs);

        assertTrue(result.size() > 0);

        IOutputValue outputValue = (IOutputValue) result.iterator().next();
        Object proyectoObject = clientLoader.getParameterConverter().outputToObject(outputValue);

        assertTrue(proyectoObject instanceof Proyecto);
        Proyecto proyecto = (Proyecto) proyectoObject;
        assertTrue(proyecto.getTareas().size() > 1);
        assertNotNull(outputValue.getOutput());
        assertTrue(outputValue.getOutput().name().equals("Proyecto"));

    }

    @Test
    public void deployServiceExample2() throws Exception
    {
        if (new File(whereToUnpackServicePath).exists() && this.deleteUnpackDir)
        {
            Unpacker.deleteDirRecursively(whereToUnpackServicePath);
        }

        // we deploy the service into a BSDLRegistry and BSDMServiceRegistry        
        SimpleDeployerForContainerWithServiceRegistry deployer = new SimpleDeployerForContainerWithServiceRegistry(new SemanticLocator(whereToUnpackServicePath));
        deployer.deployService(new SemanticLocator(packedServicePath2));

        assertTrue(deployer.getBsdlRegistry().ontologies().size() >= 3);
    }

    @Test
    public void deployServiceExample3() throws Exception
    {
        Thread.sleep(2 * Config.debugTimeMultiplier() * 1000);
        
        if (new File(whereToUnpackServicePath).exists())
        {
            Unpacker.deleteDirRecursively(whereToUnpackServicePath);
        }

        BSDMServiceContainer serviceContainer
                = new BSDMServiceContainer(
                        new SemanticLocator(whereToUnpackServicePath),
                        new SemanticIdentifier("http://hi3project.com/vineyard/test/container#aServContainer"));

        serviceContainer.registerService(new SemanticLocator(packedServicePath1));

        serviceContainer.start();

        // we search for this functionality using a ClientRequester
        ClientRequester clientRequester = new ClientRequester("client requester");

        clientRequester.askForFunctionality(
                new SemanticLocator(requestedFunctionalityWithComplexInputOptionalAndComplexOutputAsListDescriptorPath),
                new IAsyncMessageClient()
                {

                    @Override
                    public void receiveMessage(IMessage msg) throws ModelException
                    {
                        assertTrue(msg instanceof SearchFunctionalityResultMessage);
                        SearchFunctionalityResultMessage sfResultMessage = (SearchFunctionalityResultMessage) msg;
                        assertTrue(sfResultMessage.getServiceDescriptorsContents().size() > 0);
                        assertTrue(sfResultMessage.getSearchResults().size() > 0);
                        IFunctionalitySearchResult searchResult = sfResultMessage.getSearchResults().iterator().next();
                        assertTrue(searchResult.getEvaluation().isBetterThan(new BasicFunctionalitySearchEvaluation(0, true)));
                    }

                    @Override
                    public String getName()
                    {
                        return "client requester callback";
                    }
                });

        Thread.sleep(20 * Config.debugTimeMultiplier() * 1000);

        serviceContainer.stop();

        Thread.sleep(2 * Config.debugTimeMultiplier() * 1000);
    }

    @Test
    public void undeployServices() throws Exception
    {
        if (new File(whereToUnpackServicePath).exists())
        {
            Unpacker.deleteDirRecursively(whereToUnpackServicePath);
        }

        this.deleteUnpackDir = false;

        this.deployServiceExample1();
        this.deployServiceExample2();

        this.deleteUnpackDir = true;

        SimpleDeployerForContainerWithServiceRegistry deployer = new SimpleDeployerForContainerWithServiceRegistry(new SemanticLocator(whereToUnpackServicePath));
        Collection<ISemanticIdentifier> deployedServices1 = deployer.deployService(new SemanticLocator(packedServicePath1));
        Collection<ISemanticIdentifier> deployedServices2 = deployer.deployService(new SemanticLocator(packedServicePath2));

        assertTrue(deployer.getBsdlRegistry().ontologies().size() >= 4);

        deployer.undeployService(deployedServices1.iterator().next());

        assertTrue(deployer.getServicesDB().getServiceVObyId(1).getServices().isEmpty());

        assertNotNull(deployer.getBsdlRegistry().annotatedObjectFor(new SemanticIdentifier("http://hi3project.com/vineyard/test/tasks/service#serviceProfile")));

        deployer.undeployService(deployedServices2.iterator().next());

        assertNull(deployer.getBsdlRegistry().annotatedObjectFor(new SemanticIdentifier("http://hi3project.com/vineyard/test/tasks/service#serviceProfile")));

        assertTrue(deployer.getServicesDB().getServiceVObyId(2).getServices().isEmpty());
    }


    @Test
    public void deployServiceAndSearchRemotelyUsingControlChannel() throws Exception
    {
        Thread.sleep(2 * Config.debugTimeMultiplier() * 1000);
        
        if (new File(whereToUnpackServicePath).exists())
        {
            Unpacker.deleteDirRecursively(whereToUnpackServicePath);
        }

        BSDMServiceContainer serviceContainer
                = new BSDMServiceContainer(
                        new SemanticLocator(whereToUnpackServicePath),
                        new SemanticIdentifier("http://hi3project.com/vineyard/test/container#aServContainer"));

        serviceContainer.registerService(new SemanticLocator(packedServicePath1));

        serviceContainer.start();

        ClientRequester clientRequester = new ClientRequester("client requester");

        clientRequester.askForFunctionality(
                new SemanticLocator(requestedFunctionalityWithComplexInputAsListAndComplexOutputDescriptorPath),
                new IAsyncMessageClient()
                {

                    @Override
                    public void receiveMessage(IMessage msg) throws ModelException
                    {
                        assertTrue(msg instanceof SearchFunctionalityResultMessage);
                        SearchFunctionalityResultMessage sfResultMessage = (SearchFunctionalityResultMessage) msg;
                        assertTrue(sfResultMessage.getServiceDescriptorsContents().size() > 0);
                        assertTrue(sfResultMessage.getSearchResults().size() > 0);
                        IFunctionalitySearchResult searchResult = sfResultMessage.getSearchResults().iterator().next();
                        assertEquals(
                                searchResult.getAdvertisedFunctionalityName(),
                                "buildProjectFrom");
                        assertEquals(
                                searchResult.getServiceDescription().getIdentifier().toString(),
                                "http://hi3project.com/vineyard/test/tasks/service#serviceDescription");
                    }

                    @Override
                    public String getName()
                    {
                        return "client requester callback";
                    }
                });

        Thread.sleep(20 * Config.debugTimeMultiplier() * 1000);

        serviceContainer.stop();

        Thread.sleep(2 * Config.debugTimeMultiplier() * 1000);
    }

    @Test
    public void desployServiceToMicroContainer() throws Exception
    {
        Thread.sleep(2 * Config.debugTimeMultiplier() * 1000);
        
        if (new File(whereToUnpackServicePath).exists())
        {
            Unpacker.deleteDirRecursively(whereToUnpackServicePath);
        }

        BSDMServiceContainer serviceContainer
                = new BSDMServiceContainer(
                        new SemanticLocator(whereToUnpackServicePath),
                        new SemanticIdentifier("http://hi3project.com/vineyard/test/container#aServContainer"));

        serviceContainer.start();

        BSDMServiceMicroContainer serviceMicroContainer
                = new BSDMServiceMicroContainer(
                        new SemanticLocator(whereToUnpackServicePath),
                        new SemanticIdentifier("http://hi3project.com/vineyard/test/container#aServContainer"));

        serviceMicroContainer.setRemoteBroker(new SemanticIdentifier(BrokerConnectorsConfig.openWireConnectorURL));

        serviceMicroContainer.start();

        serviceMicroContainer.registerService(new SemanticLocator(packedServicePath1));

        ClientRequester clientRequester = new ClientRequester("client requester");

        clientRequester.askForFunctionality(
                new SemanticLocator(requestedFunctionalityWithComplexInputAsListAndComplexOutputDescriptorPath),
                new IAsyncMessageClient()
                {

                    @Override
                    public void receiveMessage(IMessage msg) throws ModelException
                    {
                        assertTrue(msg instanceof SearchFunctionalityResultMessage);
                        SearchFunctionalityResultMessage sfResultMessage = (SearchFunctionalityResultMessage) msg;
                        if (sfResultMessage.getSearchResults().size() > 0)
                        {
                            IFunctionalitySearchResult searchResult = sfResultMessage.getSearchResults().iterator().next();
                            assertEquals(
                                searchResult.getAdvertisedFunctionalityName(),
                                "buildProjectFrom");
                            assertEquals(
                                searchResult.getServiceDescription().getIdentifier().toString(),
                                "http://hi3project.com/vineyard/test/tasks/service#serviceDescription");
                        }
                    }

                    @Override
                    public String getName()
                    {
                        return "client requester callback";
                    }
                });

        Thread.sleep(20 * Config.debugTimeMultiplier() * 1000);

        serviceContainer.stop();

        Thread.sleep(2 * Config.debugTimeMultiplier() * 1000);
    }

}
