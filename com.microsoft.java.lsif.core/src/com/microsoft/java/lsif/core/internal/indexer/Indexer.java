package com.microsoft.java.lsif.core.internal.indexer;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.IASTSharedValues;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;

import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.protocol.JavaLsif;

public class Indexer {

	private WorkspaceHandler handler;

	public Indexer() {
		this.handler = new WorkspaceHandler(System.getProperty("intellinav.repo.path"));
	}

	public void buildModel() {

		NullProgressMonitor monitor = new NullProgressMonitor();

		List<IPath> projectRoots = this.handler.initialize();

		for (IPath path : projectRoots) {
			final JavaLsif resultData = new JavaLsif();
			try {
				LanguageServerIndexerPlugin.logInfo("Starting analysis project: " + path.toPortableString());
				handler.importProject(path, monitor);
				handler.buildProject(monitor);
				buildIndex(path, monitor, resultData);
				handler.removeProject(path, monitor);
				JavaLanguageServerPlugin.logInfo("End analysis project: " + path.toPortableString());
			} catch (Exception ex) {
				// ignore it
			} finally {
				// Output model
//				final String resultFilename = String.format("%s_result.json", outputPrefix);
//				try {
//					FileUtils.writeStringToFile(outputBasePath.resolve(resultFilename).toFile(),
//							new Gson().toJson(resultData));
//				} catch (IOException e) {
//				}
			}
		}


	}

	private void buildIndex(IPath path, IProgressMonitor monitor, JavaLsif lsif) {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		LanguageServerIndexerPlugin.logInfo(String.format("collectModel, projects # = %d", projects.length));

		for (IProject proj : projects) {
			if (proj == null) {
				return;
			}

			IJavaProject javaProject = JavaCore.create(proj);
			try {
				IClasspathEntry[] references = javaProject.getRawClasspath();
				for (IClasspathEntry reference : references) {
					if (reference.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						IPackageFragmentRoot[] fragmentRoots = javaProject.getPackageFragmentRoots(reference);
						for (IPackageFragmentRoot fragmentRoot : fragmentRoots) {
							for (IJavaElement child : fragmentRoot.getChildren()) {
								IPackageFragment fragment = (IPackageFragment) child;
								if (fragment.hasChildren()) {
									for (IJavaElement sourceFile : fragment.getChildren()) {
										CompilationUnit cu = createAST((ITypeRoot) sourceFile, monitor);
										parseDocument(cu);
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void parseDocument(CompilationUnit cu) {
		List result = cu.types();
		if (result.size() == 0 || !(result.get(0) instanceof TypeDeclaration)) {
			return;
		}
		TypeDeclaration declartion = (TypeDeclaration) result.get(0);

		declartion.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodInvocation node) {
				IMethodBinding methodBinding = node.resolveMethodBinding();
				if (methodBinding == null) {
					return false;
				}

				ITypeBinding declClazz = methodBinding.getDeclaringClass();
				if (declClazz == null) {
					return false;
				}

				return true;
			}

			@Override
			public boolean visit(QualifiedName node) {
				IBinding binding = node.resolveBinding();
				if (binding == null || !(binding instanceof IVariableBinding)) {
					return false;
				}

				ITypeBinding declClazz = ((IVariableBinding) binding).getDeclaringClass();
				if (declClazz == null) {
					return false;
				}

				return true;
			}

		});

		return;
	}

	/**
	 * Creates a new compilation unit AST.
	 *
	 * @param input the Java element for which to create the AST
	 * @param progressMonitor the progress monitor
	 * @return AST
	 */
	private static CompilationUnit createAST(final ITypeRoot input, final IProgressMonitor progressMonitor) {
		if (progressMonitor != null && progressMonitor.isCanceled()) {
			return null;
		}

		final CompilationUnit root[] = new CompilationUnit[1];

		SafeRunner.run(new ISafeRunnable() {
			@Override
			public void run() {
				try {
					if (progressMonitor != null && progressMonitor.isCanceled()) {
						return;
					}
					if (input instanceof ICompilationUnit) {
						ICompilationUnit cu = (ICompilationUnit) input;
						if (cu.isWorkingCopy()) {
							root[0] = cu.reconcile(IASTSharedValues.SHARED_AST_LEVEL, true, null, progressMonitor);
						}
					}
					if (root[0] == null) {
						final ASTParser parser = newASTParser();
						parser.setSource(input);
						root[0] = (CompilationUnit) parser.createAST(progressMonitor);
					}
					// mark as unmodifiable
					ASTNodes.setFlagsToAST(root[0], ASTNode.PROTECT);
				} catch (OperationCanceledException ex) {
					return;
				} catch (JavaModelException e) {
					JavaLanguageServerPlugin.logException(e.getMessage(), e);
					return;
				}
			}

			@Override
			public void handleException(Throwable ex) {
				IStatus status = new Status(IStatus.ERROR, JavaLanguageServerPlugin.PLUGIN_ID, IStatus.OK,
						"Error in JDT Core during AST creation", ex); //$NON-NLS-1$
				JavaLanguageServerPlugin.log(status);
			}
		});
		return root[0];
	}

	public static ASTParser newASTParser() {
		final ASTParser parser = ASTParser.newParser(IASTSharedValues.SHARED_AST_LEVEL);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(IASTSharedValues.SHARED_AST_STATEMENT_RECOVERY);
		parser.setBindingsRecovery(IASTSharedValues.SHARED_BINDING_RECOVERY);
		return parser;
	}

}