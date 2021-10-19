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

import java.io.PrintStream;

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
	private static PrintStream err;

	@Override
	public void start(BundleContext context) throws Exception {
		LanguageServerIndexerPlugin.context = context;

		LanguageServerIndexerPlugin.out = System.out;
		LanguageServerIndexerPlugin.err = System.err;
		if (context != null) {
			Platform.getLog(LanguageServerIndexerPlugin.context.getBundle()).addLogListener(new ILogListener() {
				@Override
				public void logging(IStatus status, String plugin) {
					if (status.getSeverity() == IStatus.ERROR) {
						LanguageServerIndexerPlugin.err.println(status.getMessage());
						if (status.getException() != null) {
							status.getException().printStackTrace(LanguageServerIndexerPlugin.err);
						}
					} else {
						// Make sure the log will ruin the emitter output line.
						LanguageServerIndexerPlugin.out.println();
						LanguageServerIndexerPlugin.out.println(status.getMessage());
					}
				}
			});
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
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

	private static void log(IStatus status) {
		if (context != null) {
			Platform.getLog(LanguageServerIndexerPlugin.context.getBundle()).log(status);
		}
	}
}