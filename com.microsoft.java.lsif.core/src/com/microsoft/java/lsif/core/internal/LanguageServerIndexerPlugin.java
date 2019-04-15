/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal;

import java.io.PrintStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class LanguageServerIndexerPlugin implements BundleActivator {

	private static BundleContext context;

	// Language server will redirect the out when starting, save this value for
	// indexer usage.
	private static PrintStream out;

	@Override
	public void start(BundleContext context) throws Exception {
		LanguageServerIndexerPlugin.context = context;

		LanguageServerIndexerPlugin.out = System.out;
		if (context != null) {
			Platform.getLog(LanguageServerIndexerPlugin.context.getBundle()).addLogListener(new ILogListener() {
				@Override
				public void logging(IStatus status, String plugin) {
					LanguageServerIndexerPlugin.out.println(status.getMessage());
					if (status.getException() != null) {
						status.getException().printStackTrace(LanguageServerIndexerPlugin.out);
					}
				}
			});
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

	public static void log(IStatus status) {
		if (context != null) {
			Platform.getLog(LanguageServerIndexerPlugin.context.getBundle()).log(status);
		}
	}

	public static void log(CoreException e) {
		log(e.getStatus());
	}

	public static void logError(String message) {
		if (context != null) {
			log(new Status(IStatus.ERROR, context.getBundle().getSymbolicName(), message));
		}
	}

	public static void logInfo(String message) {
		if (context != null) {
			log(new Status(IStatus.INFO, context.getBundle().getSymbolicName(), message));
		}
	}

	public static void logException(String message, Throwable ex) {
		if (context != null) {
			log(new Status(IStatus.ERROR, context.getBundle().getSymbolicName(), message, ex));
		}
	}

	public static void print(String message) {
		out.print(message);
	}

	public static void println() {
		out.println();
	}

	public static void println(String message) {
		out.println(message);
	}
}