/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */


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
