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

package com.hi3project.vineyard.container.pico;

import com.hi3project.broccoli.bsdl.impl.SemanticIdentifier;
import com.hi3project.broccoli.bsdl.impl.SemanticLocator;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.io.BSDFLogger;

/**
 * <p>
 * <b>Description:</b></p>
 *  Launcher for PicoContainer to be executed from command line.
 *  A service loaded with the PicoContainer cannot be invoked, 
 * as it is not registered in any broker.
 *
 *
 * <p>
 * <b>Creation date:</b>
 * 06-03-2015 </p>
 *
 * <p>
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 06-03-2015 - Initial release</li>
 * </ul>
 *
 *
 * 
 * @version 1
 */
public class PicoContainerLauncher
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
         needed: 1 deploymentLocation, 1 containerId. 1 pathToPackedService
         */
        System.out.println("Launched with " + args.length + " args...");

        if (args.length != 3)
        {
            System.out.println("Usage: PicoContainerLauncher deploymentLocation containerId pathToPackedService");
            return;
        }

        String deploymentLocation = args[0];
        String containerId = args[1];
        String pathToPackedService = args[2];

        BSDMServicePicoContainer servicePicoContainer
                = new BSDMServicePicoContainer(
                        new SemanticLocator(deploymentLocation),
                        new SemanticIdentifier(containerId));

        servicePicoContainer.start();

        servicePicoContainer.registerService(new SemanticLocator(pathToPackedService));

        Thread.sleep(1000000);
    }

}
