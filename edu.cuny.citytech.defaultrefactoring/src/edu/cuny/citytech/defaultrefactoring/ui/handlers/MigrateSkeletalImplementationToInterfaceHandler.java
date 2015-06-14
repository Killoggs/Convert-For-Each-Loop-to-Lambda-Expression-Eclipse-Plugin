package edu.cuny.citytech.defaultrefactoring.ui.handlers;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaElementUtil;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.util.SelectionUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;

import edu.cuny.citytech.defaultrefactoring.ui.wizards.MigrateSkeletalImplementationToInterfaceRefactoringWizard;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class MigrateSkeletalImplementationToInterfaceHandler extends
		AbstractHandler {

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection currentSelection = HandlerUtil
				.getCurrentSelectionChecked(event);

		List<?> list = SelectionUtil.toList(currentSelection);
		IType[] types = (IType[]) list.stream().filter(e -> e instanceof IType)
				.toArray();

		if (types.length > 0) {
			Shell shell = HandlerUtil.getActiveShellChecked(event);
			MigrateSkeletalImplementationToInterfaceRefactoringWizard
					.startRefactoring(types, shell);
		}

		return null;
	}
}
