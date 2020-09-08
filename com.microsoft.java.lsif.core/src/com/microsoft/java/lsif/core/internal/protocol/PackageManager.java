/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

public enum PackageManager {
	// Both Gradle and Maven projects will publish with a pom file.
	// So Manager has only two kinds, "jdk" and "maven". "maven" is likely "pom".
	MAVEN("maven"), JDK("jdk"), NULL("");

	private final String manager;

	private PackageManager(String manager) {
		this.manager = manager;
	}

	@Override
	public String toString() {
		return this.manager;
	}
}
