/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;

import com.microsoft.java.lsif.core.internal.emitter.LsifEmitter;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.indexer.Repository;
import com.microsoft.java.lsif.core.internal.protocol.DefinitionResult;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.ImplementationResult;
import com.microsoft.java.lsif.core.internal.protocol.ItemEdge;
import com.microsoft.java.lsif.core.internal.protocol.Moniker;
import com.microsoft.java.lsif.core.internal.protocol.PackageInformation;
import com.microsoft.java.lsif.core.internal.protocol.Project;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ReferenceResult;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;
import com.microsoft.java.lsif.core.internal.protocol.TypeDefinitionResult;

public class SymbolData {

	private Project project;
	private Document document;
	private ResultSet resultSet;
	private ReferenceResult referenceResult;
	private Moniker projectMoniker;
	private Moniker schemeMoniker;
	private boolean definitionResolved;
	private boolean typeDefinitionResolved;
	private boolean implementationResolved;
	private boolean hoverResolved;

	public SymbolData(Project project, Document document) {
		this.project = project;
		this.document = document;
	}

	synchronized public void ensureResultSet(LsifService lsif, Range sourceRange) {
		if (this.resultSet == null) {
			ResultSet resultSet = lsif.getVertexBuilder().resultSet();
			LsifEmitter.getInstance().emit(resultSet);
			this.resultSet = resultSet;
		}
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().next(sourceRange, this.resultSet));
	}

	synchronized public void generateMonikerImport(LsifService lsif, Range sourceRange,
			String identifier, String schemeMonikerName, String manager, String version) {
		if (this.resultSet == null || this.projectMoniker != null || this.schemeMoniker != null) {
			return;
		}
		Moniker projectMoniker = lsif.getVertexBuilder().moniker("import", "jdt", identifier, "project");
		LsifEmitter.getInstance().emit(projectMoniker);
		this.projectMoniker = projectMoniker;
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().moniker(this.resultSet, this.projectMoniker));
		PackageInformation packageInformation = Repository.getInstance().enlistImportPackageInformation(lsif,
				createMonikerKey(schemeMonikerName), schemeMonikerName, manager, version);
		if (!Repository.getInstance().enlistPackageInformationEmitted(schemeMonikerName)) {
				LsifEmitter.getInstance().emit(packageInformation);
			}
			Moniker schemeMoniker = manager.equals("maven")
					? lsif.getVertexBuilder().moniker("import", manager,
							packageInformation.getName() + "/" + identifier, "scheme")
					: lsif.getVertexBuilder().moniker("import", manager, identifier, "scheme");
			LsifEmitter.getInstance().emit(schemeMoniker);
			this.schemeMoniker = schemeMoniker;
			LsifEmitter.getInstance()
					.emit(lsif.getEdgeBuilder().packageInformation(this.schemeMoniker, packageInformation));
			LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().attach(this.schemeMoniker, this.projectMoniker));
	}

	synchronized public void generateMonikerLocal(LsifService lsif, Range sourceRange, String identifier) {
		if (this.resultSet == null || this.projectMoniker != null || this.schemeMoniker != null) {
			return;
		}
		Moniker projectMoniker = lsif.getVertexBuilder().moniker("local", "jdt", identifier, "project");
		LsifEmitter.getInstance().emit(projectMoniker);
		this.projectMoniker = projectMoniker;
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().moniker(this.resultSet, this.projectMoniker));
	}

	synchronized public void generateMonikerExport(LsifService lsif, Range sourceRange,
			String identifier, String manager, IJavaProject javaproject) {
		if (this.resultSet == null || this.projectMoniker != null || this.schemeMoniker != null) {
			return;
		}
		Moniker projectMoniker = lsif.getVertexBuilder().moniker("export", "jdt", identifier, "project");
		LsifEmitter.getInstance().emit(projectMoniker);
		this.projectMoniker = projectMoniker;
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().moniker(this.resultSet, this.projectMoniker));
		PackageInformation packageInformation = Repository.getInstance().enlistExportPackageInformation(lsif,
				javaproject.getPath().toString(), "", manager, "", "");
		Moniker schemeMoniker = lsif.getVertexBuilder().moniker("export", manager,
				packageInformation.getName() + "/" + identifier,
				"scheme");
		LsifEmitter.getInstance().emit(schemeMoniker);
		this.schemeMoniker = schemeMoniker;
		LsifEmitter.getInstance()
				.emit(lsif.getEdgeBuilder().packageInformation(this.schemeMoniker, packageInformation));
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().attach(this.schemeMoniker, this.projectMoniker));
	}

	synchronized public void resolveDefinition(LsifService lsif, Location definitionLocation) {
		if (this.definitionResolved) {
			return;
		}
		org.eclipse.lsp4j.Range definitionLspRange = definitionLocation.getRange();
		Document definitionDocument = Repository.getInstance().enlistDocument(lsif, definitionLocation.getUri(),
				project);
		Range definitionRange = Repository.getInstance().enlistRange(lsif, definitionDocument, definitionLspRange);
		DefinitionResult defResult = VisitorUtils.ensureDefinitionResult(lsif, this.resultSet);
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().item(defResult, definitionRange, document,
				ItemEdge.ItemEdgeProperties.DEFINITIONS));
		this.definitionResolved = true;
	}

	synchronized public void resolveTypeDefinition(LsifService lsif, Document docVertex,
			org.eclipse.lsp4j.Range sourceLspRange) {
		if (this.typeDefinitionResolved) {
			return;
		}
		Location typeDefinitionLocation = VisitorUtils.resolveTypeDefinitionLocation(docVertex,
				sourceLspRange.getStart().getLine(),
				sourceLspRange.getStart().getCharacter());
		if (typeDefinitionLocation != null) {
			org.eclipse.lsp4j.Range typeDefinitionLspRange = typeDefinitionLocation.getRange();
			Document typeDefinitionDocument = Repository.getInstance().enlistDocument(lsif,
					typeDefinitionLocation.getUri(), project);
			Range typeDefinitionRange = Repository.getInstance().enlistRange(lsif, typeDefinitionDocument,
					typeDefinitionLspRange);

			TypeDefinitionResult typeDefResult = VisitorUtils.ensureTypeDefinitionResult(lsif, this.resultSet);
			LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().item(typeDefResult, typeDefinitionRange, document,
					ItemEdge.ItemEdgeProperties.DEFINITIONS));
		}
		this.typeDefinitionResolved = true;
	}

	synchronized public void resolveImplementation(LsifService lsif, Document docVertex,
			org.eclipse.lsp4j.Range sourceLspRange) {
		if (this.implementationResolved) {
			return;
		}
		List<Range> implementationRanges = VisitorUtils.getImplementationRanges(lsif, project, docVertex,
				sourceLspRange.getStart().getLine(),
				sourceLspRange.getStart().getCharacter());
		if (implementationRanges != null && implementationRanges.size() > 0) {

			// ImplementationResult
			List<String> rangeIds = implementationRanges.stream().map(r -> r.getId()).collect(Collectors.toList());
			ImplementationResult implResult = VisitorUtils.ensureImplementationResult(lsif, this.resultSet);
			LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().item(implResult, rangeIds, document,
					ItemEdge.ItemEdgeProperties.IMPLEMENTATION_RESULTS));
		}
		this.implementationResolved = true;
	}

	synchronized public void resolveReference(LsifService lsif, Document sourceDocument, Location definitionLocation,
			Range sourceRange) {
		if (this.referenceResult == null) {
			ReferenceResult referenceResult = VisitorUtils.ensureReferenceResult(lsif, this.resultSet);
			this.referenceResult = referenceResult;
		}

		Document definitionDocument = Repository.getInstance().enlistDocument(lsif, definitionLocation.getUri(),
				project);
		Range definitionRange = Repository.getInstance().enlistRange(lsif, definitionDocument,
				definitionLocation.getRange());

		if (!VisitorUtils.isDefinitionItself(sourceDocument, sourceRange, definitionDocument, definitionRange)) {
			LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().item(this.referenceResult, sourceRange, document,
					ItemEdge.ItemEdgeProperties.REFERENCES));
		}
	}

	synchronized public void resolveHover(LsifService lsif, Document docVertex,
			org.eclipse.lsp4j.Range sourceLspRange) {
		if (this.hoverResolved) {
			return;
		}
		Hover hover = VisitorUtils.resolveHoverInformation(docVertex, sourceLspRange.getStart().getLine(),
				sourceLspRange.getStart().getCharacter());
		if (!VisitorUtils.isEmptyHover(hover)) {
			VisitorUtils.emitHoverResult(hover, lsif, this.resultSet);
		}
		this.hoverResolved = true;
	}

	private String createMonikerKey(String name) {
		return DigestUtils.md5Hex(name);
	}
}
