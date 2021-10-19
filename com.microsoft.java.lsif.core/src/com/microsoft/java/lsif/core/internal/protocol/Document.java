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

public class Document extends Vertex {

	private String uri;

	private String languageId;

	private String contents;

	public Document(String id, String uri) {
		super(id, Vertex.DOCUMENT);
		this.uri = uri;
		this.languageId = IConstant.JAVA_ID;
	}

	public String getUri() {
		return this.uri;
	}

	public String getLanguageId() {
		return this.languageId;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}
}
