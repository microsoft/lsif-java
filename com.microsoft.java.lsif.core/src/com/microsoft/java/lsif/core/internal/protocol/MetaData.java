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

import com.microsoft.java.lsif.core.internal.IConstant;

public class MetaData extends Vertex {

	private static final String DEFAULT_ENCODING = "utf-16";

	public String version;

	// URI String
	public String projectRoot;

	public String positionEncoding = DEFAULT_ENCODING;

	public MetaData(String id, String projectRoot) {
		super(id, Vertex.METADATA);
		this.version = IConstant.LSIF_FORMAT_VERSION;
		this.projectRoot = projectRoot;
	}
}
