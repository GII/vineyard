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

import com.hi3project.broccoli.bsdl.impl.SemanticIdentifier;
import com.hi3project.broccoli.bsdl.impl.SemanticLocator;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.io.BSDFLogger;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * <b>Description:</b></p>
 * Launcher for MicroContainer to be executed from command line.
 *
 *
 * <p>
 * <b>Creation date:</b>
 * 04-02-2015 </p>
 *
 * <p>
 * <b>Changelog:</b></p>
 * <ul>
 * <li> 1 , 04-02-2015 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class MicroContainerLauncher
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
         needed: 1 deploymentLocation, 1 containerId, 1 externalContainerIdentifier. 1 pathToPackedService
         */
        System.out.println("Launched with " + args.length + " args...");

        if (args.length < 4)
        {
            System.out.println("Usage: MicroContainerLauncher deploymentLocation containerId externalContainerIdentifier pathToPackedService");
            return;
        }

        String deploymentLocation = args[0];
        String containerId = args[1];
        String externalContainerIdentifier = args[2];

        List<String> listOfPathToPackedService = new ArrayList<String>();
        // register packed services in initialized container
        for (int index = 3; index < args.length; index++)
        {
            listOfPathToPackedService.add(args[index]);
        }

        launchMicroContainer(deploymentLocation, containerId, externalContainerIdentifier, listOfPathToPackedService);

    }

    public static void launchMicroContainer(
            String deploymentLocation,
            String containerId,
            String externalContainerIdentifier,
            List<String> listOfPathToPackedService) throws ModelException, InterruptedException
    {
        BSDMServiceMicroContainer serviceMicroContainer
                = new BSDMServiceMicroContainer(
                        new SemanticLocator(deploymentLocation),
                        new SemanticIdentifier(containerId));

        serviceMicroContainer.setRemoteBroker(new SemanticIdentifier(externalContainerIdentifier));

        serviceMicroContainer.start();

        for (String pathToPackedService : listOfPathToPackedService)
        {
            serviceMicroContainer.registerService(new SemanticLocator(pathToPackedService));
        }

//        while(true)
//        {
//           Thread.sleep(1000000); 
//        }
        
    }

}
