package edu.cuny.citytech.foreachlooptolambda.ui.visitors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;

public class EnhancedForStatementVisitor extends ASTVisitor {

	private boolean encounteredBreakStatement;
	private boolean encounteredContinueStatement;
	private boolean encounteredInvalidReturnStatement;
	private boolean encounteredThrownCheckedException;
	private boolean encounteredNonEffectivelyFinalVars;
	private int returnCount = 0;

	/**
	 * The enhanced for statement that will be visited.
	 */
	private EnhancedForStatement enhancedForStatement;
	
	private IProgressMonitor monitor;

	/**
	 * Create a new visitor.
	 * 
	 * @param enhancedForStatement
	 *            The enhanced for statement that will be visited.
	 */
	public EnhancedForStatementVisitor(EnhancedForStatement enhancedForStatement, IProgressMonitor monitor) {
		this.enhancedForStatement = enhancedForStatement;
		this.monitor = monitor;
	}

	// finding the TryStatement node
	public static ASTNode findTryAncestor(ASTNode node) {
		if (node == null || node instanceof TryStatement) {
			return node;
		}
		return findTryAncestor(node);
	}

	@Override
	public boolean visit(BreakStatement node) {
		this.encounteredBreakStatement = true;
		return super.visit(node);
	}

	@Override
	public boolean visit(ContinueStatement node) {
		this.encounteredContinueStatement = true;
		return super.visit(node);
	}

	/**
	 * @param nodeContaingException The node that throws an exception.
	 * @param exceptionTypes The list of exceptions being thrown.
	 * @throws JavaModelException 
	 */
	private void handleException(ASTNode nodeContaingException, ITypeBinding... exceptionTypes) throws JavaModelException {
		Set<ITypeBinding> thrownExceptionTypeSet = new HashSet<ITypeBinding>(Arrays.asList(exceptionTypes));
		
		// gets the top node. If it returns
		// null, there is no other top.
		ASTNode tryStatementParent = (nodeContaingException.getParent()).getParent();
		ASTNode throwStatementParent = tryStatementParent.getParent();

		if (throwStatementParent instanceof TryStatement) {
			System.out.println("this is throwStatementParent");
			this.encounteredThrownCheckedException = false;
		}

		// findTryStatmaent if there is any catch block
		else if (tryStatementParent instanceof TryStatement) {

			TryStatement tryStatement = (TryStatement) tryStatementParent;
			@SuppressWarnings("unchecked")
			List<CatchClause> catchClauses = tryStatement.catchClauses();
			
			Stream<SingleVariableDeclaration> catchVarDeclStream = catchClauses.stream().map(CatchClause::getException);
			Stream<Type> caughtExceptionTypeNodeStream = catchVarDeclStream.map(SingleVariableDeclaration::getType);
			Stream<ITypeBinding> caughtExceptionTypeBindingStream = caughtExceptionTypeNodeStream.map(Type::resolveBinding);
			Stream<IJavaElement> caughtExceptionTypeJavaStream = caughtExceptionTypeBindingStream.map(ITypeBinding::getJavaElement);
			
			//for each thrown exception type, check if it is a subtype of the caught exceptions types.
			for (ITypeBinding te : thrownExceptionTypeSet) {
				IType javaType = (IType) te.getJavaElement();
				ITypeHierarchy supertypeHierarchy = javaType.newSupertypeHierarchy(monitor);
				this.encounteredThrownCheckedException = !caughtExceptionTypeJavaStream.anyMatch(t -> supertypeHierarchy.contains((IType) t));
			}
		} else {
			this.encounteredThrownCheckedException = true;
		}
	}

	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding iMethodBinding = node.resolveMethodBinding();
		ITypeBinding[] exceptionTypes = iMethodBinding.getExceptionTypes();
		// if there are exceptions
		if (exceptionTypes.length >= 1) {
			try {
				handleException(node, exceptionTypes);
			} catch (JavaModelException e) {
				throw new RuntimeException(e);
			}
		}

		return super.visit(node);
	}

	@Override
	public boolean visit(ThrowStatement node) {
		ITypeBinding thrownExceptionType = node.getExpression().resolveTypeBinding();
		try {
			handleException(node, thrownExceptionType);
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}

		return super.visit(node);
	}

	/**
	 * checking if returnStatement is boolean, not null and has only one return
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ReturnStatement)
	 */
	@Override
	public boolean visit(ReturnStatement node) {
		// one more return statement encountered.
		returnCount++;

		// examine what is being returned.
		ASTNode expression = node.getExpression();

		// if there is a return statement, it must return a boolean literal.
		if (expression == null || !(expression instanceof BooleanLiteral)) {
			this.encounteredInvalidReturnStatement = true;
		}

		return super.visit(node);
	}

	public boolean containsBreak() {
		return this.encounteredBreakStatement;
	}

	public boolean containsContinue() {
		return encounteredContinueStatement;
	}

	public boolean containsInvalidReturn() {
		return encounteredInvalidReturnStatement;
	}

	public boolean containsMultipleReturn() {
		return returnCount > 1;
	}

	public boolean containsException() {
		return encounteredThrownCheckedException;
	}

	public boolean containsNEFS() {
		return encounteredNonEffectivelyFinalVars;
	}
}