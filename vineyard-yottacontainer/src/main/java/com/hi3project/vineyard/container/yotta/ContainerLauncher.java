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

import com.hi3project.broccoli.bsdl.impl.SemanticIdentifier;
import com.hi3project.broccoli.bsdl.impl.SemanticLocator;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.io.BSDFLogger;


/**
 * <p>
 * <b>Description:</b></p>
 * Command line launcher for BSDM container.
 *
 *
 * <p>
 * <b>Creation date:</b>
 * 04-02-2015 </p>
 *
 * <p>
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 04-02-2015 - Initial release</li>
 * </ul>
 *
 *
 * 
 * @version 1
 */
public class ContainerLauncher
{

    /**
     * @param args the command line arguments
     * @throws com.hi3project.broccoli.bsdl.impl.exceptions.ModelException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws ModelException, InterruptedException
    {
        
        BSDFLogger.initInfoLogger();

        /* Args...
         needed: 1 deploymentLocation, 1 containerId, 1 containerURL
         optional: 1,N externalContainerIdentifier
         at least one service. For each service: 1 pathToPackedService
         */
        System.out.println("Launched with " + args.length + " args...");

        if (args.length < 3)
        {
            System.out.println("Usage: ContainerLauncher deploymentLocation containerId containerURL [externalContainerIdentifier] pathToPackedService");
            return;
        }

        String deploymentLocation = args[0];
        String containerId = args[1];
        String containerURL = args[2];

        // container initialization
        BSDMServiceContainer serviceContainer
                = new BSDMServiceContainer(
                        new SemanticLocator(deploymentLocation),
                        new SemanticIdentifier(containerId),
                        containerURL);

        if (args.length > 3)
        {
            int index = 3;

            // register external containers in initialized container
            while (args[index].contains("://"))
            {
                String externalContainerIdentifier = args[index];
                serviceContainer.addRemoteContainer(new SemanticIdentifier(externalContainerIdentifier));
                index++;
            }

            // register packed services in initialized container
            for (; index < args.length; index++)
            {
                String pathToPackedService = args[index];
                serviceContainer.registerService(new SemanticLocator(pathToPackedService));
            }
        }

        // launch container
        serviceContainer.start();

//        while(true)
//        {
//           Thread.sleep(1000000); 
//        }

    }

}
