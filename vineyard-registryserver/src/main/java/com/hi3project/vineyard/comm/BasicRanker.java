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

import com.hi3project.broccoli.bsdf.impl.discovery.BasicFunctionalitySearchEvaluation;
import com.hi3project.broccoli.bsdf.api.discovery.IFunctionalitySearchEvaluation;
import com.hi3project.broccoli.bsdf.api.discovery.IRanker;
import com.hi3project.broccoli.bsdl.impl.exceptions.ModelException;
import com.hi3project.broccoli.bsdm.api.profile.functionality.IRequestedFunctionality;

/**
 *
 * <p><b>Creation date:</b> 
 * 14-05-2015 </p>
 *
 * <b>Changelog:</b>
 * <ul>
 * <li> 1 , 14-05-2015 - Initial release</li>
 * </ul>
 *
 * 
 * @version 1
 */
public class BasicRanker implements IRanker
{
    
    private static int MATCH = 5;        
    
    
    private int attemptedNumberOfRequiredProperties = 0;
    
    private int attemptedNumberOfOptionalProperties = 0;
    
    private int attemptedNumberOfPreferredProperties = 0;
    
    
    private int resultingNumberOfRequiredProperties = 0;
    
    private int resultingNumberOfOptionalProperties = 0;
    
    private int resultingNumberOfPreferredProperties = 0;
    
    
    private int resultingValueForRequiredProperties = 0;
    
    private int resultingValueForOptionalProperties = 0;
    
    private int resultingValueForPreferredProperties = 0;
    
    
    public BasicRanker(IRequestedFunctionality requestedFunctionality) throws ModelException
    {
        super();
        this.configureFor(requestedFunctionality);
    }
    

    @Override
    public final void configureFor(IRequestedFunctionality requestedFunctionality) throws ModelException
    {
        
        this.attemptedNumberOfRequiredProperties = 
                requestedFunctionality.inputs().size() +
                requestedFunctionality.outputs().size() +
                requestedFunctionality.effects().size() +
                requestedFunctionality.nonFunctionalProperties().size();
        
        if (null != requestedFunctionality.getOptionalProperties())
        {
            this.attemptedNumberOfOptionalProperties =
                requestedFunctionality.getOptionalProperties().inputs().size() +
                requestedFunctionality.getOptionalProperties().outputs().size() +
                requestedFunctionality.getOptionalProperties().nonFunctionalProperties().size();
        }
        
        if (null != requestedFunctionality.getPreferredProperties())
        {
            this.attemptedNumberOfPreferredProperties =
                requestedFunctionality.getPreferredProperties().inputs().size() +
                requestedFunctionality.getPreferredProperties().outputs().size() +
                requestedFunctionality.getPreferredProperties().nonFunctionalProperties().size();
        }
        
    }

    @Override
    public void inputRequiredIOPE(int distanceTo)
    {
        this.resultingNumberOfRequiredProperties++;
        this.resultingValueForRequiredProperties += MATCH - distanceTo;
    }

    @Override
    public void inputRequiredNFP(int distanceTo)
    {
        this.resultingNumberOfRequiredProperties++;
        this.resultingValueForRequiredProperties += MATCH - distanceTo;
    }

    @Override
    public void inputOptionalIOPE(int distanceTo)
    {
        this.resultingNumberOfOptionalProperties++;
        this.resultingValueForOptionalProperties += MATCH - distanceTo;
    }

    @Override
    public void inputOptionalNFP(int distanceTo)
    {
        this.resultingNumberOfOptionalProperties++;
        this.resultingValueForOptionalProperties += MATCH - distanceTo;
    }

    @Override
    public void inputPreferredIOPE(int distanceTo)
    {
        this.resultingNumberOfPreferredProperties++;
        this.resultingValueForPreferredProperties += MATCH - distanceTo;
    }

    @Override
    public void inputPreferredNFP(int distanceTo)
    {
        this.resultingNumberOfPreferredProperties++;
        this.resultingValueForPreferredProperties += MATCH - distanceTo;
    }

    @Override
    public IFunctionalitySearchEvaluation getComputedEvaluation()
    {
        boolean requiredPropertiesPresent = 
                (this.attemptedNumberOfRequiredProperties == this.resultingNumberOfRequiredProperties);
        
        int value =
                3 * this.resultingValueForRequiredProperties +
                2 * this.resultingValueForPreferredProperties +
                1 * this.resultingValueForOptionalProperties;
        
        return new BasicFunctionalitySearchEvaluation(value, requiredPropertiesPresent);
    }

}
