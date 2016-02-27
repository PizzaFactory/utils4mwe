package jp.pizzafactory.mwe.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.emf.codegen.merge.java.JControlModel;
import org.eclipse.emf.codegen.merge.java.JMerger;
import org.eclipse.emf.codegen.merge.java.facade.ast.ASTFacadeHelper;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.lib.AbstractWorkflowComponent;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.eclipse.osgi.util.NLS;

public class JMerge extends AbstractWorkflowComponent {

	private String sourceFile = null;
	private String targetFile = null;
	private String compilerCompliance = "1.5";

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}
	
	public void setTargetFile(String targetFile) {
		this.targetFile = targetFile;
	}
	
	public void setCompilerCompliance(String compilerCompliance) {
		this.compilerCompliance = compilerCompliance;
	}

	@Override
	public void checkConfiguration(Issues issues) {
		if (sourceFile == null) {
			issues.addError("No sourceFile set.");
		}
		if (targetFile == null) {
			issues.addError("No targetFile set.");
		}
	}

	@Override
	protected void invokeInternal(WorkflowContext ctx, ProgressMonitor monitor,
			Issues issues) {
		try {
			File source = new File(sourceFile);
			File target = new File(targetFile);
			
			if (!source.exists()) {
				issues.addWarning(this, NLS.bind("Source file {0} does not exist. Skipping.", sourceFile));
				return;
			}
			if (!target.exists()) {
				issues.addWarning(this, NLS.bind("Target file {0} does not exist. Skipping.", targetFile));
				return;
			}
			
			JMerger merger = new JMerger(getControlModel());
			
			FileInputStream sourceFis = new FileInputStream(source);
			merger.setTargetCompilationUnit(merger.createCompilationUnitForInputStream(sourceFis));
			sourceFis.close();
			FileInputStream targetFis = new FileInputStream(target);
			merger.setSourceCompilationUnit(merger.createCompilationUnitForInputStream(targetFis));
			targetFis.close();
			merger.merge();
			String result = merger.getTargetCompilationUnitContents();
			FileWriter writer = new FileWriter(target);
			writer.write(result);
			writer.close();
		} catch (IOException e) {
			issues.addError(this, "Cannot perform I/O", e);
		}
	}

	private JControlModel getControlModel() {
		JControlModel model = new JControlModel();
		String configFileUri = JMerge.class.getResource("emf-merge.xml").toString();
		ASTFacadeHelper helper = new ASTFacadeHelper();
		helper.setCompilerCompliance(compilerCompliance);
		model.initialize(helper, configFileUri);
		return model;
	}
}
