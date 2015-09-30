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

import com.hi3project.broccoli.bsdm.impl.exceptions.ServiceExecutionException;
import com.hi3project.vineyard.registry.client.ClientRequester;
import com.hi3project.broccoli.bsdm.api.asyncronous.IAsyncMessageClient;
import com.hi3project.broccoli.bsdm.api.asyncronous.IMessage;
import com.hi3project.broccoli.bsdf.api.discovery.IFunctionalitySearchResult;
import com.hi3project.broccoli.bsdl.impl.SemanticLocator;
import com.hi3project.broccoli.bsdm.impl.asyncronous.DescriptorData;
import com.hi3project.broccoli.bsdf.impl.asyncronous.SearchFunctionalityResultMessage;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.io.BSDFLogger;
import com.hi3project.broccoli.io.DescriptorWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;


/**
 * <p>
 * <b>Description:</b></p>
 * Command line launcher for a client requester that asks for a functionality
 * and writes the received answers.
 *
 *
 * <p>
 * <b>Creation date:</b>
 * 05-02-2015 </p>
 *
 * <p>
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 05-02-2015 - Initial release</li>
 * </ul>
 *
 *
 * 
 * @version 1
 */
public class ClientRequesterLauncher
{
    
    static int index = 1;

    /**
     * @param args the command line arguments
     * @throws com.hi3project.broccoli.bsdl.impl.exceptions.ModelException
     */
    public static void main(String[] args) throws ModelException
    {
        
        BSDFLogger.initInfoLogger();
        
        /* Args...
         needed: 1 resultsLocationPath, 1 requestedFunctionalityDescriptorPath, 1 externalContainerURL, 1 clientName
         */
        
        System.out.println("Launched with " + args.length + " args...");
        
        if (args.length != 4)
        {
            System.out.println("Usage: ClientRequesterLauncher resultsLocationPath requestedFunctionalityDescriptorPath externalContainerURL clientName");
            return;
        }

        final String resultsLocationPath = args[0];
        final String requestedFunctionalityDescriptorPath = args[1];
        final String externalContainerURL = args[2];
        final String clientName = args[3];

        final ClientRequester clientRequester = new ClientRequester(clientName, externalContainerURL);
        clientRequester.askForFunctionality(
                new SemanticLocator(requestedFunctionalityDescriptorPath),
                new IAsyncMessageClient()
                {

                    @Override
                    public void receiveMessage(IMessage msg) throws ModelException
                    {
                        if (msg instanceof SearchFunctionalityResultMessage)
                        {
                            
                            SearchFunctionalityResultMessage sfrMsg = (SearchFunctionalityResultMessage) msg;

                            PrintStream output = null;
                            try {
                                output = new PrintStream(System.out, true, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                throw new ServiceExecutionException("UTF not supported?", e);
                            }

                            output.println("Received: " + sfrMsg.toString());
                            
                            // write to an output some info for each received result
                            for (IFunctionalitySearchResult searchResult : sfrMsg.getSearchResults())
                            {
                                printResult(output, searchResult);
                            }                                                        
      
                            // write each received descriptor to a file
                            for (DescriptorData serviceDescriptorData : sfrMsg.getServiceDescriptorsContents())
                            {
                                DescriptorWriter descriptorWriter = 
                                        new DescriptorWriter(
                                                resultsLocationPath,
                                                String.valueOf(index),
                                                serviceDescriptorData.getName(),
                                                serviceDescriptorData.getContents());
                                descriptorWriter.write();
                            }
                            
                            for (DescriptorData ontologyDescriptorData : sfrMsg.getOntologiesDescriptorsContents())
                            {
                                DescriptorWriter descriptorWriter = 
                                        new DescriptorWriter(
                                                resultsLocationPath,
                                                String.valueOf(index),
                                                ontologyDescriptorData.getName(),
                                                ontologyDescriptorData.getContents());
                                descriptorWriter.write();
                            }
                            
                            index ++;
                        }
                    }

                    @Override
                    public String getName()
                    {
                        return clientRequester.getClientName();
                    }
                });

    }

    
    public static void printResult(PrintStream outputStream, IFunctionalitySearchResult searchResult)
    {
        outputStream.println("Functionality: " + searchResult.getAdvertisedFunctionalityName()
            + "From service: " + searchResult.getServiceDescription().name());
        outputStream.println("The service has " + searchResult.getServiceDescription().getGroundings().size() + " groundings");
//        outputStream.println("Evaluation: " + searchResult.getEvaluation().toString());
        outputStream.println("-----");
    }
    

}
