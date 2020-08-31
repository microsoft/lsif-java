/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import java.util.List;
import java.util.stream.Collectors;

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
import com.microsoft.java.lsif.core.internal.protocol.Moniker.MonikerKind;
import com.microsoft.java.lsif.core.internal.protocol.Moniker.MonikerUnique;
import com.microsoft.java.lsif.core.internal.protocol.PackageInformation.PackageManager;
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
	private Moniker groupMoniker;
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

	synchronized public void generateMonikerImport(LsifService lsif, Range sourceRange, String identifier, String packageName, PackageManager manager, String version, String type, String url) {
		if (this.resultSet == null || this.groupMoniker != null || this.schemeMoniker != null) {
			return;
		}
		Moniker groupMoniker = lsif.getVertexBuilder().moniker(MonikerKind.IMPORT, "jdt", identifier, MonikerUnique.GROUP);
		LsifEmitter.getInstance().emit(groupMoniker);
		this.groupMoniker = groupMoniker;
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().moniker(this.resultSet, this.groupMoniker));
		if (manager == null) {
			return;
		}
		PackageInformation packageInformation = Repository.getInstance().enlistPackageInformation(lsif, packageName, packageName, manager, version, type, url);
		if (packageInformation == null) {
			return;
		}
		Moniker schemeMoniker = lsif.getVertexBuilder().moniker(MonikerKind.IMPORT, manager.toString(), packageInformation.getName() + "/" + identifier, MonikerUnique.SCHEME);
		LsifEmitter.getInstance().emit(schemeMoniker);
		this.schemeMoniker = schemeMoniker;
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().packageInformation(this.schemeMoniker, packageInformation));
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().attach(this.schemeMoniker, this.groupMoniker));
	}

	synchronized public void generateMonikerLocal(LsifService lsif, Range sourceRange, String identifier) {
		if (this.resultSet == null || this.groupMoniker != null || this.schemeMoniker != null) {
			return;
		}
		Moniker groupMoniker = lsif.getVertexBuilder().moniker(MonikerKind.LOCAL, "jdt", identifier, MonikerUnique.GROUP);
		LsifEmitter.getInstance().emit(groupMoniker);
		this.groupMoniker = groupMoniker;
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().moniker(this.resultSet, this.groupMoniker));
	}

	synchronized public void generateMonikerExport(LsifService lsif, Range sourceRange, String identifier, PackageManager manager, IJavaProject javaproject) {
		if (this.resultSet == null || this.groupMoniker != null || this.schemeMoniker != null) {
			return;
		}
		Moniker groupMoniker = lsif.getVertexBuilder().moniker(MonikerKind.EXPORT, "jdt", identifier, MonikerUnique.GROUP);
		LsifEmitter.getInstance().emit(groupMoniker);
		this.groupMoniker = groupMoniker;
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().moniker(this.resultSet, this.groupMoniker));
		if (manager == null) {
			return;
		}
		PackageInformation packageInformation = Repository.getInstance().findPackageInformationById(javaproject.getPath().toString());
		if (packageInformation == null) {
			return;
		}
		Moniker schemeMoniker = lsif.getVertexBuilder().moniker(MonikerKind.EXPORT, manager.toString(), packageInformation.getName() + "/" + identifier, MonikerUnique.SCHEME);
		LsifEmitter.getInstance().emit(schemeMoniker);
		this.schemeMoniker = schemeMoniker;
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().packageInformation(this.schemeMoniker, packageInformation));
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().attach(this.schemeMoniker, this.groupMoniker));
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

}
