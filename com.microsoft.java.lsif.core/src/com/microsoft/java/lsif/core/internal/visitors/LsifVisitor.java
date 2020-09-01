/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.provider.ScmUrlUtils;
import org.apache.maven.shared.utils.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;

import com.microsoft.java.lsif.core.internal.JdtlsUtils;
import com.microsoft.java.lsif.core.internal.indexer.IndexerContext;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.indexer.Repository;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Moniker.MonikerKind;
import com.microsoft.java.lsif.core.internal.protocol.PackageInformation.PackageManager;
import com.microsoft.java.lsif.core.internal.protocol.Project;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;

public class LsifVisitor extends ProtocolVisitor {

	private boolean hasPackageInformation;

	public LsifVisitor(LsifService lsif, IndexerContext context, boolean hasPackageInformation) {
		super(lsif, context);
		this.hasPackageInformation = hasPackageInformation;
	}

	@Override
	public boolean visit(SimpleName node) {
		return visitNode(node);
	}

	@Override
	public boolean visit(SimpleType node) {
		return visitNode(node);
	}

	private boolean visitNode(ASTNode node) {
		org.eclipse.lsp4j.Range sourceLspRange = new org.eclipse.lsp4j.Range();
		IJavaElement element = null;
		try {
			sourceLspRange = JDTUtils.toRange(this.getContext().getCompilationUnit().getTypeRoot(),
					node.getStartPosition(), node.getLength());
			element = JDTUtils.findElementAtSelection(this.getContext().getCompilationUnit().getTypeRoot(),
					sourceLspRange.getStart().getLine(), sourceLspRange.getStart().getCharacter(),
					new PreferenceManager(), new NullProgressMonitor());
		} catch (JavaModelException e) {
			JavaLanguageServerPlugin.logException(e.getMessage(), e);
		}
		if (element == null) {
			return false;
		}
		IClassFile cf = (IClassFile) element.getAncestor(IJavaElement.CLASS_FILE);
		if (cf != null) {
			resolve(isTypeOrMethodDeclaration(node), MonikerKind.IMPORT, element, sourceLspRange, cf);
		} else {
			IBinding binding;
			if (node instanceof SimpleName) {
				binding = ((SimpleName) node).resolveBinding();
			} else {
				binding = ((SimpleType) node).resolveBinding();
			}
			if (binding == null) {
				return false;
			}
			ASTNode parent = node.getParent();
			int modifier = binding.getModifiers();
			if (node instanceof SimpleName && parent instanceof EnumConstantDeclaration) {
				ASTNode enumDeclaration = parent.getParent();
				if (enumDeclaration instanceof EnumDeclaration) {
					modifier = ((EnumDeclaration) enumDeclaration).getModifiers();
				}
			}
			MonikerKind monikerKind = Modifier.isPublic(modifier) ? MonikerKind.EXPORT : MonikerKind.LOCAL;
			resolve(isTypeOrMethodDeclaration(node), monikerKind, element, sourceLspRange, cf);
		}
		return false;
	}

	private void resolve(boolean needResolveImpl, MonikerKind monikerKind, IJavaElement element,
			org.eclipse.lsp4j.Range sourceLspRange, IClassFile cf) {
		LsifService lsif = this.getLsif();
		Document docVertex = this.getContext().getDocVertex();
		Project projVertex = this.getContext().getProjVertex();
		Range sourceRange = Repository.getInstance().enlistRange(lsif, docVertex, sourceLspRange);

		Location definitionLocation = JdtlsUtils.getElementLocation(element);
		if (definitionLocation == null) {
			// no target location, only resolve hover.
			Hover hover = VisitorUtils.resolveHoverInformation(docVertex, sourceRange.getStart().getLine(),
					sourceRange.getStart().getCharacter());
			if (VisitorUtils.isEmptyHover(hover)) {
				return;
			}
			ResultSet resultSet = VisitorUtils.ensureResultSet(lsif, sourceRange);
			VisitorUtils.emitHoverResult(hover, lsif, resultSet);
			return;
		}

		String id = VisitorUtils.createSymbolKey(definitionLocation);
		Document definitionDocument = Repository.getInstance().enlistDocument(lsif, definitionLocation.getUri(),
				projVertex);
		SymbolData symbolData = Repository.getInstance().enlistSymbolData(id, definitionDocument, projVertex);
		/* Ensure resultSet */
		symbolData.ensureResultSet(lsif, sourceRange);

		try {
			resolveMoniker(element, monikerKind, symbolData, definitionLocation, sourceRange, cf);
		} catch (JavaModelException e) {
			JavaLanguageServerPlugin.logException(e.getMessage(), e);
		}

		/* Resolve definition */
		symbolData.resolveDefinition(lsif, definitionLocation);

		/* Resolve typeDefinition */
		symbolData.resolveTypeDefinition(lsif, docVertex, sourceLspRange);

		/* Resolve implementation */
		if (needResolveImpl) {
			symbolData.resolveImplementation(lsif, docVertex, sourceLspRange);
		}

		/* Resolve reference */
		symbolData.resolveReference(lsif, docVertex, definitionLocation, sourceRange);

		/* Resolve hover */
		symbolData.resolveHover(lsif, docVertex, sourceLspRange);
	}

	private void resolveMoniker(IJavaElement element, MonikerKind monikerKind, SymbolData symbolData,
			Location definitionLocation, Range sourceRange, IClassFile cf) throws JavaModelException {
		LsifService lsif = this.getLsif();

		IJavaProject javaProject = element.getJavaProject();
		PackageManager manager = resolveManager(cf, monikerKind, javaProject);
		String packageName = "";
		String version = "";
		String type = "";
		String url = "";

		// Import Monikers
		if (cf != null) {
			IPath path = cf.getPath();
			if (manager == PackageManager.JDK) {
				IPackageFragmentRoot root = javaProject.findPackageFragmentRoot(path);
				if (root instanceof JarPackageFragmentRoot) {
					Manifest manifest = ((JarPackageFragmentRoot) root).getManifest();
					if (manifest != null) {
						Attributes attributes = manifest.getMainAttributes();
						version = attributes.getValue("Implementation-Version");
					}
				}
				PackageFragmentRoot packageFragmentRoot = (PackageFragmentRoot) cf
						.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
				if (packageFragmentRoot != null) {
					IModuleDescription moduleDescription = packageFragmentRoot.getAutomaticModuleDescription();
					packageName = moduleDescription.getElementName();
				}
			} else if (manager == PackageManager.MAVEN && cf != null) {
				MavenProject mavenProject = Repository.getInstance().enlistMavenProject(lsif, path);
				if (mavenProject != null) {
					Model model = mavenProject.getModel();
					String groupId = model.getGroupId();
					String artifactId = model.getArtifactId();
					if (StringUtils.isNotEmpty(groupId) && StringUtils.isNotEmpty(artifactId)) {
						packageName = groupId + "/" + artifactId;
					}
					version = model.getVersion();
					Scm scm = model.getScm();
					if (scm != null) {
						url = scm.getUrl();
						String connect = scm.getConnection();
						if (StringUtils.isNotEmpty(connect)) {
							type = ScmUrlUtils.getProvider(connect);
						}
					}
				}
			}
		}

		String identifier = this.getJDTMonikerIdentifier(element);
		if (StringUtils.isEmpty(identifier)) {
			return;
		}

		/* Generate Moniker */
		switch (monikerKind) {
			case IMPORT:
				symbolData.generateMonikerImport(lsif, sourceRange, identifier, packageName, manager, version, type, url);
				break;
			case EXPORT:
				symbolData.generateMonikerExport(lsif, sourceRange, identifier, manager, javaProject);
				break;
			case LOCAL:
				symbolData.generateMonikerLocal(lsif, sourceRange, identifier);
				break;
			default:
		}
	}

	private PackageManager resolveManager(IClassFile cf, MonikerKind monikerKind, IJavaProject javaProject) {
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
				return null;
			}
		} else if (monikerKind == MonikerKind.EXPORT && this.hasPackageInformation) {
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

	private boolean isTypeOrMethodDeclaration(ASTNode node) {
		return node.getParent() instanceof TypeDeclaration || node.getParent() instanceof MethodDeclaration;
	}

}
