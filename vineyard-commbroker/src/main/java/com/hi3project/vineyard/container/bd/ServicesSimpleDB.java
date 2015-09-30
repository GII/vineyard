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

package com.hi3project.vineyard.container.bd;


import com.hi3project.broccoli.bsdf.impl.deployment.bd.LoadedServiceVO;
import com.hi3project.broccoli.bsdf.api.deployment.bd.IServicesDB;
import com.hi3project.broccoli.bsdl.api.ISemanticIdentifier;
import com.hi3project.broccoli.io.BSDFLogger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;


/**
 * <p><b>Description:</b></p>
 *
 *
 * <p><b>Creation date:</b> 
 * 15-05-2014 </p>
 *
 * <p><b>Changelog:</b></p>
 * <ul>
 * <li> 1 , 15-05-2014 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class ServicesSimpleDB implements IServicesDB
{
    
    private ArrayList<LoadedServiceVO> loadedVOsById;
    
    private HashMap<ISemanticIdentifier, HashSet<LoadedServiceVO>> loadedVOsByServiceIdentifier;
    
    private HashMap<ISemanticIdentifier, HashSet<LoadedServiceVO>> loadedVOsByOntologyIdentifier;
        
        
    
    public ServicesSimpleDB()
    {
        this.loadedVOsById = new ArrayList<LoadedServiceVO>();
        this.loadedVOsByServiceIdentifier = new HashMap<ISemanticIdentifier, HashSet<LoadedServiceVO>>();
        this.loadedVOsByOntologyIdentifier = new HashMap<ISemanticIdentifier, HashSet<LoadedServiceVO>>();
        BSDFLogger.getLogger().info("Instances a ServicesSimpleDB");
    }
    

    @Override
    public synchronized LoadedServiceVO getNewServiceVO()
    {
        LoadedServiceVO newServiceVO = new LoadedServiceVO(this.loadedVOsById.size() + 1);
        loadedVOsById.add(newServiceVO);
        return newServiceVO;
    }

    @Override
    public synchronized LoadedServiceVO getServiceVObyId(int id)
    {
        return loadedVOsById.get(id - 1);
    }

    @Override
    public synchronized void updateServiceVO(LoadedServiceVO loadedServiceVO)
    {
        loadedVOsById.set(loadedServiceVO.getId() - 1, loadedServiceVO);
        for (ISemanticIdentifier service: loadedServiceVO.getServices())
        {
            if (null == this.loadedVOsByServiceIdentifier.get(service))
            {
                this.loadedVOsByServiceIdentifier.put(service, new HashSet<LoadedServiceVO>());
            }
            this.loadedVOsByServiceIdentifier.get(service).add(loadedServiceVO);
        }
        for (ISemanticIdentifier ontology: loadedServiceVO.getOntologies())
        {
            if (null == this.loadedVOsByOntologyIdentifier.get(ontology))
            {
                this.loadedVOsByOntologyIdentifier.put(ontology, new HashSet<LoadedServiceVO>());
            }
            this.loadedVOsByOntologyIdentifier.get(ontology).add(loadedServiceVO);
        }
    }

    @Override
    public Collection<LoadedServiceVO> getServiceVObyOntologyIdentifier(ISemanticIdentifier ontologyIdentifier)
    {
        return loadedVOsByOntologyIdentifier.get(ontologyIdentifier);
    }

    @Override
    public Collection<LoadedServiceVO> getServiceVObyServiceIdentifier(ISemanticIdentifier serviceIdentifier)
    {
        return loadedVOsByServiceIdentifier.get(serviceIdentifier);
    }

    @Override
    public synchronized void deleteServiceVO(LoadedServiceVO loadedServiceVO)
    {
        loadedVOsById.set(loadedServiceVO.getId() - 1, null);
    }

}
