/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.command;

import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.transform.DrillExpandPosition;
import com.eyeq.pivot4j.ui.PivotUIRenderer;
import com.eyeq.pivot4j.ui.RenderContext;

public class DrillCollapsePositionCommand extends AbstractDrillDownCommand {

	public static final String NAME = "collapsePosition";

	/**
	 * @param renderer
	 */
	public DrillCollapsePositionCommand(PivotUIRenderer renderer) {
		super(renderer);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.AbstractCellCommand#getMode(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public String getMode(RenderContext context) {
		return MODE_POSITION;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#canExecute(com.eyeq.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public boolean canExecute(RenderContext context) {
		if (!super.canExecute(context)) {
			return false;
		}

		PivotModel model = context.getModel();

		DrillExpandPosition transform = model
				.getTransform(DrillExpandPosition.class);

		Position position = context.getPosition();
		Member member = context.getMember();

		if (position == null || member == null) {
			return false;
		}

		return transform.canCollapse(position, member);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#createParameters(com.eyeq.pivot4j
	 *      .ui.RenderContext)
	 */
	@Override
	public CellParameters createParameters(RenderContext context) {
		CellParameters parameters = new CellParameters();
		parameters.setAxisOrdinal(context.getAxis().axisOrdinal());
		parameters.setPositionOrdinal(context.getPosition().getOrdinal());
		parameters.setMemberOrdinal(context.getPosition().getMembers()
				.indexOf(context.getMember()));

		return parameters;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#execute(com.eyeq.pivot4j.PivotModel
	 *      , com.eyeq.pivot4j.ui.command.CellParameters)
	 */
	@Override
	public Void execute(PivotModel model, CellParameters parameters) {
		CellSet cellSet = model.getCellSet();

		CellSetAxis axis = cellSet.getAxes().get(parameters.getAxisOrdinal());
		Position position = axis.getPositions().get(
				parameters.getPositionOrdinal());

		Member member = position.getMembers()
				.get(parameters.getMemberOrdinal());

		DrillExpandPosition transform = model
				.getTransform(DrillExpandPosition.class);
		transform.collapse(position, member);

		return null;
	}
}
