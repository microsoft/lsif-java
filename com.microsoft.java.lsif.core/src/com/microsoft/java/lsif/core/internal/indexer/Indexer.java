/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.lsp4j.ClientCapabilities;

import com.microsoft.java.lsif.core.internal.emitter.LsifEmitter;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Event;
import com.microsoft.java.lsif.core.internal.protocol.Project;
import com.microsoft.java.lsif.core.internal.visitors.DiagnosticVisitor;
import com.microsoft.java.lsif.core.internal.visitors.DocumentVisitor;
import com.microsoft.java.lsif.core.internal.visitors.LsifVisitor;
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

		for (IProject proj : projects) {
			if (proj == null) {
				return;
			}
			IJavaProject javaProject = JavaCore.create(proj);
			if (!javaProject.exists()) {
				continue;
			}

			Project projVertex = lsif.getVertexBuilder().project();
			LsifEmitter.getInstance().emit(projVertex);
			LsifEmitter.getInstance()
					.emit(lsif.getVertexBuilder().event(Event.EventScope.Project, Event.EventKind.BEGIN,
							projVertex.getId()));

			List<ICompilationUnit> sourceList = getAllSourceFiles(javaProject);

			dumpParallely(sourceList, threadPool, projVertex, lsif, monitor);

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
			LsifService lsif, IProgressMonitor monitor) {
		Observable.fromIterable(sourceList)
				.flatMap(item -> Observable.just(item).observeOn(Schedulers.from(threadPool)).map(sourceFile -> {
					CompilationUnit cu = ASTUtil.createAST(sourceFile, monitor);
					Document docVertex = (new DocumentVisitor(lsif, projVertex)).enlist(sourceFile);
					if (cu == null || docVertex == null) {
						return 0;
					}
					IndexerContext context = new IndexerContext(docVertex, cu, projVertex);

					LsifVisitor lsifVisitor = new LsifVisitor(lsif, context);
					cu.accept(lsifVisitor);

					DiagnosticVisitor diagnosticVisitor = new DiagnosticVisitor(lsif, context);
					diagnosticVisitor.enlist();

					VisitorUtils.endDocument(lsif, docVertex);

					return 0;
				})).blockingSubscribe();
	}
}