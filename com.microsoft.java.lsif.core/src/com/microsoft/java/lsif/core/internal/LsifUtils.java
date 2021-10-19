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

package com.microsoft.java.lsif.core.internal;

import java.util.Base64;

import org.eclipse.core.runtime.Platform;

public class LsifUtils {

	private LsifUtils() {
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

	public static String encodeToBase64(String input) {
		return Base64.getEncoder().withoutPadding().encodeToString(input.getBytes());
	}
}
