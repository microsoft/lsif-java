/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JDTUtils.LocationType;
import org.eclipse.jdt.ls.core.internal.managers.ProjectsManager;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;

public final class JdtlsUtils {

	private JdtlsUtils() {
	}

	public final static Location getElementLocation(IJavaElement element) {
		Location targetLocation = null;
		try {
			ICompilationUnit compilationUnit = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
			IClassFile cf = (IClassFile) element.getAncestor(IJavaElement.CLASS_FILE);
			if (compilationUnit != null || (cf != null && cf.getSourceRange() != null)) {
				targetLocation = JdtlsUtils.fixLocation(element, toLocation(element),
						element.getJavaProject());
			} else if (element instanceof IMember && ((IMember) element).getClassFile() != null) {
				targetLocation = JdtlsUtils.fixLocation(element,
						toLocation(((IMember) element).getClassFile(), 0, 0), element.getJavaProject());
			}
		} catch (CoreException ex) {
		}

		return targetLocation;
	}

	public final static Location fixLocation(IJavaElement element, Location location, IJavaProject javaProject) {
		if (!javaProject.equals(element.getJavaProject())
				&& element.getJavaProject().getProject().getName().equals(ProjectsManager.DEFAULT_PROJECT_NAME)) {
			// see issue at: https://github.com/eclipse/eclipse.jdt.ls/issues/842 and
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=541573
			// for jdk classes, jdt will reuse the java model by altering project to share
			// the model between projects
			// so that sometimes the project for `element` is default project and the
			// project is different from the project for `unit`
			// this fix is to replace the project name with non-default ones since default
			// project should be transparent to users.
			if (location.getUri().contains(ProjectsManager.DEFAULT_PROJECT_NAME)) {
				String patched = StringUtils.replaceOnce(location.getUri(), ProjectsManager.DEFAULT_PROJECT_NAME,
						javaProject.getProject().getName());
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

	/**
	 * Creates a location for a given java element. Element can be a
	 * {@link ICompilationUnit} or {@link IClassFile}
	 *
	 * @param element
	 * @return location or null
	 * @throws JavaModelException
	 */
	public static Location toLocation(IJavaElement element) throws JavaModelException {
		return toLocation(element, LocationType.NAME_RANGE);
	}

	/**
	 * Creates a location for a given java element. Unlike {@link #toLocation} this
	 * method can be called to return with a range that contains surrounding
	 * comments (method body), not just the name of the Java element. Element can be
	 * a {@link ICompilationUnit} or {@link IClassFile}
	 *
	 * @param element
	 * @param type    the range type. The {@link LocationType#NAME_RANGE name} or
	 *                {@link LocationType#FULL_RANGE full} range.
	 * @return location or null
	 * @throws JavaModelException
	 */
	public static Location toLocation(IJavaElement element, LocationType type) throws JavaModelException {
		ICompilationUnit unit = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
		IClassFile cf = (IClassFile) element.getAncestor(IJavaElement.CLASS_FILE);
		if (unit == null && cf == null) {
			return null;
		}
		if (element instanceof ISourceReference) {
			ISourceRange nameRange = getNameRange(element);
			if (SourceRange.isAvailable(nameRange)) {
				if (cf == null) {
					return JDTUtils.toLocation(unit, nameRange.getOffset(), nameRange.getLength());
				} else {
					return toLocation(cf, nameRange.getOffset(), nameRange.getLength());
				}
			}
		}
		return null;
	}

	/**
	 * Creates location to the given offset and length for the class file.
	 *
	 * @param unit
	 * @param offset
	 * @param length
	 * @return location
	 * @throws JavaModelException
	 */
	public static Location toLocation(IClassFile classFile, int offset, int length) throws JavaModelException {
		String uriString = toUri(classFile);
		if (uriString != null) {
			Range range = JDTUtils.toRange(classFile, offset, length);
			return new Location(uriString, range);
		}
		return null;
	}

	public static ISourceRange getNameRange(IJavaElement element) throws JavaModelException {
		ISourceRange nameRange = null;
		if (element instanceof IMember) {
			IMember member = (IMember) element;
			nameRange = member.getNameRange();
			if ((!SourceRange.isAvailable(nameRange))) {
				nameRange = member.getSourceRange();
			}
		} else if (element instanceof ITypeParameter || element instanceof ILocalVariable) {
			nameRange = ((ISourceReference) element).getNameRange();
		} else if (element instanceof ISourceReference) {
			nameRange = ((ISourceReference) element).getSourceRange();
		}
		if (!SourceRange.isAvailable(nameRange) && element.getParent() != null) {
			nameRange = getNameRange(element.getParent());
		}
		return nameRange;
	}

	/**
	 * Normalize the URI to the same format as the client.
	 */
	public final static String normalizeUri(String uri) {
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			if (uri.startsWith("file:///") && uri.length() > 10 && Character.isUpperCase(uri.charAt(8))
					&& uri.charAt(9) == ':') {
				return "file:///" + Character.toLowerCase(uri.charAt(8)) + uri.substring(9);
			}
		}
		return uri;
	}

	public static String toUri(IClassFile classFile) {
		String packageName = classFile.getParent().getElementName();
		String jarName = classFile.getParent().getParent().getElementName();
		String uriString = null;
		try {
			uriString = new URI(
					"jdt", "contents", JDTUtils.PATH_SEPARATOR + jarName + JDTUtils.PATH_SEPARATOR + packageName
							+ JDTUtils.PATH_SEPARATOR + classFile.getElementName(),
					classFile.getHandleIdentifier(), null).toASCIIString();
		} catch (URISyntaxException e) {
			LanguageServerIndexerPlugin.logException("Error generating URI for class ", e);
		}
		return uriString;
	}

}
