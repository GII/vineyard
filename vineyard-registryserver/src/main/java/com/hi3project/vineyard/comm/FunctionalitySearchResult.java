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

import com.hi3project.broccoli.bsdl.api.ISemanticIdentifier;
import com.hi3project.broccoli.bsdm.api.IServiceDescription;
import com.hi3project.broccoli.bsdf.api.discovery.IFunctionalitySearchEvaluation;
import com.hi3project.broccoli.bsdf.api.discovery.IFunctionalitySearchResult;
import com.hi3project.broccoli.bsdl.impl.exceptions.SemanticModelException;

/**
 *
 * 
 */
public class FunctionalitySearchResult implements IFunctionalitySearchResult
{

    private IServiceDescription serviceDescription = null;
    private String advertisedFunctionality = null;
    private IFunctionalitySearchEvaluation searchEvaluation = null;

    public FunctionalitySearchResult(
            IServiceDescription serviceDescription,
            String advertisedFunctionality,
            IFunctionalitySearchEvaluation searchEvaluation)
    {
        this.serviceDescription = serviceDescription;
        this.advertisedFunctionality = advertisedFunctionality;
        this.searchEvaluation = searchEvaluation;
    }

    @Override
    public IServiceDescription getServiceDescription()
    {
        return serviceDescription;
    }

    @Override
    public String getAdvertisedFunctionalityName()
    {
        return advertisedFunctionality;
    }

    @Override
    public IFunctionalitySearchEvaluation getEvaluation()
    {
        return searchEvaluation;
    }

    @Override
    public void setServiceDescription(IServiceDescription serviceDescription)
    {
        this.serviceDescription = serviceDescription;
    }

    @Override
    public ISemanticIdentifier getServiceIdentifier() throws SemanticModelException
    {
        return null != getServiceDescription() ? getServiceDescription().getIdentifier() : null;
    }

}
