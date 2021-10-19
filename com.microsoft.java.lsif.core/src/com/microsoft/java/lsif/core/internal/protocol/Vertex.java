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
