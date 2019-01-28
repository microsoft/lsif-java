/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.managers.ProjectsManager;
import org.eclipse.lsp4j.Location;

public final class JdtlsUtils {

	public final static Location fixLocation(IJavaElement element, Location location, IJavaProject javaProject) {
		if (!javaProject.equals(element.getJavaProject()) && element.getJavaProject().getProject().getName().equals(ProjectsManager.DEFAULT_PROJECT_NAME)) {
			// see issue at: https://github.com/eclipse/eclipse.jdt.ls/issues/842 and
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=541573
			// for jdk classes, jdt will reuse the java model by altering project to share
			// the model between projects
			// so that sometimes the project for `element` is default project and the
			// project is different from the project for `unit`
			// this fix is to replace the project name with non-default ones since default
			// project should be transparent to users.
			if (location.getUri().contains(ProjectsManager.DEFAULT_PROJECT_NAME)) {
				String patched = StringUtils.replaceOnce(location.getUri(), ProjectsManager.DEFAULT_PROJECT_NAME, javaProject.getProject().getName());
				try {
					IClassFile cf = (IClassFile) JavaCore.create(JDTUtils.toURI(patched).getQuery());
					if (cf != null && cf.exists()) {
						location.setUri(patched);
					}
				} catch (Exception ex) {

				}
			}
		}
		return location;
	}

	public final static String normalizeUri(String uri) {
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			if (uri.startsWith("file:///") && uri.length() > 10 && Character.isUpperCase(uri.charAt(8)) && uri.charAt(9) == ':') {
				return "file:///" + Character.toLowerCase(uri.charAt(8)) + uri.substring(9);
			}
		}
		return uri;
	}
}
