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

package com.hi3project.vineyard.test.tasks.stomp;

import com.hi3project.broccoli.bsdm.api.IServiceDescription;
import com.hi3project.broccoli.bsdm.api.asyncronous.IAsyncMessageClient;
import com.hi3project.broccoli.bsdm.api.asyncronous.IMessage;
import com.hi3project.broccoli.bsdm.api.parsing.IServiceDescriptionLoader;
import com.hi3project.broccoli.bsdm.api.profile.functionality.IAnnotatedValue;
import com.hi3project.broccoli.bsdm.api.profile.functionality.IOutputValue;
import com.hi3project.broccoli.bsdl.impl.SemanticLocator;
import com.hi3project.broccoli.bsdm.impl.asyncronous.FunctionalityResultMessage;
import com.hi3project.vineyard.comm.broker.AsyncMessageBroker;
import com.hi3project.broccoli.bsdf.impl.parsing.ServiceDescriptionLoader;
import com.hi3project.broccoli.bsdf.impl.serializing.JSONMessageSerializer;
import com.hi3project.broccoli.bsdf.exceptions.MessageBrokerException;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.vineyard.comm.FunctionalityGroundingFactory;
import com.hi3project.vineyard.comm.ServiceGroundingFactory;
import com.hi3project.broccoli.test.tareasmodel.Proyecto;
import com.hi3project.broccoli.test.tareasmodel.Tarea;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.security.auth.login.LoginException;
import com.hi3project.vineyard.comm.stomp.gozirraws.Client;
import com.hi3project.vineyard.comm.stomp.gozirraws.Listener;
import com.hi3project.vineyard.comm.stomp.gozirraws.WebSocketStomp;
import com.hi3project.vineyard.test.tasks.Config;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *  Tests suite for BSDM services with a Stomp grounding
 */
public class LoadAndExecuteBSDMServicesWithStompGroundingTest
{
    
    private static final String stompServiceWithoutImplDescriptorPath = Config.stompFilesDir() + "tasks_service_StompGrounding_complexInputAsListAndComplexOutput.xml";
    private static final String stompServiceWithImplDescriptorPath = Config.stompFilesDir() + "tasks_service_StompGrounding_complexInputAsListAndComplexOutputWithImpl.xml";
    
    private static final String stompWSServiceWithoutImplDescriptorPath = Config.stompFilesDir() + "tasks_service_StompWSGrounding_complexInputAsListAndComplexOutput.xml";
    private static final String stompWSServiceWithImplDescriptorPath = Config.stompFilesDir() + "tasks_service_StompWSGrounding_complexInputAsListAndComplexOutputWithImpl.xml";
    
    private static final String classesOntologyInBSDL = Config.testImplDir() + "tasksOntology.xml";
    public static final String owlToJavaReferencesDescriptorPath = Config.testImplDir() + "clientOntologyToJavaReference.xml";

    
    private AsyncMessageBroker messageBroker;
    

    @BeforeClass
    public static void setUpClass(){}
    
    @AfterClass
    public static void tearDownClass() {}
    
    
    @Before
    public void setUp() throws ModelException {}

    @After
    public void tearDown() throws MessageBrokerException {}


    /**
     * <ul>
     * <li>
     *     Loads a BSDM service with implementation and Stomp grounding into the AsyncMessageBroker
     * </li>
     * <li>
     *     Loads the same BSDM service without implementation using an IServiceDescriptionLoader
     * </li>
     * <li>
     *     Invokes a functionality for that service
     * </li>
     * </ul>
     * @throws Exception
     */
    @Test
    public void executeServiceWithStompGroundingWithoutLocalImplementation() throws Exception
    {

        // load service with local implementation
        if (null == this.messageBroker)
        {
            this.messageBroker = createBrokerAndLoadServiceWithImplementation(stompServiceWithImplDescriptorPath);
            this.messageBroker.start();
        }

        // load "client" service
        final String functionalityName = "buildProjectFrom";

        final IServiceDescriptionLoader serviceLoader =
                new ServiceDescriptionLoader(
                        ServiceGroundingFactory.getSingleton(),
                        FunctionalityGroundingFactory.getSingleton(),
                        new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()));
        IServiceDescription loadedService = loadServiceWithStompGroundingWithoutLocalImplementation(serviceLoader, stompServiceWithoutImplDescriptorPath);

        Collection<IAnnotatedValue> inputs = new ArrayList<>();
        List<Tarea> tareas = new ArrayList<>();
        tareas.add(new Tarea("una tarea"));
        tareas.add(new Tarea("otra tarea"));
        inputs.add(
                serviceLoader
                .getParameterConverter()
                .createAnnotatedValue(tareas));

        IAsyncMessageClient asyncMessageClient = new IAsyncMessageClient()
        {
            @Override
            public void receiveMessage(IMessage msg) throws ModelException
            {
                assertTrue(msg instanceof FunctionalityResultMessage);
                FunctionalityResultMessage resultMessage = (FunctionalityResultMessage) msg;
                assertEquals(resultMessage.getFunctionalityName(), functionalityName);
                IOutputValue outputValue = (IOutputValue) resultMessage.getResult().iterator().next();
                Object proyectoObject = serviceLoader.getParameterConverter().outputToObject(outputValue);
                assertTrue(proyectoObject instanceof Proyecto);
                Proyecto proyecto = (Proyecto) proyectoObject;
                assertTrue(proyecto.getTareas().size() > 1);
            }

            @Override
            public String getName()
            {
                return "Simple asyncronous little client";
            }
        };

        // invoke "client" service
        loadedService.executeMultipleResultFunctionalityAsyncronously(functionalityName, inputs, asyncMessageClient);
        
        Thread.sleep(10 * Config.debugTimeMultiplier() * 1000);
        
        this.messageBroker.stop();
        this.messageBroker = null;
    }


    /**
     * <ul>
     * <li>
     *     Loads a BSDM service with implementation and Stomp grounding into the AsyncMessageBroker
     * </li>
     * <li>
     *     Loads the same BSDM service without implementation using an IServiceDescriptionLoader
     * </li>
     * <li>
     *     Invokes a functionality for that service, two consecutive times
     * </li>
     * </ul>
     * @throws Exception
     */
    @Test
    public void executeServiceWithStompGroundingWithoutLocalImplementationTwice() throws Exception
    {

        // load service with local implementation
        if (null == this.messageBroker)
        {
            this.messageBroker = createBrokerAndLoadServiceWithImplementation(stompServiceWithImplDescriptorPath);
            this.messageBroker.start();
        }

        // load "client" service
        final String functionalityName = "buildProjectFrom";

        final IServiceDescriptionLoader serviceLoader =
                new ServiceDescriptionLoader(
                        ServiceGroundingFactory.getSingleton(),
                        FunctionalityGroundingFactory.getSingleton(),
                        new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()));
        IServiceDescription loadedService = loadServiceWithStompGroundingWithoutLocalImplementation(serviceLoader, stompServiceWithoutImplDescriptorPath);

        Collection<IAnnotatedValue> inputs = new ArrayList<>();
        List<Tarea> tareas = new ArrayList<>();
        tareas.add(new Tarea("una tarea"));
        tareas.add(new Tarea("otra tarea"));
        inputs.add(
                serviceLoader
                .getParameterConverter()
                .createAnnotatedValue(tareas));

        IAsyncMessageClient asyncMessageClient = new IAsyncMessageClient()
        {

            @Override
            public void receiveMessage(IMessage msg) throws ModelException
            {
                assertTrue(msg instanceof FunctionalityResultMessage);
                FunctionalityResultMessage resultMessage = (FunctionalityResultMessage) msg;
                assertEquals(resultMessage.getFunctionalityName(), functionalityName);
                IOutputValue outputValue = (IOutputValue) resultMessage.getResult().iterator().next();
                Object proyectoObject = serviceLoader.getParameterConverter().outputToObject(outputValue);
                assertTrue(proyectoObject instanceof Proyecto);
                Proyecto proyecto = (Proyecto) proyectoObject;
                assertTrue(proyecto.getTareas().size() > 1);
            }

            @Override
            public String getName()
            {
                return "Simple asyncronous little client";
            }
        };

        // invoke "client" service
        loadedService.executeMultipleResultFunctionalityAsyncronously(functionalityName, inputs, asyncMessageClient);
        
        Thread.sleep(1000);
        
        loadedService.executeMultipleResultFunctionalityAsyncronously(functionalityName, inputs, asyncMessageClient);
        
        Thread.sleep(10 * Config.debugTimeMultiplier() * 1000);
        
        this.messageBroker.stop();
        this.messageBroker = null;
    }


    /**
     * <ul>
     * <li>
     *     Loads a BSDM service with implementation and WebSockets over Stomp grounding into the AsyncMessageBroker
     * </li>
     * <li>
     *     Loads the same BSDM service without implementation using an IServiceDescriptionLoader
     * </li>
     * <li>
     *     Invokes a functionality for that service
     * </li>
     * </ul>
     * @throws Exception
     */
    @Test
    public void executeServiceWithStompWSGroundingWithoutLocalImplementation() throws Exception
    {

        // load service with local implementation
        if (null == this.messageBroker)
        {
            this.messageBroker = createBrokerAndLoadServiceWithImplementation(stompWSServiceWithImplDescriptorPath);
            this.messageBroker.start();
        }

        // load "client" service
        final String functionalityName = "buildProjectFrom";

        final IServiceDescriptionLoader serviceLoader =
                new ServiceDescriptionLoader(
                        ServiceGroundingFactory.getSingleton(),
                        FunctionalityGroundingFactory.getSingleton(),
                        new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()));
        IServiceDescription loadedService = loadServiceWithStompGroundingWithoutLocalImplementation(serviceLoader, stompWSServiceWithoutImplDescriptorPath);

        Collection<IAnnotatedValue> inputs = new ArrayList<>();
        List<Tarea> tareas = new ArrayList<>();
        tareas.add(new Tarea("una tarea"));
        tareas.add(new Tarea("otra tarea"));
        inputs.add(
                serviceLoader
                .getParameterConverter()
                .createAnnotatedValue(tareas));

        IAsyncMessageClient asyncMessageClient = new IAsyncMessageClient()
        {

            @Override
            public void receiveMessage(IMessage msg) throws ModelException
            {
                assertTrue(msg instanceof FunctionalityResultMessage);
                FunctionalityResultMessage resultMessage = (FunctionalityResultMessage) msg;
                assertEquals(resultMessage.getFunctionalityName(), functionalityName);
                IOutputValue outputValue = (IOutputValue) resultMessage.getResult().iterator().next();
                Object proyectoObject = serviceLoader.getParameterConverter().outputToObject(outputValue);
                assertTrue(proyectoObject instanceof Proyecto);
                Proyecto proyecto = (Proyecto) proyectoObject;
                assertTrue(proyecto.getTareas().size() > 1);
            }

            @Override
            public String getName()
            {
                return "Simple asyncronous little client";
            }
        };

        // invoke "client" service
        loadedService.executeMultipleResultFunctionalityAsyncronously(functionalityName, inputs, asyncMessageClient);
        
        Thread.sleep(15 * Config.debugTimeMultiplier() * 1000);
        
        this.messageBroker.stop();
        this.messageBroker = null;
    }
    
    
    public static AsyncMessageBroker createBrokerAndLoadServiceWithImplementation(String serviceDescriptor) throws MessageBrokerException, ModelException
    {
        AsyncMessageBroker messageBroker = new AsyncMessageBroker("aBroker");
        IServiceDescriptionLoader serviceLoader =
                new ServiceDescriptionLoader(
                        ServiceGroundingFactory.getSingleton(),
                        FunctionalityGroundingFactory.getSingleton(),
                        new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()));
        Collection<IServiceDescription> loadedServices = serviceLoader
                .addOntology(new SemanticLocator(classesOntologyInBSDL))
                .addOntologyReferences(new SemanticLocator(owlToJavaReferencesDescriptorPath))
                .readServicesFrom(new SemanticLocator(serviceDescriptor));

        assertTrue(!loadedServices.isEmpty());

        IServiceDescription loadedService = loadedServices.iterator().next();
        messageBroker.registerService(loadedService);
        return messageBroker;
    }

    public static IServiceDescription loadServiceWithStompGroundingWithoutLocalImplementation(
            IServiceDescriptionLoader serviceLoader, String serviceDescriptor) throws Exception
    {
        Collection<IServiceDescription> loadedServices = serviceLoader
                .addOntology(new SemanticLocator(classesOntologyInBSDL))
                .addOntologyReferences(new SemanticLocator(owlToJavaReferencesDescriptorPath))
                .readServicesFrom(new SemanticLocator(serviceDescriptor));

        assertTrue(!loadedServices.isEmpty());

        return loadedServices.iterator().next();
    }


    /**
     *  Test method meant to debug Stomp implementation.
     */
    @Test
    public void testStomp() throws IOException, LoginException, InterruptedException
    {
        
        String hostname = "localhost";
        int portNumber = 61613;
        
        String channelName = "/topic/nombrecito";
        
        if (Config.debugging())
        {
            Client client1 = new Client(hostname, portNumber, "", "");
            Client client2 = new Client(hostname, portNumber, "", "");
            
            client1.subscribe(channelName, new Listener()
            {

                @Override
                public void message(Map headers, String body)
                {
                    System.out.println("Mensajito mensajito: " + body);
                }
            });
            
            Thread.sleep(1000);
            
            client2.send(channelName, "poca cosa que comentar");
            client2.send(channelName, "pues eso");
            client2.send(channelName, "y tal");
            
            Thread.sleep(10 * Config.debugTimeMultiplier() * 1000);
            
        }
        
    }


    /**
     *  Test method meant to debug WebSockets over Stomp implementation.
     */
    @Test
    public void testWSStomp() throws IOException, LoginException, URISyntaxException, InterruptedException
    {
        
        String brokerURL = "ws://localhost:61614";
        String channelName = "/topic/nombrecito";
        
        if (Config.debugging())
        {
            
            WebSocketStomp client1 = new WebSocketStomp(new URI(brokerURL), "", "");
            WebSocketStomp client2 = new WebSocketStomp(new URI(brokerURL), "", "");
            
            client1.subscribe(channelName, new Listener()
            {

                @Override
                public void message(Map headers, String body)
                {
                    System.out.println("Mensajito mensajito: " + body);
                }
            });
            
            Thread.sleep(1000);
            
            client2.send(channelName, "poca cosa que comentar");
            client2.send(channelName, "pues eso");
            client2.send(channelName, "y tal");
            
            Thread.sleep(10 * Config.debugTimeMultiplier() * 1000);
            
        }
        
    }
    
}
