/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;

import com.microsoft.java.lsif.core.internal.LsifUtils;
import com.microsoft.java.lsif.core.internal.emitter.LsifEmitter;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Event;
import com.microsoft.java.lsif.core.internal.protocol.PackageInformation;
import com.microsoft.java.lsif.core.internal.protocol.Project;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.visitors.SymbolData;
import com.microsoft.java.lsif.core.internal.visitors.VisitorUtils;

public class Repository {

	// Key: document URI
	// Value: Document object
	private Map<String, Document> documentMap = new ConcurrentHashMap<>();

	// Key: document URI
	// Value: ranges among the documents
	// Key: LSP range
	// LSIF: range
	private Map<String, Map<org.eclipse.lsp4j.Range, Range>> rangeMap = new ConcurrentHashMap<>();

	// Key: definition meta-data id
	// Value: SymbolData
	private Map<String, SymbolData> symbolDataMap = new ConcurrentHashMap<>();

	// Key: documentURI
	// Value: Document object
	private Map<String, Document> beginededDocumentMap = new ConcurrentHashMap<>();

	// Key: groupId + artifactId
	// Value: PackageInformation
	private Map<String, PackageInformation> packageInformationMap = new ConcurrentHashMap<>();

	// Key: pomFile path
	// Value: PackageInformation
	private Map<String, MavenProject> mavenProjectMap = new ConcurrentHashMap<>();

	// Key: SymbolData
	// Value: List<SymbolData>
	private Map<SymbolData, List<SymbolData>> referenceResultsMap = new ConcurrentHashMap<>();

	private Repository() {
	}

	private static class RepositoryHolder {
		private static final Repository INSTANCE = new Repository();
	}

	public static Repository getInstance() {
		return RepositoryHolder.INSTANCE;
	}

	public synchronized Document enlistDocument(LsifService service, String uri, Project projVertex) {
		uri = LsifUtils.normalizeUri(uri);
		Document targetDocument = findDocumentByUri(uri);
		if (targetDocument == null) {
			targetDocument = service.getVertexBuilder().document(uri);
			addDocument(targetDocument);
			LsifEmitter.getInstance().emit(targetDocument);
			addToBeginededDocuments(targetDocument);
			LsifEmitter.getInstance().emit(service.getVertexBuilder().event(Event.EventScope.DOCUMENT,
					Event.EventKind.BEGIN, targetDocument.getId()));
			LsifEmitter.getInstance().emit(service.getEdgeBuilder().contains(projVertex, targetDocument));
		}

		return targetDocument;
	}

	public synchronized Range enlistRange(LsifService service, Document docVertex, org.eclipse.lsp4j.Range lspRange) {
		Range range = findRange(docVertex.getUri(), lspRange);
		if (range == null) {
			range = service.getVertexBuilder().range(lspRange);
			addRange(docVertex, lspRange, range);
			LsifEmitter.getInstance().emit(range);
			LsifEmitter.getInstance().emit(service.getEdgeBuilder().contains(docVertex, range));
		}
		return range;
	}

	public Range enlistRange(LsifService service, String uri, org.eclipse.lsp4j.Range lspRange, Project projVertex) {
		return enlistRange(service, enlistDocument(service, uri, projVertex), lspRange);
	}

	public synchronized SymbolData enlistSymbolData(String id, Document docVertex, Project projVertex) {
		SymbolData symbolData = findSymbolDataById(id);
		if (symbolData == null) {
			symbolData = new SymbolData(projVertex, docVertex);
			addSymbolData(id, symbolData);
		}
		return symbolData;
	}

	public synchronized PackageInformation enlistPackageInformation(LsifService lsif, String id, String name,
			String manager, String version, String type, String url) {
		PackageInformation packageInformation = findPackageInformationById(id);
		if (packageInformation == null) {
			packageInformation = lsif.getVertexBuilder().packageInformation(name, manager, version, type, url);
			addPackageInformation(id, packageInformation);
			LsifEmitter.getInstance().emit(packageInformation);
		}
		return packageInformation;
	}

	public synchronized MavenProject enlistMavenProject(LsifService lsif, File pomFile) {
		MavenProject mavenProject = findMavenProjectByPath(pomFile.getAbsolutePath());
		if (mavenProject == null) {
			IMaven maven = MavenPlugin.getMaven();
			try {
				mavenProject = maven.readProject(pomFile, new NullProgressMonitor());
				addMavenProject(pomFile.getAbsolutePath(), mavenProject);
			} catch (CoreException ce) {
				JavaLanguageServerPlugin.logException(ce.getMessage(), ce);
			}
		}
		return mavenProject;
	}

	public synchronized MavenProject enlistMavenProject(LsifService lsif, IPath path) {
		// For Maven dependency, use findPom(path, 1). For Gradle dependency, use findPom(path, 2).
		File pomFile = VisitorUtils.findPom(path, 2);
		if (pomFile == null) {
			return null;
		}
		return enlistMavenProject(lsif, pomFile);
	}

	public void addToBeginededDocuments(Document doc) {
		this.beginededDocumentMap.put(doc.getUri(), doc);
	}

	public void removeFromBeginededDocuments(String uri) {
		this.beginededDocumentMap.remove(uri);
	}

	public ArrayList<Document> getAllBeginededDocuments() {
		return new ArrayList<>(this.documentMap.values());
	}

	private void addDocument(Document doc) {
		this.documentMap.put(doc.getUri(), doc);
	}

	private void addRange(Document owner, org.eclipse.lsp4j.Range lspRange, Range range) {
		Map<org.eclipse.lsp4j.Range, Range> ranges = this.rangeMap.computeIfAbsent(owner.getUri(),
				s -> new ConcurrentHashMap<>());
		ranges.putIfAbsent(lspRange, range);
	}

	private void addSymbolData(String id, SymbolData symbolData) {
		this.symbolDataMap.put(id, symbolData);
	}

	private Document findDocumentByUri(String uri) {
		return this.documentMap.getOrDefault(uri, null);
	}

	private Range findRange(String uri, org.eclipse.lsp4j.Range lspRange) {
		Map<org.eclipse.lsp4j.Range, Range> ranges = rangeMap.get(uri);
		if (ranges != null) {
			return ranges.get(lspRange);
		}
		return null;
	}

	private SymbolData findSymbolDataById(String id) {
		return this.symbolDataMap.getOrDefault(id, null);
	}

	public PackageInformation findPackageInformationById(String id) {
		return this.packageInformationMap.getOrDefault(id, null);
	}

	private void addPackageInformation(String id, PackageInformation packageInformation) {
		this.packageInformationMap.put(id, packageInformation);
	}

	private MavenProject findMavenProjectByPath(String path) {
		return this.mavenProjectMap.getOrDefault(path, null);
	}

	private void addMavenProject(String id, MavenProject mavenProject) {
		this.mavenProjectMap.put(id, mavenProject);
	}

	public synchronized void addReferenceResults(SymbolData from, SymbolData to) {
		if (this.referenceResultsMap.containsKey(from)) {
			this.referenceResultsMap.get(from).add(to);
		} else {
			this.referenceResultsMap.put(from, Arrays.asList(to));
		}

	}

	public Map<SymbolData, List<SymbolData>> getReferenceResultsMap() {
		return this.referenceResultsMap;
	}

}
