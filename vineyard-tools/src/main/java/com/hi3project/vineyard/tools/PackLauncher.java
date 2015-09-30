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

import com.hi3project.broccoli.bsdl.impl.SemanticLocator;
import com.hi3project.broccoli.bsdl.impl.exceptions.ParsingException;
import com.hi3project.broccoli.bsdf.exceptions.ServiceDeployException;
import com.hi3project.broccoli.io.BSDFLogger;
import java.io.File;
import java.io.IOException;

/**
* <p>
 *  <b>Description:</b></p>
 *  Command line tool that uses Unpacker to pack BSD services.
 *
 *
 * <p><b>Creation date:</b> 
 * 10-02-2015 </p>
 *
 * <p><b>Changelog:</b></p>
 * <ul>
 * <li> 1 , 10-02-2015 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class PackLauncher 
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParsingException, ServiceDeployException, IOException 
    {
        
        BSDFLogger.initInfoLogger();
        
        /* Args...
         needed: 1 baseDirPath, 1 packedServicePath
         */
        
        System.out.println("Launched with " + args.length + " args...");
        
        if (args.length != 2)
        {
            System.out.println("Usage: PackLauncher baseDirPath packedServicePath");
            return;
        }

        final String baseDirPath = args[0];
        final String packedServicePath = args[1];
        
        File baseDirFile = new File(baseDirPath);
        File packedServiceFile = new File(packedServicePath);
        
        if (!baseDirFile.exists())
        {
            System.out.println("There is nothing here: " + baseDirFile.toPath().toString());
            System.exit(1);
        }
        
        Unpacker unpacker = new Unpacker();
        unpacker.packService(
                new SemanticLocator(baseDirFile.getCanonicalPath()),
                new SemanticLocator(packedServiceFile.getCanonicalPath()));
    }

}
