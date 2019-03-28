package com.microsoft.java.lsif.core.internal.indexer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.adapters.HoverTypeAdapter;
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapter;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapter;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.java.lsif.core.internal.IConstant;
import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.emitter.Emitter;
import com.microsoft.java.lsif.core.internal.emitter.JsonEmitter;
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

	private static final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new CollectionTypeAdapter.Factory())
			.registerTypeAdapterFactory(new EnumTypeAdapter.Factory())
			.registerTypeAdapterFactory(new HoverTypeAdapter.Factory())
			.registerTypeAdapterFactory(new EitherTypeAdapter.Factory()).create();

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

	public void generateLsif() {
		long startTime = System.currentTimeMillis();
		NullProgressMonitor monitor = new NullProgressMonitor();
		List<IPath> projectRoots = this.handler.initialize();
		initializeJdtls();

		JsonEmitter emitter = new JsonEmitter();

		for (IPath path : projectRoots) {
			try {
				LanguageServerIndexerPlugin.logInfo("Starting index project: " + path.toPortableString());
				handler.importProject(path, monitor);
				handler.buildProject(monitor);
				buildIndex(path, monitor, emitter);
				handler.removeProject(path, monitor);
				LanguageServerIndexerPlugin.logInfo("End index project: " + path.toPortableString());
			} catch (Exception ex) {
				LanguageServerIndexerPlugin.logException("Exception when indexing ", ex);
			}
		}
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		LanguageServerIndexerPlugin.logInfo("Total execution time: " + elapsedTime / 1000 + "s.");
	}

	private void buildIndex(IPath path, IProgressMonitor monitor, Emitter emitter) {
		LsifService lsif = new LsifService();

		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		for (IProject proj : projects) {
			emitter.start();
			if (proj == null) {
				return;
			}

			emitter.emit(lsif.getVertexBuilder().metaData("0.1.0"));

			IJavaProject javaProject = JavaCore.create(proj);
			if (!javaProject.exists()) {
				continue;
			}
			try {
				Project projVertex = lsif.getVertexBuilder().project();
				emitter.emit(projVertex);
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
										LanguageServerIndexerPlugin
												.logInfo("Indexing file " + sourceFile.getResource());
										CompilationUnit cu = ASTUtil.createAST((ITypeRoot) sourceFile, monitor);

										IndexerContext currentContext = new IndexerContext(emitter, lsif, null,
												(ITypeRoot) sourceFile,
												JavaLanguageServerPlugin.getPreferencesManager());

										Document docVertex = (new DocumentVisitor(currentContext, projVertex))
												.enlist(sourceFile);
										currentContext.setDocVertex(docVertex);

										for (ProtocolVisitor vis : this.visitors) {
											vis.setContext(currentContext);
											cu.accept(vis);
										}

										// Dump diagnostic information
										DiagnosticVisitor diagnosticVisitor = new DiagnosticVisitor(currentContext, cu);
										diagnosticVisitor.enlist();
									}
								}
							}
						}
					}
				}
			} catch (Exception ex) {
				LanguageServerIndexerPlugin.logException("Exception when indexing ", ex);
			} finally {
				// Output model
				try {
					Path projectPath = Paths.get(path.toFile().toURI());
					FileUtils.writeStringToFile(projectPath.resolve(IConstant.DEFAULT_LSIF_FILE_NAME).toFile(),
							gson.toJson(emitter.getElements()));
				} catch (IOException e) {
				}
			}
			emitter.end();
		}
	}

	private void initializeJdtls() {
		Map<String, Object> extendedClientCapabilities = new HashMap<>();
		extendedClientCapabilities.put("classFileContentsSupport", false);
		JavaLanguageServerPlugin.getPreferencesManager().updateClientPrefences(new ClientCapabilities(),
				extendedClientCapabilities);
	}
}