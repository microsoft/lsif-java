/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;

import com.microsoft.java.lsif.core.internal.JdtlsUtils;
import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
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

	private boolean isPublish;

	public LsifVisitor(LsifService lsif, IndexerContext context, boolean isPublish) {
		super(lsif, context);
		this.isPublish = isPublish;
	}

	@Override
	public boolean visit(SimpleName node) {
		resolve(node.getStartPosition(), node.getLength(), isTypeOrMethodDeclaration(node), MonikerKind.IMPORT);
		return false;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		MonikerKind monikerKind = getMonikerKind(node);
		resolve(node.getName().getStartPosition(), node.getName().getLength(), false, monikerKind);
		return true;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		MonikerKind monikerKind = getMonikerKind(node);
		resolve(node.getName().getStartPosition(), node.getName().getLength(), false, monikerKind);
		return true;
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		MonikerKind monikerKind = MonikerKind.EXPORT; // All the enum values are modified by `public static final`
		resolve(node.getName().getStartPosition(), node.getName().getLength(), false, monikerKind);
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		MonikerKind monikerKind = getMonikerKind(node);
		resolve(node.getName().getStartPosition(), node.getName().getLength(), false, monikerKind);
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		MonikerKind monikerKind = getMonikerKind(node);
		resolve(node.getName().getStartPosition(), node.getName().getLength(), false, monikerKind);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		ASTNode parent = node.getParent();
		if (parent instanceof VariableDeclarationStatement) {
			MonikerKind monikerKind = getMonikerKind((VariableDeclarationStatement) parent);
			resolve(node.getName().getStartPosition(), node.getName().getLength(), false, monikerKind);
		} else if (parent instanceof FieldDeclaration) {
			MonikerKind monikerKind = getMonikerKind((FieldDeclaration) parent);
			resolve(node.getName().getStartPosition(), node.getName().getLength(), false, monikerKind);
		}
		return true;
	}

	@Override
	public boolean visit(SimpleType node) {
		resolve(node.getStartPosition(), node.getLength(), isTypeOrMethodDeclaration(node), MonikerKind.IMPORT);
		return false;
	}

	private void resolve(int startPosition, int length, boolean needResolveImpl, MonikerKind monikerKind) {
		try {
			org.eclipse.lsp4j.Range sourceLspRange = JDTUtils
					.toRange(this.getContext().getCompilationUnit().getTypeRoot(), startPosition, length);

			IJavaElement element = JDTUtils.findElementAtSelection(this.getContext().getCompilationUnit().getTypeRoot(),
					sourceLspRange.getStart().getLine(), sourceLspRange.getStart().getCharacter(),
					new PreferenceManager(), new NullProgressMonitor());
			if (element == null) {
				return;
			}

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

			resolveMoniker(element, monikerKind, symbolData, definitionLocation, sourceRange);

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
		} catch (Throwable ex) {
			LanguageServerIndexerPlugin.logException("Exception when dumping definition information ", ex);
		}
	}

	private void resolveMoniker(IJavaElement element, MonikerKind monikerKind, SymbolData symbolData, Location definitionLocation, Range sourceRange) throws JavaModelException {
		LsifService lsif = this.getLsif();
		// Import Monikers
		IJavaProject javaProject = element.getJavaProject();
		IClassFile cf = (IClassFile) element.getAncestor(IJavaElement.CLASS_FILE);
		PackageManager manager = null;
		String packageName = "";
		String version = "";
		String type = "";
		String url = "";
		if (cf != null) {
			IPath path = cf.getPath();
			IPackageFragmentRoot root = javaProject.findPackageFragmentRoot(path);
			IClasspathEntry container = root.getRawClasspathEntry();
			IPath containerPath = container.getPath();
			String pathName = containerPath.toString();
			if (pathName.startsWith(JavaRuntime.JRE_CONTAINER)) {
				// JDK Library
				manager = PackageManager.JDK;
				Manifest manifest = new Manifest();
				if (root instanceof JarPackageFragmentRoot) {
					manifest = ((JarPackageFragmentRoot) root).getManifest();
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
			} else {
				manager = PackageManager.MAVEN;
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
						type = ScmUrlUtils.getProvider(scm.getConnection());
					}
				}
			}
		}

		// Export Monikers
		if (monikerKind == MonikerKind.EXPORT && this.isPublish) {
			manager = PackageManager.MAVEN;
		}

		String identifier = "";
		try {
			identifier = this.getMonikerIdentifier(element);
		} catch (JavaModelException e) {
			// Do nothing
		}
		/* Generate Moniker */
		if (StringUtils.isNotEmpty(identifier) && StringUtils.isNotEmpty(packageName)
				&& StringUtils.isNotEmpty(version)) {
			if (monikerKind == MonikerKind.EXPORT) {
				symbolData.generateMonikerExport(lsif, sourceRange, identifier, manager, javaProject);
			} else if (monikerKind == MonikerKind.LOCAL) {
				symbolData.generateMonikerLocal(lsif, sourceRange, identifier);
			} else if (definitionLocation.getUri().startsWith("jdt")) {
				symbolData.generateMonikerImport(lsif, sourceRange, identifier, packageName, manager, version, type, url);
			}
		}
	}

	private String getMonikerIdentifier(IJavaElement element) throws JavaModelException {
		String identifier = element.getElementName();
		if (element instanceof IType) {
			return ((IType) element).getFullyQualifiedName();
		} else if (element instanceof IField || element instanceof ILocalVariable) {
			return getMonikerIdentifier(element.getParent()) + "/" + identifier;
		} else if (element instanceof IMethod) {
			return getMonikerIdentifier(element.getParent()) + "/" + identifier + ":"
					+ ((IMethod) element).getSignature();
		}
		return identifier;
	}

	private MonikerKind getMonikerKind(BodyDeclaration node) {
		return (node.getModifiers() & Modifier.PUBLIC) > 0 ? MonikerKind.EXPORT : MonikerKind.LOCAL;
	}

	private MonikerKind getMonikerKind(SingleVariableDeclaration node) {
		return (node.getModifiers() & Modifier.PUBLIC) > 0 ? MonikerKind.EXPORT : MonikerKind.LOCAL;
	}

	private MonikerKind getMonikerKind(VariableDeclarationStatement node) {
		return (node.getModifiers() & Modifier.PUBLIC) > 0 ? MonikerKind.EXPORT : MonikerKind.LOCAL;
	}

	private boolean isTypeOrMethodDeclaration(ASTNode node) {
		return node.getParent() instanceof TypeDeclaration || node.getParent() instanceof MethodDeclaration;
	}

	public static File findPom(IPath jarPath, int length) {
		int current = 0;
		Set<File> exclude = new HashSet<>();
		while (current <= length) {
			File pomFile = findPomFromFolder(jarPath.removeLastSegments(current).toFile(), exclude);
			if (pomFile != null) {
				return pomFile;
			}
			current++;
		}
		return null;
	}

	public static File findPomFromFolder(File folder, Set<File> exclude) {
		if (!folder.isDirectory() || exclude.contains(folder)) {
			return null;
		}
		exclude.add(folder);
		List<File> subFolders = new ArrayList<>();
		for (File file : folder.listFiles()) {
			if (file.getName().endsWith(".pom") || StringUtils.equals(file.getName(), "pom.xml")) {
				return file;
			} else if (file.isDirectory()) {
				subFolders.add(file);
			}
		}
		for (File subFolder : subFolders) {
			File pomFile = findPomFromFolder(subFolder, exclude);
			if (pomFile != null) {
				return pomFile;
			}
		}
		return null;
	}
}
