/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.ls.core.internal.BuildWorkspaceStatus;
import org.eclipse.jdt.ls.core.internal.IConstants;
import org.eclipse.jdt.ls.core.internal.IProjectImporter;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.managers.GradleBuildSupport;

import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;

public class WorkspaceHandler {

	private String workspaceFolder;

	public WorkspaceHandler(String workspaceFolder) {
		this.workspaceFolder = workspaceFolder;
	}

	public List<IPath> initialize() {
		NullProgressMonitor monitor = new NullProgressMonitor();
		List<IPath> projectRoots = new ArrayList<>();
		LanguageServerIndexerPlugin.logInfo("Collecting repo information");
		if (workspaceFolder != null) {
			File projectDir = new File(workspaceFolder);
			if (projectDir.isDirectory()) {
				collectionProjects(projectDir, projectRoots);
			}
		}

		try {
			deleteInvalidProjects(monitor);
			GradleBuildSupport.cleanGradleModels(monitor);
		} catch (OperationCanceledException e) {
		}

		return projectRoots;
	}

	public void importProject(IPath path, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		File rootFolder = path.toFile();
		IProjectImporter importer;
		try {
			importer = getImporter(rootFolder, subMonitor.split(30));
			if (importer != null) {
				importer.importToWorkspace(subMonitor.split(70));
			}
		} catch (OperationCanceledException | CoreException e) {
			e.printStackTrace();
		}
	}

	public BuildWorkspaceStatus buildProject(IProgressMonitor monitor) {
		try {
			ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
			// ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE,
			// monitor);
			if (monitor.isCanceled()) {
				return BuildWorkspaceStatus.CANCELLED;
			}
			return BuildWorkspaceStatus.SUCCEED;
		} catch (CoreException e) {
			LanguageServerIndexerPlugin.logException("Failed to build workspace.", e);
			return BuildWorkspaceStatus.FAILED;
		} catch (OperationCanceledException e) {
			return BuildWorkspaceStatus.CANCELLED;
		}
	}

	public IProjectImporter getImporter(File rootFolder, IProgressMonitor monitor)
			throws OperationCanceledException, CoreException {
		Collection<IProjectImporter> importers = importers();
		SubMonitor subMonitor = SubMonitor.convert(monitor, importers.size());
		for (IProjectImporter importer : importers) {
			importer.initialize(rootFolder);
			if (importer.applies(subMonitor.split(1))) {
				return importer;
			}
		}
		return null;
	}

	private Collection<IProjectImporter> importers() {
		Map<Integer, IProjectImporter> importers = new TreeMap<>();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(IConstants.PLUGIN_ID,
				"importers");
		IConfigurationElement[] configs = extensionPoint.getConfigurationElements();
		for (int i = 0; i < configs.length; i++) {
			try {
				Integer order = Integer.valueOf(configs[i].getAttribute("order"));
				importers.put(order, (IProjectImporter) configs[i].createExecutableExtension("class")); //$NON-NLS-1$
			} catch (CoreException e) {
				JavaLanguageServerPlugin.log(e.getStatus());
			}
		}
		return importers.values();
	}

	private static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	private void deleteInvalidProjects(IProgressMonitor monitor) {
		for (IProject project : getWorkspaceRoot().getProjects()) {
			try {
				project.delete(false, true, monitor);
			} catch (CoreException e1) {
				JavaLanguageServerPlugin.logException(e1.getMessage(), e1);
			}
		}
	}

	private void collectionProjects(File f, List<IPath> result) {
		if (f.isDirectory() && ((new File(f.getAbsolutePath(), "pom.xml")).exists()
				|| (new File(f.getAbsolutePath(), "build.gradle")).exists())) {
			result.add(ResourceUtils.filePathFromURI(f.toURI().toString()));
		}
	}

	public void removeProject(IProgressMonitor monitor) {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		try {
			for (IProject proj : projects) {
				proj.getProject().delete(false, true, monitor);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}