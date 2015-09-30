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

package com.hi3project.vineyard.comm;

import com.hi3project.broccoli.bsdm.impl.grounding.LocalImplementationGrounding;
import com.hi3project.broccoli.bsdm.api.IServiceDescription;
import com.hi3project.broccoli.bsdm.api.grounding.IServiceGrounding;
import com.hi3project.broccoli.bsdm.api.grounding.IServiceGroundingFactory;
import com.hi3project.broccoli.bsdl.impl.Instance;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdm.api.grounding.IFunctionalityGroundingFactory;
import com.hi3project.broccoli.bsdm.api.serializing.IMessageSerializer;
import com.hi3project.broccoli.io.BSDFLogger;

/**
 * <p><b>Description:</b></p>
 *  Factory class that creates a IServiceGrounding object for a given groundingType.
 *
 *
 * <p><b>Creation date:</b> 
 * 23-06-2014 </p>
 *
 * <p><b>Changelog:</b></p>
 * <ul>
 * <li> 1 , 23-06-2014 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class ServiceGroundingFactory implements IServiceGroundingFactory
{
    
    private static ServiceGroundingFactory singletonInstance;
    
    
    public ServiceGroundingFactory() {}
    
    
    public static ServiceGroundingFactory getSingleton()
    {
        if (null == singletonInstance)
        {
            singletonInstance = new ServiceGroundingFactory();
        }
        return singletonInstance;
    }

    @Override
    public IServiceGrounding instanceFor(
            IFunctionalityGroundingFactory funcGroundingFactory,
            String groundingType,
            Instance instance,
            IServiceDescription serviceDescription,
            IMessageSerializer messageSerializer) throws ModelException
    {
        if (groundingType.equalsIgnoreCase("http://hi3project.com/broccoli/bsdm/grounding#asyncMessageGrounding")
                || groundingType.equalsIgnoreCase("http://hi3project.com/broccoli/bsdm/grounding#asyncJMSMessageGrounding")
                || groundingType.contains("tcp"))
        {
            BSDFLogger.getLogger().info("Builds an AsyncJMSMessageServiceGrounding for: " + groundingType);
            return new AsyncMessageServiceGroundingImpl(messageSerializer, funcGroundingFactory, instance, serviceDescription);
        }
        if (groundingType.equalsIgnoreCase("http://hi3project.com/broccoli/bsdm/grounding#asyncStompMessageGrounding")
                || groundingType.contains("stomp"))
        {
            BSDFLogger.getLogger().info("Builds an AsyncStompMessageServiceGrounding for: " + groundingType);
            return new AsyncMessageServiceGroundingImpl(messageSerializer, funcGroundingFactory, instance, serviceDescription);
        }
        if (groundingType.equalsIgnoreCase("http://hi3project.com/broccoli/bsdm/grounding#localImplementationGrounding"))
        {
            BSDFLogger.getLogger().info("Builds a LocalImplementationGrounding for: " + groundingType);
            return new LocalImplementationGrounding(funcGroundingFactory, messageSerializer, instance, serviceDescription);
        }
        BSDFLogger.getLogger().debug("Cannot build a service grounding for: " + groundingType);
        return null;
    }

}
