/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.provider.ScmUrlUtils;
import org.apache.maven.shared.utils.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;

import com.microsoft.java.lsif.core.internal.emitter.LsifEmitter;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.indexer.Repository;
import com.microsoft.java.lsif.core.internal.protocol.DefinitionResult;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.ImplementationResult;
import com.microsoft.java.lsif.core.internal.protocol.ImportPackageMetaData;
import com.microsoft.java.lsif.core.internal.protocol.ItemEdge;
import com.microsoft.java.lsif.core.internal.protocol.Moniker;
import com.microsoft.java.lsif.core.internal.protocol.Moniker.MonikerKind;
import com.microsoft.java.lsif.core.internal.protocol.Moniker.MonikerUnique;
import com.microsoft.java.lsif.core.internal.protocol.PackageInformation;
import com.microsoft.java.lsif.core.internal.protocol.PackageInformation.PackageManager;
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

	synchronized public void generateImportMoniker(LsifService lsif, String identifier, PackageManager manager,
			String packageName, String version, String type, String url) {
		if (this.resultSet == null || this.groupMoniker != null || this.schemeMoniker != null) {
			return;
		}
		this.groupMoniker = lsif.getVertexBuilder().moniker(MonikerKind.IMPORT, "jdt", identifier, MonikerUnique.GROUP);
		LsifEmitter.getInstance().emit(this.groupMoniker);
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().moniker(this.resultSet, this.groupMoniker));
		if (manager == null) {
			return;
		}
		PackageInformation packageInformation = Repository.getInstance().enlistPackageInformation(lsif, packageName, packageName, manager, version, type, url);
		if (packageInformation == null) {
			return;
		}
		this.schemeMoniker = lsif.getVertexBuilder().moniker(MonikerKind.IMPORT, manager.toString(),
				packageInformation.getName() + "/" + identifier, MonikerUnique.SCHEME);
		LsifEmitter.getInstance().emit(this.schemeMoniker);
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().packageInformation(this.schemeMoniker, packageInformation));
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().attach(this.schemeMoniker, this.groupMoniker));
	}

	synchronized public void generateLocalMoniker(LsifService lsif, String identifier) {
		if (this.resultSet == null || this.groupMoniker != null || this.schemeMoniker != null) {
			return;
		}
		this.groupMoniker = lsif.getVertexBuilder().moniker(MonikerKind.LOCAL, "jdt", identifier, MonikerUnique.GROUP);
		LsifEmitter.getInstance().emit(this.groupMoniker);
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().moniker(this.resultSet, this.groupMoniker));
	}

	synchronized public void generateExportMoniker(LsifService lsif, String identifier, PackageManager manager,
			String projectPath) {
		if (this.resultSet == null || this.groupMoniker != null || this.schemeMoniker != null) {
			return;
		}
		this.groupMoniker = lsif.getVertexBuilder().moniker(MonikerKind.EXPORT, "jdt", identifier, MonikerUnique.GROUP);
		LsifEmitter.getInstance().emit(this.groupMoniker);
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().moniker(this.resultSet, this.groupMoniker));
		if (manager == null) {
			return;
		}
		PackageInformation packageInformation = Repository.getInstance().findPackageInformationById(projectPath);
		if (packageInformation == null) {
			return;
		}
		this.schemeMoniker = lsif.getVertexBuilder().moniker(MonikerKind.EXPORT, manager.toString(),
				packageInformation.getName() + "/" + identifier, MonikerUnique.SCHEME);
		LsifEmitter.getInstance().emit(this.schemeMoniker);
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

	public void resolveMoniker(LsifService lsif, IJavaElement element, int modifier,
			boolean hasPackageInformation) throws JavaModelException {

		IJavaProject javaProject = element.getJavaProject();
		IClassFile cf = (IClassFile) element.getAncestor(IJavaElement.CLASS_FILE);
		MonikerKind monikerKind = resolveMonikerKind(cf, modifier);
		PackageManager manager = resolveManager(cf, monikerKind, javaProject, hasPackageInformation);
		String identifier = this.getJDTMonikerIdentifier(element);
		if (StringUtils.isEmpty(identifier)) {
			return;
		}

		switch (monikerKind) {
			case IMPORT:
				ImportPackageMetaData metaData = generateImportMonikerData(lsif, cf, manager, javaProject);
				if (metaData == null) {
					return;
				}
				generateImportMoniker(lsif, identifier, manager, metaData.packageName, metaData.version, metaData.type,
						metaData.url);
				break;
			case EXPORT:
				generateExportMoniker(lsif, identifier, manager, javaProject.getPath().toString());
				break;
			case LOCAL:
				generateLocalMoniker(lsif, identifier);
				break;
			default:
		}
	}

	private MonikerKind resolveMonikerKind(IClassFile cf, int modifier) {
		if (cf != null) {
			return MonikerKind.IMPORT;
		} else {
			if (Modifier.isPublic(modifier)) {
				return MonikerKind.EXPORT;
			} else {
				return MonikerKind.LOCAL;
			}
		}
	}

	private PackageManager resolveManager(IClassFile cf, MonikerKind monikerKind, IJavaProject javaProject,
			boolean hasPackageInformation) {
		if (cf != null) {
			try {
				IPath path = cf.getPath();
				IPackageFragmentRoot root = javaProject.findPackageFragmentRoot(path);
				IClasspathEntry container = root.getRawClasspathEntry();
				IPath containerPath = container.getPath();
				String pathName = containerPath.toString();
				if (pathName.startsWith(JavaRuntime.JRE_CONTAINER)) {
					return PackageManager.JDK;
				} else {
					return PackageManager.MAVEN;
				}
			} catch (JavaModelException e) {
				JavaLanguageServerPlugin.logException(e.getMessage(), e);
			}
		} else if (monikerKind == MonikerKind.EXPORT && hasPackageInformation) {
			return PackageManager.MAVEN;
		}
		return null;
	}

	private String getJDTMonikerIdentifier(IJavaElement element) {
		String identifier = element.getElementName();
		try {
			if (element instanceof IType) {
				return ((IType) element).getFullyQualifiedName();
			} else if (element instanceof IField || element instanceof ILocalVariable) {
				return getJDTMonikerIdentifier(element.getParent()) + "/" + identifier;
			} else if (element instanceof IMethod) {
				return getJDTMonikerIdentifier(element.getParent()) + "/" + identifier + ":"
						+ ((IMethod) element).getSignature();
			}
		} catch (JavaModelException e) {
			return "";
		}
		return identifier;
	}

	private ImportPackageMetaData generateImportMonikerData(LsifService lsif, IClassFile cf, PackageManager manager,
			IJavaProject javaProject) throws JavaModelException {
		ImportPackageMetaData importPackageMetaData = new ImportPackageMetaData();
		if (cf != null) {
			IPath path = cf.getPath();
			if (manager == PackageManager.JDK) {
				IPackageFragmentRoot root = javaProject.findPackageFragmentRoot(path);
				if (root instanceof JarPackageFragmentRoot) {
					Manifest manifest = ((JarPackageFragmentRoot) root).getManifest();
					if (manifest == null) {
						return null;
					}
					Attributes attributes = manifest.getMainAttributes();
					importPackageMetaData.version = attributes.getValue("Implementation-Version");
					if (StringUtils.isEmpty(importPackageMetaData.version)) {
						return null;
					}
				}
				PackageFragmentRoot packageFragmentRoot = (PackageFragmentRoot) cf
						.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
				if (packageFragmentRoot == null) {
					return null;
				}
				IModuleDescription moduleDescription = packageFragmentRoot.getAutomaticModuleDescription();
				importPackageMetaData.packageName = moduleDescription.getElementName();
			} else if (manager == PackageManager.MAVEN) {
				MavenProject mavenProject = Repository.getInstance().enlistMavenProject(lsif, path);
				if (mavenProject == null) {
					return null;
				}
				Model model = mavenProject.getModel();
				String groupId = model.getGroupId();
				if (StringUtils.isEmpty(groupId)) {
					return null;
				}
				String artifactId = model.getArtifactId();
				if (StringUtils.isEmpty(artifactId)) {
					return null;
				}
				importPackageMetaData.packageName = groupId + "/" + artifactId;
				importPackageMetaData.version = model.getVersion();
				if (StringUtils.isEmpty(importPackageMetaData.version)) {
					return null;
				}
				Scm scm = model.getScm();
				// scm is optional
				if (scm != null) {
					importPackageMetaData.url = scm.getUrl();
					String connect = scm.getConnection();
					if (StringUtils.isNotEmpty(connect)) {
						importPackageMetaData.type = ScmUrlUtils.getProvider(connect);
					}
				}
			}
		}
		return importPackageMetaData;
	}

}
