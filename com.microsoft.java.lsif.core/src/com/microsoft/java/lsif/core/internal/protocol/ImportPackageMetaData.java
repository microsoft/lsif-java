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
