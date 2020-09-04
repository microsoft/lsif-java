/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import org.apache.commons.lang3.StringUtils;

public class PackageInformation extends Vertex {

	public enum PackageManager {
		// Both Gradle and Maven projects will publish with a pom file.
		// So Manager has only two kinds, "jdk" and "maven". "maven" is likely "pom".
		MAVEN("maven"), JDK("jdk");

		private final String manager;

		private PackageManager(String manager) {
			this.manager = manager;
		}

		@Override
		public String toString() {
			return this.manager;
		}
	}

	private String name;

	private String manager;

	private String version;

	private Repo repository;

	public PackageInformation(String id, String name, PackageManager manager, String version, String type, String url) {
		super(id, Vertex.PACKAGEINFORMATION);
		this.name = name;
		this.manager = manager.toString();
		this.version = version;
		if (StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(url)) {
			this.repository = new Repo(type, url);
		}
	}

	public class Repo {

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
