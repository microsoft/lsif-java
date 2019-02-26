package com.microsoft.java.lsif.core.internal.indexer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.java.lsif.core.internal.IConstant;
import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.emitter.Emitter;
import com.microsoft.java.lsif.core.internal.emitter.JsonEmitter;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Project;
import com.microsoft.java.lsif.core.internal.visitors.DocumentVisitor;

public class Indexer {

	private static final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new CollectionTypeAdapterFactory())
			.registerTypeAdapterFactory(new EnumTypeAdapterFactory()).create();

	private WorkspaceHandler handler;

	public Indexer() {
		this.handler = new WorkspaceHandler(System.getProperty("intellinav.repo.path"));
	}

	public void buildModel() {
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
				// ignore it
			} finally {
				// Output model
				try {
					Path projectPath = Paths.get(path.toFile().toURI());
					FileUtils.writeStringToFile(projectPath.resolve(IConstant.DEFAULT_LSIF_FILE_NAME).toFile(), gson.toJson(emitter.getElements()));
				} catch (IOException e) {
				}
			}
		}
	}

	private void buildIndex(IPath path, IProgressMonitor monitor, Emitter emitter) {
		LsifService lsif = new LsifService();
		emitter.start();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		LanguageServerIndexerPlugin.logInfo(String.format("collectModel, projects # = %d", projects.length));

		for (IProject proj : projects) {
			if (proj == null) {
				return;
			}

			emitter.emit(lsif.getVertexBuilder().metaData("0.1.0"));

			IJavaProject javaProject = JavaCore.create(proj);
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
										CompilationUnit cu = ASTUtil.createAST((ITypeRoot) sourceFile, monitor);

										IndexerContext currentContext = new IndexerContext(emitter, lsif, null, (ITypeRoot) sourceFile,
												JavaLanguageServerPlugin.getPreferencesManager());

										Document docVertex = (new DocumentVisitor(currentContext, projVertex)).enlist(sourceFile);
										currentContext.setDocVertex(docVertex);

										cu.accept(new LsifVisitor((new IndexerContext(emitter, lsif, docVertex, (ITypeRoot) sourceFile,
												JavaLanguageServerPlugin.getPreferencesManager()))));
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
			}
		}
		emitter.end();
	}

	private void initializeJdtls() {
		Map<String, Object> extendedClientCapabilities = new HashMap<>();
		extendedClientCapabilities.put("classFileContentsSupport", false);
		JavaLanguageServerPlugin.getPreferencesManager().updateClientPrefences(new ClientCapabilities(), extendedClientCapabilities);
	}
}