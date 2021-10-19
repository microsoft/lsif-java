/*******************************************************************************
* Copyright (c) 2021 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package com.microsoft.java.lsif.core.internal;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import com.microsoft.java.lsif.core.internal.indexer.Indexer;

public class LanguageServerIndexer implements IApplication {

	@Override
	public Object start(IApplicationContext context) {
		try {
			Indexer indexer = new Indexer();
			indexer.generateLsif();

		} catch (Exception ex) {
			LanguageServerIndexerPlugin.logException("Exception when indexing ", ex);
			System.exit(1);
		}

		return null;
	}

	@Override
	public void stop() {
	}
}
