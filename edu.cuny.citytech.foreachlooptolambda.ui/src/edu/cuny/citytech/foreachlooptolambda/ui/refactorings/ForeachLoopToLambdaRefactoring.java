package edu.cuny.citytech.foreachlooptolambda.ui.refactorings;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.codeassist.ThrownExceptionFinder;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import edu.cuny.citytech.foreachlooptolambda.ui.messages.Messages;
import edu.cuny.citytech.foreachlooptolambda.ui.visitors.EnhancedForStatementVisitor;
import edu.cuny.citytech.refactoring.common.core.Refactoring;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author <a href="mailto:rkhatchadourian@citytech.cuny.edu">Raffi
 *         Khatchadourian</a>
 */
public class ForeachLoopToLambdaRefactoring extends Refactoring {

	/**
	 * The methods to refactor.
	 */
	private Set<IMethod> methods;

	/**
	 * Creates a new refactoring with the given methods to refactor.
	 * 
	 * @param methods
	 *            The methods to refactor.
	 */
	public ForeachLoopToLambdaRefactoring(IMethod... methods) {
		this.methods = new HashSet<IMethod>(Arrays.asList(methods));
	}

	/**
	 * Default constructor
	 */
	public ForeachLoopToLambdaRefactoring() {
	}

	@Override
	public String getName() {
		// TODO: Please rename.
		return Messages.ForEachLoopToLambdaRefactoring_Name;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		// TODO Empty for now.
		final RefactoringStatus status = new RefactoringStatus();
		return status;
	}

	// this method get the EnhancedForSrarement to check the precondition
	private static Set<EnhancedForStatement> getEnhancedForStatements(
			IMethod method, IProgressMonitor pm) throws JavaModelException {
		ICompilationUnit iCompilationUnit = method.getCompilationUnit();

		// there may be a shared AST already parsed. Let's try to get that
		// one.
		CompilationUnit compilationUnit = RefactoringASTParser
				.parseWithASTProvider(iCompilationUnit, true,
						new SubProgressMonitor(pm, 1));

		// get the method declaration ASTNode.
		MethodDeclaration methodDeclarationNode = ASTNodeSearchUtil
				.getMethodDeclarationNode(method, compilationUnit);

		final Set<EnhancedForStatement> statements = new LinkedHashSet<EnhancedForStatement>();
		// extract all enhanced for loop statements in the method.
		methodDeclarationNode.accept(new ASTVisitor() {

			@Override
			public boolean visit(EnhancedForStatement node) {
				statements.add(node);
				return super.visit(node);
			}
		});

		return statements;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		try {
			final RefactoringStatus status = new RefactoringStatus();
			for (IMethod method : methods) {
				Set<EnhancedForStatement> statements = getEnhancedForStatements(
						method, new SubProgressMonitor(pm, 1));

				IProgressMonitor subMonitor = new SubProgressMonitor(pm,
						statements.size());

				// check preconditions on each.
				statements.stream().forEach(
						s -> status.merge(checkEnhancedForStatement(s,
								subMonitor)));
				pm.worked(1);
			}
			return status;
		} finally {
			pm.done();
		}
	}

	// Checking if the EnhancedForLoop iterate over collection
	private static boolean checkEnhancedForStatementIteratesOverCollection(
			EnhancedForStatement enhancedForStatement, IProgressMonitor pm) {
		boolean isNotInstanceOfCollection = true;

		Expression expression = enhancedForStatement.getExpression();
		ITypeBinding nodeBindingType = expression.resolveTypeBinding();
		
		//getting java IJavaElement
		IJavaElement nodeElementType = nodeBindingType.getJavaElement();
		
		//ITypeHierarchy superType and getting the interface from them.
		ITypeHierarchy iTypeHeirchay;
		try {
			iTypeHeirchay =  ((IType) nodeElementType).newSupertypeHierarchy(pm);//giving null pointer exception
			IType[] iType = iTypeHeirchay.getAllInterfaces();

		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		

		String fullTypeName = nodeBindingType.getQualifiedName();
		String typeName = fullTypeName.split("<")[0];
		
		final Set<String> collectionsClassName = new HashSet<String>();

		collectionsClassName.add("java.util.Collection");
		collectionsClassName.add("java.util.List");
		collectionsClassName.add("java.util.LinkedList");
		collectionsClassName.add("java.util.LinkedHashSet");
		collectionsClassName.add("java.util.Set");
		collectionsClassName.add("java.util.HashSet");
		collectionsClassName.add("java.util.Map");
		collectionsClassName.add("java.util.HashMap");

		for (String name : collectionsClassName) {
			if ((typeName.equals(name))) {
				isNotInstanceOfCollection = false;
				break;
			}
		}
		
		return isNotInstanceOfCollection;
	}

	// getting any uncaught exception
	public void checkException() {
		ThrownExceptionFinder thrownUncaughtExceptions = new ThrownExceptionFinder();
		ReferenceBinding[] thrownUncaughtException = thrownUncaughtExceptions
				.getThrownUncaughtExceptions();
		if (thrownUncaughtException.length > 0) {

		}
	}

	// Checking with the precondiiton,
	private static RefactoringStatus checkEnhancedForStatement(
			EnhancedForStatement enhancedForStatement, IProgressMonitor pm) {
		try {
			// create the visitor.
			EnhancedForStatementVisitor visitor = new EnhancedForStatementVisitor();

			// have the AST node "accept" the visitor.
			enhancedForStatement.accept(visitor);

			if (visitor.containsBreak()
					|| visitor.containsContinue()
					|| visitor.containsInvalidReturn()
					|| visitor.containsMultipleReturn()
					|| visitor.containsException()
					|| checkEnhancedForStatementIteratesOverCollection(
							enhancedForStatement, pm)) {
				final Set<String> warningStatement = new HashSet<String>();
				if (visitor.containsBreak()) {
					warningStatement
							.add("Enhanced for statement contains break.");
				}

				if (visitor.containsContinue()) {
					warningStatement
							.add("Enhanced for statement contains continue.");
				}

				if (visitor.containsInvalidReturn()) {
					warningStatement
							.add("Enhanced for statement contains invalid return.");
				}

				if (visitor.containsMultipleReturn()) {
					warningStatement
							.add("Enhanced for statement contains multiple return.");
				}

				if (visitor.containsException()) {
					warningStatement
							.add("Enhanced for statement contains Exception.");
				}

				if (checkEnhancedForStatementIteratesOverCollection(
						enhancedForStatement, pm)) {
					warningStatement
							.add("Enhanced for statement doesn't iterate over collecitons.");
				}
				
				String warning = String.join(" ", warningStatement);
				return RefactoringStatus.createWarningStatus(warning);
			}
			pm.worked(1);
			return new RefactoringStatus(); // passed.
		} finally {
			pm.done();
		}
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {

		try {
			pm.beginTask(
					Messages.ForEachLoopToLambdaRefactoring_CreatingChange, 1);

			return new NullChange(getName());
		} finally {
			pm.done();
		}
	}
}
