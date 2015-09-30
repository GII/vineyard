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


import com.hi3project.broccoli.bsdm.impl.grounding.AbstractFunctionalityGrounding;
import com.hi3project.broccoli.bsdm.impl.grounding.AsyncMessageServiceGrounding;
import com.hi3project.broccoli.bsdm.api.grounding.IFunctionalityGroundingFactory;
import com.hi3project.broccoli.bsdm.api.grounding.IServiceGrounding;
import com.hi3project.broccoli.bsdl.impl.Instance;
import com.hi3project.broccoli.bsdm.impl.asyncronous.ChannelManager;
import com.hi3project.broccoli.bsdl.impl.exceptions.SemanticModelException;
import com.hi3project.broccoli.bsdm.api.serializing.IMessageSerializer;
import com.hi3project.broccoli.io.BSDFLogger;

/**
 * <p><b>Description:</b></p>
 *
 *
 * <p><b>Creation date:</b> 
 * 18-06-2014 </p>
 *
 * <p><b>Changelog:</b></p>
 * <ul>
 * <li> 1 , 18-06-2014 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class FunctionalityGroundingFactory implements IFunctionalityGroundingFactory
{
    
    private static FunctionalityGroundingFactory singletonInstance;
    
    
    public FunctionalityGroundingFactory() {}
    
    public static FunctionalityGroundingFactory getSingleton()
    {
        if (null == singletonInstance)
        {
            singletonInstance = new FunctionalityGroundingFactory();
        }
        return singletonInstance;
    }

    @Override
    public AbstractFunctionalityGrounding instanceFor(
            String groundingType,
            Instance instance,
            IServiceGrounding serviceGrounding,
            IMessageSerializer messageSerializer,
            ChannelManager channelHelper)
            throws SemanticModelException
    {
        if (groundingType.equalsIgnoreCase("http://hi3project.com/broccoli/bsdm/grounding#asyncMessageFunctionalityGrounding")
                || groundingType.equalsIgnoreCase("http://hi3project.com/broccoli/bsdm/grounding#asyncJMSMessageFunctionalityGrounding"))
        {
            BSDFLogger.getLogger().info("Builds an AsyncMessageFunctionalityGrounding for: " + instance.toString());
            return new AsyncMessageFunctionalityGrounding(messageSerializer, instance, (AsyncMessageServiceGrounding)serviceGrounding, channelHelper);
        }
        if (groundingType.equalsIgnoreCase("http://hi3project.com/broccoli/bsdm/grounding#asyncStompMessageFunctionalityGrounding"))
        {
            BSDFLogger.getLogger().info("Builds an AsyncMessageFunctionalityGrounding for: " + instance.toString());
            return new AsyncMessageFunctionalityGrounding(messageSerializer, instance, (AsyncMessageServiceGrounding)serviceGrounding, channelHelper);
        }
        BSDFLogger.getLogger().debug("Cannot build a functionality grounding for: " + instance.toString());
        return null;
    }

}
