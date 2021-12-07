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

package com.microsoft.java.lsif.core.internal.indexer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.provider.ScmUrlUtils;
import org.apache.maven.shared.utils.StringUtils;
import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.GradleCore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.jdt.ls.core.internal.managers.GradleProjectImporter;
import org.eclipse.jdt.ls.core.internal.managers.MavenProjectImporter;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.DocumentSymbolCapabilities;
import org.eclipse.lsp4j.SymbolKindCapabilities;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.gradle.tooling.model.GradleModuleVersion;
import org.gradle.tooling.model.gradle.GradlePublication;
import org.gradle.tooling.model.gradle.ProjectPublications;

import com.microsoft.java.lsif.core.internal.emitter.LsifEmitter;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Event;
import com.microsoft.java.lsif.core.internal.protocol.Group;
import com.microsoft.java.lsif.core.internal.protocol.ItemEdge;
import com.microsoft.java.lsif.core.internal.protocol.PackageInformation;
import com.microsoft.java.lsif.core.internal.protocol.Project;
import com.microsoft.java.lsif.core.internal.protocol.Group.ConflictResolution;
import com.microsoft.java.lsif.core.internal.visitors.DiagnosticVisitor;
import com.microsoft.java.lsif.core.internal.visitors.DocumentVisitor;
import com.microsoft.java.lsif.core.internal.visitors.LsifVisitor;
import com.microsoft.java.lsif.core.internal.visitors.SymbolData;
import com.microsoft.java.lsif.core.internal.visitors.VisitorUtils;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class Indexer {

	private WorkspaceHandler handler;

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

		Group groupVertex = lsif.getVertexBuilder().group(ResourceUtils.fixURI(path.toFile().toURI()),
				ConflictResolution.TAKEDB, path.lastSegment(), ResourceUtils.fixURI(path.toFile().toURI()));
		LsifEmitter.getInstance().emit(groupVertex);
		LsifEmitter.getInstance().emit(
				lsif.getVertexBuilder().event(Event.EventScope.Group, Event.EventKind.BEGIN, groupVertex.getId()));

		for (IProject proj : projects) {
			if (proj == null) {
				return;
			}
			IJavaProject javaProject = JavaCore.create(proj);
			if (!javaProject.exists()) {
				continue;
			}
			boolean hasPackageInformation = false;
			try {
				hasPackageInformation = generateExportPackageInformation(proj, monitor, lsif, javaProject);
			} catch (Exception e) {
				// OperationCanceledException, CoreException from WorkspaceHandler.getImporter
				// GradleConnectionException, IllegalStateException from
				// ProjectConnection.getModel
				JavaLanguageServerPlugin.logException(e.getMessage(), e);
			}

			Project projVertex = lsif.getVertexBuilder().project(javaProject.getElementName());
			LsifEmitter.getInstance().emit(projVertex);
			LsifEmitter.getInstance().emit(
					lsif.getVertexBuilder().event(Event.EventScope.Project, Event.EventKind.BEGIN, projVertex.getId()));

			LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().belongsTo(projVertex, groupVertex));
			List<ICompilationUnit> sourceList = getAllSourceFiles(javaProject);

			dumpParallelly(sourceList, threadPool, projVertex, lsif, hasPackageInformation, monitor);

			emitReferenceEdges(lsif);
			VisitorUtils.endAllDocument(lsif);
			LsifEmitter.getInstance().emit(
					lsif.getVertexBuilder().event(Event.EventScope.Project, Event.EventKind.END, projVertex.getId()));
		}

		LsifEmitter.getInstance()
				.emit(lsif.getVertexBuilder().event(Event.EventScope.Group, Event.EventKind.END, groupVertex.getId()));

		threadPool.shutdown();
	}

	private void initializeJdtls() {
		Map<String, Object> extendedClientCapabilities = new HashMap<>();
		extendedClientCapabilities.put("classFileContentsSupport", false);
		DocumentSymbolCapabilities documentSymbol = new DocumentSymbolCapabilities(new SymbolKindCapabilities(),
			/** dynamicRegistration */ false, /** hierarchicalDocumentSymbolSupport */ true);
		TextDocumentClientCapabilities textDocument = new TextDocumentClientCapabilities();
		textDocument.setDocumentSymbol(documentSymbol);
		ClientCapabilities clientCapabilities = new ClientCapabilities();
		clientCapabilities.setTextDocument(textDocument);
		JavaLanguageServerPlugin.getPreferencesManager().updateClientPrefences(clientCapabilities,
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

	private void emitReferenceEdges(LsifService lsif) {
		for (Entry<SymbolData, List<SymbolData>> entry : Repository.getInstance().getReferenceResultsMap().entrySet()) {
			SymbolData fromSymbolData = entry.getKey();
			List<String> referenceResultsInVs = new ArrayList<String>();
			List<String> referenceLinksInVs = new ArrayList<String>();
			for (SymbolData symbolData : entry.getValue()) {
				referenceResultsInVs.add(symbolData.getReferenceResult().getId());
				referenceLinksInVs.add(symbolData.getGroupMoniker().getId());
			}
			LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().item(fromSymbolData.getReferenceResult(), referenceResultsInVs, fromSymbolData.getDocument(),
				ItemEdge.ItemEdgeProperties.REFERENCE_RESULTS));
			LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().item(fromSymbolData.getReferenceResult(), referenceLinksInVs, fromSymbolData.getDocument(),
				ItemEdge.ItemEdgeProperties.REFERENCE_LINKS));
		}
	}

	/**
	 * Generate and emit the package information of the given project and return if
	 * the project is published.
	 *
	 * @param proj        the project
	 * @param monitor     the IProgressMonitor
	 * @param lsif        the lsif instance
	 * @param javaProject the javaProject of proj
	 * @return <code>true</code> if the given project is published
	 */
	private boolean generateExportPackageInformation(IProject proj, IProgressMonitor monitor, LsifService lsif,
			IJavaProject javaProject) throws Exception {
		IPath folderPath = proj.getLocation();
		if (folderPath == null) {
			return false;
		}
		IProjectImporter importer = this.handler.getImporter(folderPath.toFile(), monitor);
		if (importer instanceof MavenProjectImporter) {
			File pomfile = VisitorUtils.findPom(proj.getLocation(), 0);
			if (pomfile == null) {
				return false;
			}
			MavenProject mavenProject = Repository.getInstance().enlistMavenProject(lsif, pomfile);
			if (mavenProject == null) {
				return false;
			}
			Model model = mavenProject.getModel();
			String groupId = model.getGroupId();
			if (groupId == null) {
				return false;
			}
			String artifactId = model.getArtifactId();
			if (artifactId == null) {
				return false;
			}
			String version = model.getVersion();
			if (version == null) {
				return false;
			}
			Scm scm = model.getScm();
			String url = null;
			String type = null;
			// scm is optional
			if (scm != null) {
				url = scm.getUrl();
				String connect = scm.getConnection();
				if (connect != null) {
					type = ScmUrlUtils.getProvider(connect);
				}
			}
			Repository.getInstance().enlistPackageInformation(lsif, javaProject.getPath().toString(),
					groupId + "/" + artifactId, PackageInformation.MAVEN, version, type, url);
			return true;
		} else if (importer instanceof GradleProjectImporter) {
			GradleBuild build = GradleCore.getWorkspace().getBuild(proj).get();
			ProjectPublications model = build
					.withConnection(connection -> connection.getModel(ProjectPublications.class), monitor);
			List<? extends GradlePublication> publications = model.getPublications().getAll();
			if (publications.size() == 0) {
				return false;
			}
			GradleModuleVersion gradleModuleVersion = publications.get(0).getId();
			String groupId = gradleModuleVersion.getGroup();
			if (StringUtils.isEmpty(groupId)) {
				return false;
			}
			String artifactId = gradleModuleVersion.getName();
			if (StringUtils.isEmpty(artifactId)) {
				return false;
			}
			String version = gradleModuleVersion.getVersion();
			if (StringUtils.isEmpty(version)) {
				return false;
			}
			Repository.getInstance().enlistPackageInformation(lsif, javaProject.getPath().toString(),
					groupId + "/" + artifactId, PackageInformation.MAVEN, version, null, null);
			return true;
		}
		return false;
	}

	private void dumpParallelly(List<ICompilationUnit> sourceList, ExecutorService threadPool, Project projVertex,
			LsifService lsif, boolean hasPackageInformation, IProgressMonitor monitor) {
		Observable.fromIterable(sourceList)
				.flatMap(item -> Observable.just(item).observeOn(Schedulers.from(threadPool)).map(sourceFile -> {
					CompilationUnit cu = ASTUtil.createAST(sourceFile, monitor);
					Document docVertex = (new DocumentVisitor(lsif, projVertex)).enlist(sourceFile);
					if (cu == null || docVertex == null) {
						return 0;
					}
					IndexerContext context = new IndexerContext(docVertex, cu, projVertex);

					LsifVisitor lsifVisitor = new LsifVisitor(lsif, context, hasPackageInformation);
					cu.accept(lsifVisitor);

					DiagnosticVisitor diagnosticVisitor = new DiagnosticVisitor(lsif, context);
					diagnosticVisitor.enlist();

					VisitorUtils.endDocument(lsif, docVertex);

					return 0;
				})).blockingSubscribe();
	}

}
