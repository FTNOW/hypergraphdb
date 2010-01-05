/* 
 * This file is part of the HyperGraphDB source distribution. This is copyrighted 
 * software. For permitted uses, licensing options and redistribution, please see  
 * the LicensingInformation file at the root level of the distribution.  
 * 
 * Copyright (c) 2005-2010 Kobrix Software, Inc.  All rights reserved. 
 */
package org.hypergraphdb.peer.bootstrap;

import java.util.Map;

import org.hypergraphdb.peer.BootstrapPeer;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.cact.DefineAtom;
import org.hypergraphdb.peer.cact.GetClassForType;
import org.hypergraphdb.peer.cact.TransferGraph;

public class CACTBootstrap implements BootstrapPeer
{
    public void bootstrap(HyperGraphPeer peer, Map<String, Object> config)
    {
        peer.getActivityManager().registerActivityType(GetClassForType.TYPENAME, GetClassForType.class);
        peer.getActivityManager().registerActivityType(DefineAtom.TYPENAME, DefineAtom.class);
        peer.getActivityManager().registerActivityType(TransferGraph.TYPENAME, TransferGraph.class);
    }
}
