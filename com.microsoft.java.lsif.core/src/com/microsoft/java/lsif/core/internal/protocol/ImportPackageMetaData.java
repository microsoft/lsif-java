/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

public class ImportPackageMetaData {

	public String packageName;
	public String version;
	public String type;
	public String url;

	public ImportPackageMetaData() {
		this.packageName = "";
		this.version = "";
		this.type = "";
		this.url = "";
	}
}
