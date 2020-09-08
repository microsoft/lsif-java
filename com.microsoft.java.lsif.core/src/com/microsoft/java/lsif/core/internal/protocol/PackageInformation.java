/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import org.apache.commons.lang3.StringUtils;

public class PackageInformation extends Vertex {

	private String name;

	private String manager;

	private String version;

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

		private String type;

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
