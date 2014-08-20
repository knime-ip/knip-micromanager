package org.knime.knip.mm.loops;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class MMLoopEndNodeFactory extends NodeFactory<MMLoopEndNodeModel> {

	@Override
	public MMLoopEndNodeModel createNodeModel() {
		return new MMLoopEndNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<MMLoopEndNodeModel> createNodeView(final int viewIndex,
			final MMLoopEndNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new MMLoopEndNodeDialog();
	}

}
