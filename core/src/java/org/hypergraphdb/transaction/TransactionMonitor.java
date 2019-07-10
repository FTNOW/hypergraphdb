/* 
 * This file is part of the HyperGraphDB source distribution. This is copyrighted 
 * software. For permitted uses, licensing options and redistribution, please see  
 * the LicensingInformation file at the root level of the distribution.  
 * 
 * Copyright (c) 2005-2010 Kobrix Software, Inc.  All rights reserved. 
 */
package org.hypergraphdb.transaction;

import java.util.Set;
import java.util.concurrent.Callable;

/**
 * <p>
 * The transaction monitor controls whether metrics are being collected
 * during transaction execution.
 * </p>
 * <p>
 * It is important to note that once enabled the monitor will collect 
 * metrics until the {@link #disable()} method is called. Every transaction
 * will have a corresponding structure holding info about how long it took, 
 * how many times was it retried. Thus if not disabled, the monitor will
 * eat up all memory eventually. 
 * </p>
 */
public interface TransactionMonitor
{
	/**
	 * Return <code>true</code> is transaction monitoring is currently enabled
	 * and <code>false</code> otherwise.
	 */
	boolean enabled();
	
	/**
	 * Start collecting transaction metrics.
	 * @return <code>this</code>
	 */
	TransactionMonitor enable();

	/**
	 * Start collecting transaction metrics.
	 * @return <code>this</code>
	 */	
	TransactionMonitor disable();
	
	/**
	 * Return info of the current transaction (if any) or <code>null</code>
	 * if not present.
	 */
	<T> TxInfo tx();	
	
	/**
	 * Look up transactions by their attributes. 
	 * 
	 * @param params A sequence of name/value pairs: each event parameter
	 * is expected to be a string and each odd parameter can be any value.
	 * @return
	 */
	Set<TxInfo> lookup(Object... params);
	
	/**
	 * Perform a transaction while monitoring regardless of whether
	 * the enabled flag is set or not.
	 * 
	 * @param name
	 * @param transaction
	 * @param config
	 * @return
	 */
	<V> V transact(String name, Callable<V> transaction, HGTransactionConfig config);
}
