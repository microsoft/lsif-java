/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
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
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.ResolvedBinaryField;
import org.eclipse.jdt.internal.core.ResolvedBinaryMethod;
import org.eclipse.jdt.internal.core.ResolvedBinaryType;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceType;
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
import com.microsoft.java.lsif.core.internal.protocol.Project;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;

public class LsifVisitor extends ProtocolVisitor {

	public LsifVisitor(LsifService lsif, IndexerContext context) {
		super(lsif, context);
	}

	@Override
	public boolean visit(SimpleName node) {
		resolve(node.getStartPosition(), node.getLength(), isTypeOrMethodDeclaration(node), true, "");
		return false;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		String monikerKind = (node.getModifiers() & Modifier.PUBLIC) > 0 ? "export" : "local";
		resolve(node.getName().getStartPosition(), node.getName().getLength(), false, true, monikerKind);
		return true;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		String monikerKind = (node.getModifiers() & Modifier.PUBLIC) > 0 ? "export" : "local";
		resolve(node.getName().getStartPosition(), node.getName().getLength(), false, true, monikerKind);
		return true;
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		String monikerKind = "export"; // all the enum values are public static final
		resolve(node.getName().getStartPosition(), node.getName().getLength(), false, true, monikerKind);
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		String monikerKind = (node.getModifiers() & Modifier.PUBLIC) > 0 ? "export" : "local";
		resolve(node.getName().getStartPosition(), node.getName().getLength(), false, true, monikerKind);
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		String monikerKind = (node.getModifiers() & Modifier.PUBLIC) > 0 ? "export" : "local";
		resolve(node.getName().getStartPosition(), node.getName().getLength(), false, true, monikerKind);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		ASTNode parent = node.getParent();
		if (parent instanceof VariableDeclarationStatement) {
			String monikerKind = (((VariableDeclarationStatement) parent).getModifiers() & Modifier.PUBLIC) > 0
					? "export"
					: "local";
			resolve(node.getName().getStartPosition(), node.getName().getLength(), false, true, monikerKind);
		} else if (parent instanceof FieldDeclaration) {
			String monikerKind = (((FieldDeclaration) parent).getModifiers() & Modifier.PUBLIC) > 0 ? "export"
					: "local";
			resolve(node.getName().getStartPosition(), node.getName().getLength(), false, true, monikerKind);
		}
		return true;
	}

	@Override
	public boolean visit(SimpleType node) {
		resolve(node.getStartPosition(), node.getLength(), isTypeOrMethodDeclaration(node), true, "");
		return false;
	}

	private void resolve(int startPosition, int length, boolean needResolveImpl, boolean hasMoniker,
			String monikerKind) {
		try {
			org.eclipse.lsp4j.Range sourceLspRange = JDTUtils.toRange(this.getContext().getCompilationUnit().getTypeRoot(),
					startPosition, length);

			IJavaElement element = JDTUtils.findElementAtSelection(this.getContext().getCompilationUnit().getTypeRoot(),
					sourceLspRange.getStart().getLine(), sourceLspRange.getStart().getCharacter(), new PreferenceManager(),
					new NullProgressMonitor());
			if (element == null) {
				return;
			}


			LsifService lsif = this.getLsif();
			Document docVertex = this.getContext().getDocVertex();
			Project projVertex = this.getContext().getProjVertex();
			Range sourceRange = Repository.getInstance().enlistRange(lsif, docVertex, sourceLspRange);
			// emit range
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
				// emit hover
				return;
			}

			IJavaProject javaproject = element.getJavaProject();
			ICompilationUnit compilationUnit = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
			IClassFile cf = (IClassFile) element.getAncestor(IJavaElement.CLASS_FILE);
			boolean isFromMaven = false;
			int mavenCount = 0;
			String groupId;
			String artifactId;
			String version;
			if (compilationUnit == null && cf != null) {
				IPath path = cf.getPath();
				IClasspathEntry entry = javaproject.getClasspathEntryFor(path);
				IClasspathAttribute[] attrs = entry.getExtraAttributes();
				for (IClasspathAttribute attr : attrs) {
					if (attr.getName().equals("maven.pomderived")) {
						if (attr.getValue().equals("true")) {
							mavenCount++;
						}
					} else if (attr.getName().equals("maven.groupId")) {
						groupId = attr.getValue();
					} else if (attr.getName().equals("maven.artifactId")) {
						artifactId = attr.getValue();
					} else if (attr.getName().equals("maven.version")) {
						version = attr.getValue();
					}
				}
				if (mavenCount == 2) {
					isFromMaven = true;
				}
			} /*
				 * else { IPath path = compilationUnit.getPath(); IClasspathEntry entry =
				 * javaproject.getClasspathEntryFor(path); int test = 1; }
				 */

			String id = createSymbolKey(definitionLocation);
			Document definitionDocument = Repository.getInstance().enlistDocument(lsif, definitionLocation.getUri(),
					projVertex);
			SymbolData symbolData = Repository.getInstance().enlistSymbolData(id, definitionDocument, projVertex);

			/* Ensure resultSet */
			symbolData.ensureResultSet(lsif, sourceRange);
			if (hasMoniker) {
				String identifier = "";
				try {
					identifier = this.getMonikerIdentifier(element);
				} catch (JavaModelException e) {

				}
			/* Generate Moniker */
				if (monikerKind.equals("export")) {
					symbolData.generateMoniker(lsif, sourceRange, "export", identifier);
				} else if (monikerKind.equals("local")) {
					symbolData.generateMoniker(lsif, sourceRange, "local", identifier);
				} else if (definitionLocation.getUri().startsWith("jdt")) {
					symbolData.generateMoniker(lsif, sourceRange, "import", identifier);
				}
				return;
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
		} catch (Throwable ex) {
			LanguageServerIndexerPlugin.logException("Exception when dumping definition information ", ex);
		}
	}

	private String getMonikerIdentifier(IJavaElement element) throws JavaModelException {
		String identifier = element.getElementName();
		if (element instanceof SourceType) {
			return ((SourceType) element).getFullyQualifiedName();
		} else if (element instanceof BinaryType) {
			return ((BinaryType) element).getFullyQualifiedName();
		} else if (element instanceof SourceField) {
			return getMonikerIdentifier(element.getParent()) + "/" + identifier;
		} else if (element instanceof SourceMethod) {
			return getMonikerIdentifier(element.getParent()) + "/"
					+ identifier + ":" + ((SourceMethod) element).getSignature();
		} else if (element instanceof LocalVariable) {
			return getMonikerIdentifier(element.getParent()) + "/" + identifier;
		} else if (element instanceof ResolvedBinaryType) {
			return ((ResolvedBinaryType) element).getFullyQualifiedName();
		} else if (element instanceof ResolvedBinaryMethod) {
			return getMonikerIdentifier(element.getParent()) + "/"
					+ identifier + ":" + ((ResolvedBinaryMethod) element).getSignature();
		} else if (element instanceof ResolvedBinaryField) {
			return getMonikerIdentifier(element.getParent()) + "/" + identifier;
		}
		return identifier;
	}

	private String createSymbolKey(Location definitionLocation) {
		String rawKeyString = definitionLocation.toString();
		return DigestUtils.md5Hex(rawKeyString);
	}

	private boolean isTypeOrMethodDeclaration(ASTNode node) {
		return node.getParent() instanceof TypeDeclaration || node.getParent() instanceof MethodDeclaration;
	}
}
