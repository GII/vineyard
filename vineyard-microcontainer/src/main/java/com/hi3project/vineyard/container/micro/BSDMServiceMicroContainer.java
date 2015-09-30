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

package com.hi3project.vineyard.container.micro;

import com.hi3project.vineyard.container.AbstractBSDMServiceContainer;
import com.hi3project.broccoli.bsdl.api.ISemanticIdentifier;
import com.hi3project.broccoli.bsdl.api.ISemanticLocator;
import com.hi3project.broccoli.bsdm.api.asyncronous.IChannelProducer;
import com.hi3project.broccoli.bsdf.api.deployment.container.IBSDMServiceMicroContainer;
import com.hi3project.broccoli.bsdm.impl.asyncronous.DescriptorData;
import com.hi3project.broccoli.bsdf.impl.asyncronous.RegisterServiceRequestMessage;
import com.hi3project.broccoli.bsdf.impl.deployment.bd.LoadedServiceVO;
import com.hi3project.broccoli.bsdf.impl.serializing.JSONMessageSerializer;
import com.hi3project.broccoli.bsdl.api.parsing.PreParsingRule;
import static com.hi3project.vineyard.registry.client.ClientRequester.ContainerRequestChannelName;
import com.hi3project.vineyard.registry.server.SimpleBSDMServiceLoader;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdm.api.IServiceDescription;
import com.hi3project.broccoli.bsdm.api.grounding.IServiceGrounding;
import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceExecutionException;
import com.hi3project.broccoli.io.BSDFLogger;
import com.hi3project.broccoli.io.DescriptorReader;
import com.hi3project.vineyard.comm.ChannelFactory;
import com.hi3project.vineyard.comm.FunctionalityGroundingFactory;
import com.hi3project.vineyard.comm.ServiceGroundingFactory;
import com.hi3project.vineyard.tools.IDynamicJarLoader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>
 * <b>Description:</b></p>
 * Implementation of IBSDMServiceMicroContainer.
 *
 *
 * <p>
 * <b>Creation date:</b>
 * 23-01-2015 </p>
 *
 * <p>
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 23-01-2015 - Initial release</li>
 * </ul>
 *
 *
 * 
 * @version 1
 */
public class BSDMServiceMicroContainer extends AbstractBSDMServiceContainer implements IBSDMServiceMicroContainer
{

    private SimpleBSDMServiceLoader serviceLoader = null;

    private boolean started = false;
    private boolean connected = false;

    private IChannelProducer requestChannelProducer = null;

    private Collection<IServiceGrounding> activeGroundings = new ArrayList<IServiceGrounding>();

    private ISemanticIdentifier serviceProvider = null;

    private IDynamicJarLoader dynamicJarLoader = null;

    public BSDMServiceMicroContainer(
            ISemanticLocator workingLocation,
            ISemanticIdentifier containerName) throws ModelException
    {
        super(workingLocation, containerName);
        this.serviceLoader = new SimpleBSDMServiceLoader(
                ServiceGroundingFactory.getSingleton(),
                FunctionalityGroundingFactory.getSingleton(),
                new JSONMessageSerializer(ServiceGroundingFactory.getSingleton(), FunctionalityGroundingFactory.getSingleton()));
        this.started = false;
        this.connected = false;
    }

    public void setDynamicJarLoader(IDynamicJarLoader dynamicJarLoader)
    {
        this.dynamicJarLoader = dynamicJarLoader;
    }

    @Override
    public String toString()
    {
        return "BSDMServiceMicroContainer{" + "workingLocation=" + workingLocation + '}';
    }

    @Override
    public void setRemoteBroker(ISemanticIdentifier remoteBrokerIdentifier) throws ServiceExecutionException
    {
        this.serviceProvider = remoteBrokerIdentifier;
        if (this.hasStarted())
        {
            try
            {
                this.connectToBroker();
            } catch (ModelException ex)
            {
                throw new ServiceExecutionException("Error starting micro container connecting to: " + this.serviceProvider.toString(), ex);
            }
        }
    }

    @Override
    public synchronized void registerService(ISemanticLocator packedServiceDescriptor) throws ServiceExecutionException
    {
        if (!this.hasStarted() || !this.isConnected())
        {
            throw new ServiceExecutionException("microContainer must be connected to register a service...");
        }
        try
        {

            BSDFLogger.getLogger().info("Registers service: " + packedServiceDescriptor.toString());

            SimpleDeployerForMicroContainer deployer = new SimpleDeployerForMicroContainer(workingLocation, this.serviceLoader, this.servicesDB);
            if (null != this.dynamicJarLoader)
            {
                deployer.setDynamicJarLoader(this.dynamicJarLoader);
            }
            deployer.addPreParsingRule(
                    new PreParsingRule("with", "property", "url", "value", this.serviceProvider.getURI().toString()));
            Collection<ISemanticIdentifier> deployedServices = deployer.deployService(packedServiceDescriptor);
            for (ISemanticIdentifier deployedServiceIdentifier : deployedServices)
            {
                Collection<LoadedServiceVO> serviceVOs = this.getServicesDB().getServiceVObyServiceIdentifier(deployedServiceIdentifier);
                for (LoadedServiceVO serviceVO : serviceVOs)
                {
                    RegisterServiceRequestMessage message
                            = new RegisterServiceRequestMessage(
                                    this.containerName.getLastName(),
                                    this.conversationIdGenerator.getNextIdS());

                    String serviceDescriptorContents = new DescriptorReader(serviceVO.getServiceLocators().getServiceDescriptorLocator()).readAsString();

                    message.setServiceDescriptorContents(
                            new DescriptorData(
                                    serviceVO.getServiceLocators().getServiceDescriptorLocator().getLastName(),
                                    serviceDescriptorContents));

                    for (ISemanticLocator ontologiesDescriptorLocator : serviceVO.getServiceLocators().getOntologiesDescriptorLocators())
                    {

                        String ontologyDescriptorContents = new DescriptorReader(ontologiesDescriptorLocator).readAsString();
                        message.addOntologyDescriptorContents(
                                new DescriptorData(ontologiesDescriptorLocator.getLastName(), ontologyDescriptorContents));

                    }

                    this.requestChannelProducer.send(message);

                    IServiceDescription deployedService = this.serviceLoader.getService(deployedServiceIdentifier);

                    for (IServiceGrounding serviceGrounding : deployedService.getGroundings())
                    {
                        serviceGrounding.activate();
                        this.activeGroundings.add(serviceGrounding);
                    }

                }
            }

        } catch (ModelException ex)
        {
            throw new ServiceExecutionException("Error deploying service in micro container: " + packedServiceDescriptor.toString(), ex);
        }

    }

    @Override
    public void start() throws ServiceExecutionException
    {
        this.started = true;
        if (null == this.requestChannelProducer)
        {
            try
            {
                this.connectToBroker();
            } catch (ModelException ex)
            {
                throw new ServiceExecutionException("Error starting micro container connecting to: " + this.serviceProvider.toString(), ex);
            }
        }
    }

    @Override
    public void stop() throws ServiceExecutionException
    {
        this.started = false;
        if (null != this.requestChannelProducer)
        {
            this.requestChannelProducer.close();
            this.requestChannelProducer = null;
        }
        for (IServiceGrounding grounding : this.activeGroundings)
        {
            grounding.deactivate();
        }
        this.activeGroundings = new ArrayList<IServiceGrounding>();
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

    @Override
    public boolean isConnected()
    {
        return this.connected;
    }

    private void connectToBroker() throws ModelException
    {
        String connectionString = this.serviceProvider.getURI().toString();

        this.requestChannelProducer
                = ChannelFactory.getSingleton().controlChannelProducerInstanceFor(
                        connectionString,
                        ContainerRequestChannelName,
                        this.bsdlRegistry);

        this.connected = (this.requestChannelProducer != null);
    }

}
