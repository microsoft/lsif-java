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

import org.apache.commons.lang3.StringUtils;

public class PackageInformation extends Vertex {

	private String name;

	@SuppressWarnings("unused")
	private String manager;

	@SuppressWarnings("unused")
	private String version;

	@SuppressWarnings("unused")
	private Repo repository;

	/**
	 * A Constant representing a maven manager. Since most java repositories use pom
	 * as their publish format, "maven" is used as the packageInformation's manager
	 * regardless of build tools.
	 */
	public static final String MAVEN = "maven";

	/**
	 * A Constant representing a jdk manager. The manager of a packageInformation
	 * imported from JDK library consists of "jdk" and its Implementation-Vendor.
	 * For example, <code>jdk(Oracle Corporation)</code>.
	 */
	public static final String JDK = "jdk";

	public PackageInformation(String id, String name, String manager, String version, String type, String url) {
		super(id, Vertex.PACKAGEINFORMATION);
		this.name = name;
		this.manager = manager;
		this.version = version;
		if (StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(url)) {
			this.repository = new Repo(type, url);
		}
	}

	private class Repo {

		@SuppressWarnings("unused")
		private String type;

		@SuppressWarnings("unused")
		private String url;

		Repo(String type, String url) {
			this.type = type;
			this.url = url;
		}
	}

	public String getName() {
		return this.name;
	}
}
