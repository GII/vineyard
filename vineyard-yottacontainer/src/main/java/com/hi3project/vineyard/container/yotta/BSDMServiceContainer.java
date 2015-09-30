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

import com.hi3project.vineyard.container.AbstractBSDMServiceContainer;
import com.hi3project.broccoli.bsdl.api.ISemanticIdentifier;
import com.hi3project.broccoli.bsdl.api.ISemanticLocator;
import com.hi3project.broccoli.bsdm.api.asyncronous.IAsyncMessageClient;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelConsumer;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelProducer;
import com.hi3project.broccoli.bsdm.api.asyncronous.IMessage;
import com.hi3project.broccoli.bsdm.api.asyncronous.IMessageBroker;
import com.hi3project.broccoli.bsdm.api.asyncronous.IMessageTimeoutCallback;
import com.hi3project.broccoli.bsdf.api.deployment.container.IBSDMServiceContainer;
import com.hi3project.broccoli.bsdf.api.discovery.IFunctionalitySearchResult;
import com.hi3project.broccoli.bsdf.api.discovery.IMatchmaker;
import com.hi3project.broccoli.bsdf.api.discovery.IServiceRegistry;
import com.hi3project.broccoli.bsdm.api.profile.functionality.IRequestedFunctionality;
import com.hi3project.broccoli.bsdm.impl.asyncronous.DescriptorData;
import com.hi3project.broccoli.bsdf.impl.asyncronous.RegisterServiceRequestMessage;
import com.hi3project.broccoli.bsdf.impl.asyncronous.SearchFunctionalityRequestMessage;
import com.hi3project.broccoli.bsdf.impl.asyncronous.SearchFunctionalityResultMessage;
import com.hi3project.vineyard.tools.Deployer;
import com.hi3project.vineyard.tools.Unpacker;
import com.hi3project.broccoli.bsdf.impl.deployment.bd.LoadedServiceVO;
import com.hi3project.vineyard.registry.client.ClientRequester;
import com.hi3project.vineyard.comm.broker.AsyncMessageBroker;
import com.hi3project.broccoli.bsdf.impl.parsing.ServiceDescriptionLoader;
import com.hi3project.broccoli.bsdf.impl.serializing.JSONMessageSerializer;
import com.hi3project.vineyard.registry.server.BSDMServiceRegistry;
import com.hi3project.vineyard.registry.server.SimpleStructuralMatchmaker;
import com.hi3project.broccoli.bsdf.exceptions.MessageBrokerException;
import com.hi3project.broccoli.bsdl.api.parsing.PreParsingRule;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceExecutionException;
import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceGroundingException;
import com.hi3project.broccoli.io.BSDFLogger;
import com.hi3project.broccoli.io.DescriptorReader;
import com.hi3project.vineyard.comm.ChannelFactory;
import com.hi3project.vineyard.comm.FunctionalityGroundingFactory;
import com.hi3project.vineyard.comm.ReceivedMessagesChecker;
import com.hi3project.vineyard.comm.ServiceGroundingFactory;
import com.hi3project.vineyard.registry.client.BrokerConnectorsConfig;
import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>
 * <b>Description:</b></p>
 * Implementation of IBSDMServiceContainer.
 *
 *
 * <p>
 * <b>Creation date:</b>
 * 17-07-2014 </p>
 *
 * <p>
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 17-07-2014 - Initial release</li>
 * </ul>
 *
 *
 * 
 * @version 1
 */
public final class BSDMServiceContainer extends AbstractBSDMServiceContainer implements IBSDMServiceContainer
{

    private BSDMServiceRegistry bsdmServiceRegistry;

    private AsyncMessageBroker messageBroker;

    private Collection<ConnectionWithExternalContainer> externalContainers;

    private IChannelProducer containerChannelProducer = null;

    private MessageReceiverWithTimeout messageReceiverWithTimeoutForSearchResults;

    private String urlContainer;
    
    private String ipContainer;

    private static final String CONV_ID_SEPARATOR = ";";

    public BSDMServiceContainer(
            ISemanticLocator workingLocation,
            ISemanticIdentifier containerName) throws ModelException
    {
        this(workingLocation, containerName, BrokerConnectorsConfig.openWireConnectorURL);
    }

    public BSDMServiceContainer(
            ISemanticLocator workingLocation,
            ISemanticIdentifier containerName,
            String urlContainer) throws ModelException
    {
        super(workingLocation, containerName);

        this.urlContainer = urlContainer;
        
        this.ipContainer = AbstractBSDMServiceContainer.getIPfromURL(urlContainer);

        this.messageReceiverWithTimeoutForSearchResults = new MessageReceiverWithTimeout("Receiver for " + containerName.getLastName());

        this.bsdmServiceRegistry = new BSDMServiceRegistry(
                ServiceGroundingFactory.getSingleton(),
                FunctionalityGroundingFactory.getSingleton(),
                new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()));
        this.bsdmServiceRegistry.setMatchmaker(new SimpleStructuralMatchmaker(bsdlRegistry));
        this.messageBroker
                = new AsyncMessageBroker("AsyncMessageBroker-" + containerName.getLastName());
        this.externalContainers = new ArrayList<ConnectionWithExternalContainer>();

        BSDFLogger.getLogger().info("Instances a BSDMServiceContainer");
    }
    
    

    @Override
    public synchronized void start() throws ServiceExecutionException
    {
        try
        {

            this.messageBroker.setMainControlChannel(this);

            this.messageReceiverWithTimeoutForSearchResults.start();

            this.messageBroker.start();
            
            this.bsdmServiceRegistry.initServiceImplementations();

        } catch (MessageBrokerException ex)
        {
            throw new ServiceExecutionException(ex.getMessage(), ex);
        }
    }

    @Override
    public synchronized void stop() throws ServiceExecutionException
    {
        try
        {           

            if (null != this.containerChannelProducer)
            {
                this.containerChannelProducer.close();
                this.containerChannelProducer = null;
            }
            
            this.messageBroker.stop();

            this.messageReceiverWithTimeoutForSearchResults.stop();

        } catch (MessageBrokerException ex)
        {
            throw new ServiceExecutionException(ex.getMessage(), ex);
        }
    }

    @Override
    public synchronized void restart() throws ServiceExecutionException
    {
        try
        {

            this.messageBroker.restart();

        } catch (MessageBrokerException ex)
        {
            throw new ServiceExecutionException(ex.getMessage(), ex);
        }
    }

    /*
     With the current implementation, a service must be registered before the
     message broker is started... or restart the broker to activate messaging for
     that service.
     */
    @Override
    public void registerService(ISemanticLocator packedServiceDescriptor) throws ServiceExecutionException
    {
        try
        {

            BSDFLogger.getLogger().info("Deploys service(s) from: " + packedServiceDescriptor.toString());

            Collection<ISemanticIdentifier> deployedServices = this.deploy(packedServiceDescriptor, bsdmServiceRegistry);

            for (ISemanticIdentifier deployedService : deployedServices)
            {
                this.messageBroker.registerService(
                        this.bsdmServiceRegistry.getService(deployedService).applyConnectorURL(this.urlContainer));
            }

        } catch (ModelException ex)
        {
            BSDFLogger.getLogger().debug("Cannot deploy service(s) from: " + packedServiceDescriptor.toString()
                    + " . Exception: " + ex.toString());
            throw new ServiceExecutionException(ex.getMessage(), ex);
        }
    }

    @Override
    public IMessageBroker getMessageBroker()
    {
        return this.messageBroker;
    }

    @Override
    public IServiceRegistry getBSDMServiceRegistry()
    {
        return this.bsdmServiceRegistry;
    }

    @Override
    public IMatchmaker getMatchmaker()
    {
        return this.bsdmServiceRegistry.getMatchmaker();
    }

    @Override
    public void setMatchmaker(IMatchmaker matchmaker)
    {
        this.bsdmServiceRegistry.setMatchmaker(matchmaker);
    }

    @Override
    public String toString()
    {
        return "BSDMServiceContainer{" + "workingLocation=" + workingLocation.getLastName() + '}';
    }

    @Override
    public synchronized void activateControlChannels() throws ModelException
    {
        
        ControlChannelReceiver receiver = new ControlChannelReceiver();
        ReceivedMessagesChecker messagesChecker = new ReceivedMessagesChecker();

        IChannelConsumer containerChannelConsumer
                = ChannelFactory.getSingleton().controlChannelConsumerInstanceFor(
                        BrokerConnectorsConfig.getOpenWireURLfromIP(this.ipContainer),
                        ClientRequester.ContainerRequestChannelName,
                        this.bsdlRegistry,
                        messagesChecker);

        containerChannelConsumer.addClientCallback(receiver);

        containerChannelConsumer
                = ChannelFactory.getSingleton().controlChannelConsumerInstanceFor(
                        BrokerConnectorsConfig.getStompURLfromIP(this.ipContainer),
                        ClientRequester.ContainerRequestChannelName,
                        this.bsdlRegistry,
                        messagesChecker);

        containerChannelConsumer.addClientCallback(receiver);

        containerChannelConsumer
                = ChannelFactory.getSingleton().controlChannelConsumerInstanceFor(
                        BrokerConnectorsConfig.getWSURLfromIP(this.ipContainer),
                        ClientRequester.ContainerRequestChannelName,
                        this.bsdlRegistry,
                        messagesChecker);

        containerChannelConsumer.addClientCallback(receiver);

        containerChannelProducer
                = ChannelFactory.getSingleton().controlChannelProducerInstanceFor(
                        this.urlContainer,
                        ClientRequester.ContainerResponseChannelName,
                        this.bsdlRegistry);

    }

    @Override
    public synchronized void addRemoteContainer(ISemanticIdentifier remoteBrokerIdentifier) throws ServiceExecutionException
    {
        BSDFLogger.getLogger().info("Registers connection to remote container: " + remoteBrokerIdentifier.toString());
        ConnectionWithExternalContainer externalContainer
                = new ConnectionWithExternalContainer(remoteBrokerIdentifier, this.bsdlRegistry);
        try
        {
            externalContainer.connect();
            externalContainer.addResponseCallback(new IAsyncMessageClient()
            {

                @Override
                public void receiveMessage(IMessage msg) throws ModelException
                {
                    if (msg instanceof SearchFunctionalityResultMessage)
                    {
                        BSDFLogger.getLogger().info("Process received SearchFunctionalityResultMessage: " + msg.toString()
                                + " from: " + BSDMServiceContainer.this.toString());
                        BSDMServiceContainer.this.messageReceiverWithTimeoutForSearchResults.receiveMessage(msg);
                    }
                }

                @Override
                public String getName()
                {
                    return BSDMServiceContainer.this.containerName.getLastName();
                }
            });
        } catch (ModelException ex)
        {
            throw new ServiceExecutionException(remoteBrokerIdentifier.toString(), ex);
        }
        this.externalContainers.add(externalContainer);
    }

    private synchronized Collection<ISemanticIdentifier> deploy(
            ISemanticLocator packedServiceDescriptor,
            IServiceRegistry bsdmServiceRegistry) throws ModelException
    {
        SimpleDeployerForContainerWithServiceRegistry deployer
                = new SimpleDeployerForContainerWithServiceRegistry(
                        new Unpacker(),
                        this.servicesDB,
                        workingLocation,
                        bsdmServiceRegistry,
                        this.bsdlRegistry);

        deployer.addPreParsingRule(
                new PreParsingRule("with", "property", "url", "value", this.urlContainer));

        return deployer.deployService(packedServiceDescriptor);
    }

    private SearchFunctionalityResultMessage searchForFunctionalities(
            SearchFunctionalityRequestMessage sfRequestMsg,
            Collection<IRequestedFunctionality> readFunctionalities) throws ServiceGroundingException, ModelException
    {
        SearchFunctionalityResultMessage sfResultMsg
                = new SearchFunctionalityResultMessage(
                        sfRequestMsg.getClientName(),
                        sfRequestMsg.getConversationId());

        for (IRequestedFunctionality reqFunc : readFunctionalities)
        {
            Collection<IFunctionalitySearchResult> searchResults = getBSDMServiceRegistry().searchFor(reqFunc);
            for (IFunctionalitySearchResult searchResult : searchResults)
            {
                sfResultMsg.addSearchResult(searchResult);
                Collection<LoadedServiceVO> serviceVOs = servicesDB.getServiceVObyServiceIdentifier(searchResult.getServiceDescription().getIdentifier());
                for (LoadedServiceVO serviceVO : serviceVOs)
                {

                    String serviceContents = new DescriptorReader(serviceVO.getServiceLocators().getServiceDescriptorLocator()).readAsString();
                    sfResultMsg.addServiceDescriptorContents(
                            new DescriptorData(
                                    serviceVO.getServiceLocators().getServiceDescriptorLocator().getLastName(),
                                    serviceContents));

                    for (ISemanticLocator ontologieDescriptorLocator : serviceVO.getServiceLocators().getOntologiesDescriptorLocators())
                    {
                        String ontologyContents = new DescriptorReader(ontologieDescriptorLocator).readAsString();
                        sfResultMsg.addOntologyDescriptorContents(
                                new DescriptorData(
                                        ontologieDescriptorLocator.getLastName(),
                                        ontologyContents));
                    }

                }

            }
        }

        return sfResultMsg;
    }

    private void sendSearchResultToExternalContainers(
            SearchFunctionalityRequestMessage sfRequestMsg,
            String clientName) throws ServiceGroundingException
    {
        for (ConnectionWithExternalContainer externalContainer : BSDMServiceContainer.this.externalContainers)
        {
            final SearchFunctionalityRequestMessage redirectMsg;
            try
            {
                redirectMsg = (SearchFunctionalityRequestMessage) sfRequestMsg.clone();
                redirectMsg.setClientName(clientName);
                redirectMsg.setConversationId(sfRequestMsg.getClientName() + CONV_ID_SEPARATOR + sfRequestMsg.getConversationId());
                externalContainer.getRequestChannelProducer().send(redirectMsg);
            } catch (CloneNotSupportedException ex)
            {
                throw new ServiceGroundingException("Cannot redirect search functionality message to external container", ex);
            }
        }
    }

    private class ControlChannelReceiver implements IAsyncMessageClient
    {

        @Override
        public void receiveMessage(IMessage msg) throws ModelException
        {
            if (msg instanceof SearchFunctionalityRequestMessage)
            {
                BSDFLogger.getLogger().info("Process received SearchFunctionalityRequestMessage: " + msg.toString()
                        + " from: " + this.toString());

                final SearchFunctionalityRequestMessage sfRequestMsg = (SearchFunctionalityRequestMessage) msg;

                // load the functionalities as IRequestedFunctionality instances
                ServiceDescriptionLoader loader
                        = new ServiceDescriptionLoader(
                                ServiceGroundingFactory.getSingleton(),
                                FunctionalityGroundingFactory.getSingleton(),
                                bsdlRegistry,
                                new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()));
                Collection<IRequestedFunctionality> readFunctionalities = loader.readFunctionalities(sfRequestMsg.getRequestedFunctionalityContents());

                // search functionalitites and build the result message
                final SearchFunctionalityResultMessage sfResultMsg = searchForFunctionalities(sfRequestMsg, readFunctionalities);

                if (BSDMServiceContainer.this.externalContainers.size() > 0)
                {
                    // if there are any external containers, a timeout callback is created
                    //to handle the SearchFunctionalityResultMessage received from them
                    TimeoutCallback callback = new TimeoutCallback(5000, new IMessageTimeoutCallback()
                    {

                        @Override
                        public void timeout(Collection<IMessage> messages)
                        {
                            for (IMessage msg : messages)
                            {
                                if (msg instanceof SearchFunctionalityResultMessage)
                                {
                                    // for each SearchFunctionalityResultMessage received, its contents
                                    //are merged with the results obtained for This container, so only
                                    //one results message is sent to the client
                                    SearchFunctionalityResultMessage sfrMessage = (SearchFunctionalityResultMessage) msg;
                                    if (sfrMessage.getClientName().equals(BSDMServiceContainer.this.containerName.getLastName()))
                                    {
                                        String[] split = sfrMessage.getConversationId().split(CONV_ID_SEPARATOR);
                                        if (split.length != 2)
                                        {
                                            BSDFLogger.getLogger().error(
                                                    "ConversationId should be: [clientName;clientConvId] but is: ["
                                                    + sfrMessage.getConversationId()
                                                    + "]");
                                        }
                                        try
                                        {
                                            SearchFunctionalityResultMessage redirectMsg = (SearchFunctionalityResultMessage) sfrMessage.clone();
                                            redirectMsg.setClientName(split[0]);
                                            redirectMsg.setConversationId(split[1]);

                                            sfResultMsg.merge(redirectMsg);

                                        } catch (CloneNotSupportedException ex)
                                        {
                                            BSDFLogger.getLogger().error("Error redirecting a SearchFunctionalityResultMessage", ex);
                                        }
                                    }

                                }
                            }
                            try
                            {
                                // send results back
                                BSDMServiceContainer.this.containerChannelProducer.send(sfResultMsg);
                            } catch (ServiceGroundingException ex)
                            {
                                BSDFLogger.getLogger().error(ex.toString());
                            }
                        }
                    });

                    // registers the timeout callback with the proper conversationId
                    BSDMServiceContainer.this.messageReceiverWithTimeoutForSearchResults.addCalback(
                            sfRequestMsg.getClientName() + CONV_ID_SEPARATOR + sfRequestMsg.getConversationId(),
                            callback);

                    // send search functionality msg to external containers
                    BSDMServiceContainer.this.sendSearchResultToExternalContainers(sfRequestMsg, this.getName());

                } else
                {
                    // send results back
                    BSDMServiceContainer.this.containerChannelProducer.send(sfResultMsg);
                }

            }

            if (msg instanceof RegisterServiceRequestMessage)
            {
                BSDFLogger.getLogger().info("Process received RegisterServiceRequestMessage: " + msg.toString()
                        + " from: " + this.toString());

                final RegisterServiceRequestMessage rsRequestMessage = (RegisterServiceRequestMessage) msg;

                // "deploy" the service(s) with the given descriptors data
                Deployer deployer = new Deployer(BSDMServiceContainer.this.getServicesDB(), BSDMServiceContainer.this.getWorkingLocation());
                deployer.deployService(rsRequestMessage, bsdlRegistry, bsdmServiceRegistry);

            }
        }

        @Override
        public String getName()
        {
            return BSDMServiceContainer.this.containerName.getLastName();
        }
    }

}
