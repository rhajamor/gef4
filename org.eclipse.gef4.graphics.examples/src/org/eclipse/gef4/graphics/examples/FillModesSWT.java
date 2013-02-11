package org.eclipse.gef4.graphics.examples;

import org.eclipse.gef4.graphics.swt.SwtGraphics;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class FillModesSWT implements PaintListener {

	public static void main(String[] args) {
		new FillModesSWT(FillModesUtil.TITLE);
	}

	public FillModesSWT(String title) {
		Display display = new Display();

		final Shell shell = new Shell(display, SWT.SHELL_TRIM
				| SWT.DOUBLE_BUFFERED);
		shell.setText(title);
		shell.setBounds(0, 0, FillModesUtil.WIDTH, FillModesUtil.HEIGHT);
		shell.open();
		shell.addPaintListener(this);
		shell.redraw(); // platform independently triggers a PaintEvent

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public void paintControl(PaintEvent e) {
		SwtGraphics g = new SwtGraphics(e.gc);
		FillModesUtil.renderScene(g);
		g.cleanUp();
	}

}