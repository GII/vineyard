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

package com.hi3project.vineyard.registry.server;

import com.hi3project.broccoli.bsdl.api.IAxiom;
import com.hi3project.broccoli.bsdl.api.ISemanticIdentifier;
import com.hi3project.broccoli.bsdl.api.registry.IBSDLRegistry;
import com.hi3project.broccoli.bsdf.api.discovery.IMatchmaker;
import com.hi3project.broccoli.bsdl.impl.Concept;
import com.hi3project.broccoli.bsdl.impl.Instance;
import com.hi3project.broccoli.bsdl.impl.parsing.ReferenceToSemanticAxiom;
import com.hi3project.broccoli.bsdl.impl.exceptions.SemanticModelException;
import com.hi3project.broccoli.io.BSDFLogger;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Implementation of IMatchmaker that handles Concept and Instance
 * implementations
 * <p>
 * TODO: subsumes
 *
 *
 * 
 */
public class SimpleStructuralMatchmaker implements IMatchmaker
{

    IBSDLRegistry bsdlRegistry = null;

    public SimpleStructuralMatchmaker(IBSDLRegistry bsdlRegistry)
    {
        this.bsdlRegistry = bsdlRegistry;
        BSDFLogger.getLogger().debug("Instances a SimpleStructuralMatchmaker");
    }

    @Override
    public boolean same(IAxiom axiom1, IAxiom axiom2)
    {
        if (null == axiom1 || null == axiom2)
        {
            return false;
        }
        if (axiom1 instanceof Concept && axiom2 instanceof Concept)
        {
            return ((Concept) axiom1).getSemanticIdentifier().equals(((Concept) axiom2).getSemanticIdentifier());
        }
        if (axiom1 instanceof Instance && axiom2 instanceof Instance)
        {
            return ((Instance) axiom1).getConcept().getSemanticIdentifier().equals(((Instance) axiom1).getConcept().getSemanticIdentifier());
        }
        return false;
    }

    @Override
    public boolean same(ISemanticIdentifier identifier1, ISemanticIdentifier identifier2) throws SemanticModelException
    {
        return identifier1.equals(identifier2)
                || same(bsdlRegistry.axiomFor(identifier1), bsdlRegistry.axiomFor(identifier2));
    }

    @Override
    public int subsumes(IAxiom axiomThatSubsumes, IAxiom axiomSubsumed) throws SemanticModelException
    {
        if (axiomThatSubsumes instanceof Concept && axiomSubsumed instanceof Concept)
        {
            Concept aS = (Concept) axiomSubsumed;
            return recurSubsumes(axiomThatSubsumes, aS, 0);
        }
        return -1;
    }

    public int recurSubsumes(IAxiom axiomThatSubsumes, Concept conceptForAxiomSubsumed, int accumulatedDistance) throws SemanticModelException
    {
        if (same(axiomThatSubsumes, conceptForAxiomSubsumed))
        {
            return accumulatedDistance;
        } else
        {
            // recur   
            List<Integer> distances = new ArrayList<Integer>();
            for (ReferenceToSemanticAxiom<Concept> ref : conceptForAxiomSubsumed.superconcepts())
            {
                int recurDistance = recurSubsumes(axiomThatSubsumes, ref.semanticAxiom(), accumulatedDistance + 1);
                if (recurDistance >= 0)
                {
                    distances.add(recurDistance);
                }
            }

            if (!distances.isEmpty())
            {
                // max of distances            
                int returnDistance = Integer.MAX_VALUE;
                for (Integer distance : distances)
                {
                    returnDistance = Math.min(returnDistance, distance);
                }

                return returnDistance;
            } else
            {
                // not a single case of subsumed axiom
                return -1;
            }
        }
    }

    /**
     *
     * @param identifierThatSubsumes
     * @param identifierSubsumed
     * @return
     * @throws SemanticModelException
     */
    @Override
    public int subsumes(ISemanticIdentifier identifierThatSubsumes, ISemanticIdentifier identifierSubsumed) throws SemanticModelException
    {
        return subsumes(bsdlRegistry.axiomFor(identifierThatSubsumes), bsdlRegistry.axiomFor(identifierSubsumed));
    }

}
