package com.microsoft.java.lsif.core.internal.indexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.lsp4j.ClientCapabilities;

import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.emitter.LsifEmitter;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Project;
import com.microsoft.java.lsif.core.internal.visitors.DefinitionVisitor;
import com.microsoft.java.lsif.core.internal.visitors.DiagnosticVisitor;
import com.microsoft.java.lsif.core.internal.visitors.DocumentVisitor;
import com.microsoft.java.lsif.core.internal.visitors.HoverVisitor;
import com.microsoft.java.lsif.core.internal.visitors.ImplementationsVisitor;
import com.microsoft.java.lsif.core.internal.visitors.ProtocolVisitor;
import com.microsoft.java.lsif.core.internal.visitors.ReferencesVisitor;
import com.microsoft.java.lsif.core.internal.visitors.TypeDefinitionVisitor;

public class Indexer {

	private WorkspaceHandler handler;

	//@formatter:off
	private List<ProtocolVisitor> visitors = Arrays.asList(
			new DefinitionVisitor(),
			new TypeDefinitionVisitor(),
			new ImplementationsVisitor(),
			new ReferencesVisitor(),
			new HoverVisitor());
	//@formatter:on

	public Indexer() {
		this.handler = new WorkspaceHandler(System.getProperty("intellinav.repo.path"));
	}

	public void generateLsif() throws JavaModelException {
		NullProgressMonitor monitor = new NullProgressMonitor();
		IPath path = this.handler.initialize();

		initializeJdtls();
		LsifService lsif = new LsifService();

		LsifEmitter.getInstance().start();
		LsifEmitter.getInstance().emit(lsif.getVertexBuilder().metaData());

		handler.importProject(path, monitor);
		handler.buildProject(monitor);
		buildIndex(path, monitor, lsif);
		handler.removeProject(monitor);

		LsifEmitter.getInstance().end();
	}

	private void buildIndex(IPath path, IProgressMonitor monitor, LsifService lsif)
			throws JavaModelException {

		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

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
			IClasspathEntry[] references = javaProject.getRawClasspath();
			for (IClasspathEntry reference : references) {
				if (reference.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPackageFragmentRoot[] fragmentRoots = javaProject.findPackageFragmentRoots(reference);
					for (IPackageFragmentRoot fragmentRoot : fragmentRoots) {
						for (IJavaElement child : fragmentRoot.getChildren()) {
							IPackageFragment fragment = (IPackageFragment) child;
							if (fragment.hasChildren()) {
								for (IJavaElement sourceFile : fragment.getChildren()) {
									if (!sourceFile.exists()) {
										continue;
									}
									CompilationUnit cu = ASTUtil.createAST((ITypeRoot) sourceFile, monitor);

									IndexerContext currentContext = new IndexerContext(lsif, null,
											(ITypeRoot) sourceFile, JavaLanguageServerPlugin.getPreferencesManager());

									Document docVertex = (new DocumentVisitor(currentContext, projVertex))
											.enlist(sourceFile);
									currentContext.setDocVertex(docVertex);

									List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
									for (ProtocolVisitor vis : this.visitors) {
										vis.setContext(currentContext);
										completableFutures.add(CompletableFuture.runAsync(() -> {
											cu.accept(vis);
										}));
									}

									DiagnosticVisitor diagnosticVisitor = new DiagnosticVisitor(currentContext, cu);
									completableFutures.add(CompletableFuture.runAsync(() -> {
										diagnosticVisitor.enlist();
									}));
									try {
										CompletableFuture
												.allOf(completableFutures
														.toArray(new CompletableFuture[completableFutures.size()]))
												.get();
									} catch (InterruptedException | ExecutionException e) {
										LanguageServerIndexerPlugin.logException("Exception occurs when indexing: ",
												e);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void initializeJdtls() {
		Map<String, Object> extendedClientCapabilities = new HashMap<>();
		extendedClientCapabilities.put("classFileContentsSupport", false);
		JavaLanguageServerPlugin.getPreferencesManager().updateClientPrefences(new ClientCapabilities(),
				extendedClientCapabilities);
	}
}