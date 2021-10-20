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


package com.microsoft.java.lsif.core.internal.emitter;

import org.eclipse.lsp4j.adapters.HoverTypeAdapter;
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapter;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapter;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonParser {
	private static final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new CollectionTypeAdapter.Factory())
			.registerTypeAdapterFactory(new EnumTypeAdapter.Factory())
			.registerTypeAdapterFactory(new HoverTypeAdapter.Factory())
			.registerTypeAdapterFactory(new EitherTypeAdapter.Factory()).create();

	public static String toJson(Object element) {
		return gson.toJson(element);
	}
}
