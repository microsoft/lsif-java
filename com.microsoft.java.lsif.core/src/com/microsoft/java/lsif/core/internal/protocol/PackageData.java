/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */


package com.microsoft.java.lsif.core.internal.protocol;

public class PackageData {

	private String groupId;

	private String artifactId;

	private String version;

	private PackageInformation packageInformation;

	public PackageData(String groupId, String artifactId, String version) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public String getArtifactId() {
		return this.artifactId;
	}

	public String getVersion() {
		return this.version;
	}

	public void setPackageInformation(PackageInformation packageInformation) {
		this.packageInformation = packageInformation;
	}

	public PackageInformation getPackageInformation() {
		return this.packageInformation;
	}
}
