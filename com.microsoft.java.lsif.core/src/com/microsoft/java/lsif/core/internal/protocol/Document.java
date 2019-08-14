/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

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
