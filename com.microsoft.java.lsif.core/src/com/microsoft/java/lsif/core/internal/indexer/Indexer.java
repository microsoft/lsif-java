/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import org.apache.maven.scm.provider.ScmUrlUtils;
import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.GradleCore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ls.core.internal.BuildWorkspaceStatus;
import org.eclipse.jdt.ls.core.internal.IProjectImporter;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.managers.EclipseProjectImporter;
import org.eclipse.jdt.ls.core.internal.managers.GradleProjectImporter;
import org.eclipse.jdt.ls.core.internal.managers.MavenProjectImporter;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.project.LocalProjectScanner;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.gradle.tooling.model.GradleModuleVersion;
import org.gradle.tooling.model.gradle.GradlePublication;
import org.gradle.tooling.model.gradle.ProjectPublications;

import com.microsoft.java.lsif.core.internal.emitter.LsifEmitter;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Event;
import com.microsoft.java.lsif.core.internal.protocol.Project;
import com.microsoft.java.lsif.core.internal.protocol.PackageInformation.PackageManager;
import com.microsoft.java.lsif.core.internal.visitors.DiagnosticVisitor;
import com.microsoft.java.lsif.core.internal.visitors.DocumentVisitor;
import com.microsoft.java.lsif.core.internal.visitors.LsifVisitor;
import com.microsoft.java.lsif.core.internal.visitors.VisitorUtils;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class Indexer {

	private WorkspaceHandler handler;

	public enum ProjectBuildTool {
		MAVEN, GRADLE, ECLIPSE, INVISIBLE;
	}

	public Indexer() {
		String repoPath = System.getProperty("repo.path");
		if (repoPath == null) {
			repoPath = System.getProperty("user.dir");
		}
		this.handler = new WorkspaceHandler(repoPath);
	}

	public void generateLsif() throws JavaModelException {
		NullProgressMonitor monitor = new NullProgressMonitor();
		IPath path = this.handler.initialize();
		initializeJdtls();
		LsifService lsif = new LsifService();

		LsifEmitter.getInstance().start();
		LsifEmitter.getInstance().emit(lsif.getVertexBuilder().metaData(ResourceUtils.fixURI(path.toFile().toURI())));

		handler.importProject(path, monitor);
		BuildWorkspaceStatus buildStatus = handler.buildProject(monitor);
		if (buildStatus != BuildWorkspaceStatus.SUCCEED) {
			return;

		}
		buildIndex(path, monitor, lsif);
		handler.removeProject(monitor);

		LsifEmitter.getInstance().end();
	}

	private void buildIndex(IPath path, IProgressMonitor monitor, LsifService lsif) throws JavaModelException {

		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (IProject proj : projects) {
			if (proj == null) {
				return;
			}
			IJavaProject javaProject = JavaCore.create(proj);
			if (!javaProject.exists()) {
				continue;
			}
			ProjectBuildTool builder = null;
			try {
				IPath folderPath = proj.getLocation();
				if (folderPath == null) {
					continue;
				}
				IProjectImporter importer = this.handler.getImporter(folderPath.toFile(), monitor);
				if (importer instanceof MavenProjectImporter) {
					builder = ProjectBuildTool.MAVEN;
				} else if (importer instanceof GradleProjectImporter) {
					builder = ProjectBuildTool.GRADLE;
				} else if (importer instanceof EclipseProjectImporter) {
					builder = ProjectBuildTool.ECLIPSE;
				} else {
					builder = ProjectBuildTool.INVISIBLE;
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
			boolean isPublish = false;
			if (builder == ProjectBuildTool.MAVEN) {
				Set<MavenProjectInfo> infoSet = collectMavenProjectInfo(monitor, path);
				for (MavenProjectInfo mavenProjectInfo : infoSet) {
					Model model = mavenProjectInfo.getModel();
					String groupId = model.getGroupId();
					String artifactId = model.getArtifactId();
					String version = model.getVersion();
					Scm scm = model.getScm();
					String url = "";
					String type = "";
					if (scm != null) {
						url = scm.getUrl();
						type = ScmUrlUtils.getProvider(scm.getConnection());
					}
					if (!groupId.equals("") && !artifactId.equals("") && !version.equals("")) {
						isPublish = true;
						Repository.getInstance().enlistPackageInformation(lsif, javaProject.getPath().toString(), groupId + "/" + artifactId, PackageManager.MAVEN, version, type, url);
					}
				}
			} else if (builder == ProjectBuildTool.GRADLE) {
				GradleBuild build = GradleCore.getWorkspace().getBuild(proj).get();
				ProjectPublications model;
				try {
					model = build.withConnection(connection -> connection.getModel(ProjectPublications.class), monitor);
					List<? extends GradlePublication> publications = model.getPublications().getAll();
					if (publications.size() > 0) {
						GradleModuleVersion gradleModuleVersion = publications.get(0).getId();
						String groupId = gradleModuleVersion.getGroup();
						String artifactId = gradleModuleVersion.getName();
						String version = gradleModuleVersion.getVersion();
						isPublish = true;
						Repository.getInstance().enlistPackageInformation(lsif, javaProject.getPath().toString(), groupId + "/" + artifactId, PackageManager.GRADLE, version);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Project projVertex = lsif.getVertexBuilder().project();
			LsifEmitter.getInstance().emit(projVertex);
			LsifEmitter.getInstance().emit(
					lsif.getVertexBuilder().event(Event.EventScope.Project, Event.EventKind.BEGIN, projVertex.getId()));

			List<ICompilationUnit> sourceList = getAllSourceFiles(javaProject);

			dumpParallely(sourceList, threadPool, projVertex, lsif, builder, isPublish, monitor);

			VisitorUtils.endAllDocument(lsif);
			LsifEmitter.getInstance().emit(
					lsif.getVertexBuilder().event(Event.EventScope.Project, Event.EventKind.END, projVertex.getId()));
		}

		threadPool.shutdown();
	}

	private void initializeJdtls() {
		Map<String, Object> extendedClientCapabilities = new HashMap<>();
		extendedClientCapabilities.put("classFileContentsSupport", false);
		JavaLanguageServerPlugin.getPreferencesManager().updateClientPrefences(new ClientCapabilities(),
				extendedClientCapabilities);
	}

	private List<ICompilationUnit> getAllSourceFiles(IJavaProject javaProject) throws JavaModelException {
		List<ICompilationUnit> res = new LinkedList<>();
		IClasspathEntry[] references = javaProject.getRawClasspath();
		for (IClasspathEntry reference : references) {
			if (reference.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				IPackageFragmentRoot[] fragmentRoots = javaProject.findPackageFragmentRoots(reference);
				for (IPackageFragmentRoot fragmentRoot : fragmentRoots) {
					for (IJavaElement child : fragmentRoot.getChildren()) {
						IPackageFragment fragment = (IPackageFragment) child;
						if (fragment.hasChildren()) {
							for (IJavaElement sourceFile : fragment.getChildren()) {
								if (sourceFile.exists() && sourceFile instanceof ICompilationUnit) {
									res.add((ICompilationUnit) sourceFile);
								}
							}
						}
					}
				}
			}
		}
		return res;
	}

	private void dumpParallely(List<ICompilationUnit> sourceList, ExecutorService threadPool, Project projVertex,
			LsifService lsif, ProjectBuildTool builder, boolean isPublish, IProgressMonitor monitor) {
		Observable.fromIterable(sourceList)
				.flatMap(item -> Observable.just(item).observeOn(Schedulers.from(threadPool)).map(sourceFile -> {
					CompilationUnit cu = ASTUtil.createAST(sourceFile, monitor);
					Document docVertex = (new DocumentVisitor(lsif, projVertex)).enlist(sourceFile);
					if (cu == null || docVertex == null) {
						return 0;
					}
					IndexerContext context = new IndexerContext(docVertex, cu, projVertex);

					LsifVisitor lsifVisitor = new LsifVisitor(lsif, context, builder, isPublish);
					cu.accept(lsifVisitor);

					DiagnosticVisitor diagnosticVisitor = new DiagnosticVisitor(lsif, context);
					diagnosticVisitor.enlist();

					VisitorUtils.endDocument(lsif, docVertex);

					return 0;
				})).blockingSubscribe();
	}

	private Set<MavenProjectInfo> collectMavenProjectInfo(IProgressMonitor monitor, IPath path)
			throws OperationCanceledException {
		MavenModelManager modelManager = MavenPlugin.getMavenModelManager();
		return getMavenProjects(path.toFile(), modelManager, monitor);
	}

	private Set<MavenProjectInfo> getMavenProjects(File directory, MavenModelManager modelManager,
			IProgressMonitor monitor) throws OperationCanceledException {
		if (directory == null) {
			return Collections.emptySet();
		}
		try {
			LocalProjectScanner scanner = new LocalProjectScanner(directory.getParentFile(), directory.toString(),
					false, modelManager);
			scanner.run(monitor);
			return collectProjects(scanner.getProjects());
		} catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
	}

	private Set<MavenProjectInfo> collectProjects(Collection<MavenProjectInfo> projects) {
		return new LinkedHashSet<MavenProjectInfo>() {
			private static final long serialVersionUID = 1L;

			public Set<MavenProjectInfo> collectProjects(Collection<MavenProjectInfo> projects) {
				for (MavenProjectInfo projectInfo : projects) {
					add(projectInfo);
					collectProjects(projectInfo.getProjects());
				}
				return this;
			}
		}.collectProjects(projects);
	}
}
