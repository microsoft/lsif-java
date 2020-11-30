/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

public class Vertex extends Element {

	public static final String METADATA = "metaData";

	public static final String EVENT = "$event";

	public static final String PROJECT = "project";

	public static final String RANGE = "range";

	public static final String LOCATION = "location";

	public static final String DOCUMENT = "document";

	public static final String EXTERNALIMPORTITEM = "externalImportItem";

	public static final String EXPORTITEM = "exportItem";

	public static final String RESULTSET = "resultSet";

	public static final String DOCUMENTSYMBOLRESULT = "documentSymbolResult";

	public static final String FOLDINGRANGERESULT = "foldingRangeResult";

	public static final String DOCUMENTLINKRESULT = "documentLinkResult";

	public static final String DIAGNOSTICRESULT = "diagnosticResult";

	public static final String DECLARATIONRESULT = "declarationResult";

	public static final String DEFINITIONRESULT = "definitionResult";

	public static final String TYPEDEFINITIONRESULT = "typeDefinitionResult";

	public static final String HOVERRESULT = "hoverResult";

	public static final String REFERENCERESULT = "referenceResult";

	public static final String IMPLEMENTATIONRESULT = "implementationResult";

	public static final String GROUP = "group";

	public static final String MONIKER = "moniker";

	public static final String PACKAGEINFORMATION = "packageInformation";

	public Vertex(String id, String label) {
		super(id, Element.VERTEX, label);
	}
}
