/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.LogFactory;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.OlapException;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.ui.aggregator.Aggregator;
import com.eyeq.pivot4j.ui.aggregator.AggregatorFactory;
import com.eyeq.pivot4j.ui.aggregator.AggregatorPosition;
import com.eyeq.pivot4j.ui.aggregator.DefaultAggregatorFactory;
import com.eyeq.pivot4j.ui.impl.RenderStrategyImpl;

public abstract class AbstractPivotRenderer implements PivotRenderer,
		PivotLayoutCallback {

	private boolean hideSpans = false;

	private boolean showParentMembers = false;

	private boolean showDimensionTitle = true;

	private PropertyCollector propertyCollector;

	private RenderStrategy renderStrategy;

	private AggregatorFactory aggregatorFactory = new DefaultAggregatorFactory();

	private HashMap<AggregatorKey, List<String>> aggregatorNames = new HashMap<AggregatorKey, List<String>>();

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#initialize()
	 */
	public void initialize() {
		this.renderStrategy = createRenderStrategy();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#render(com.eyeq.pivot4j.PivotModel)
	 */
	@Override
	public void render(PivotModel model) {
		if (model == null) {
			throw new NullArgumentException("model");
		}

		if (renderStrategy == null) {
			throw new IllegalStateException("Renderer was not initialized yet.");
		}

		renderStrategy.render(model, this, this);
	}

	/**
	 * @return the hideSpans
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getHideSpans()
	 */
	public boolean getHideSpans() {
		return hideSpans;
	}

	/**
	 * @param hideSpans
	 *            the hideSpans to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setHideSpans(boolean)
	 */
	public void setHideSpans(boolean hideSpans) {
		this.hideSpans = hideSpans;
	}

	/**
	 * @return the showParentMembers
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getShowParentMembers()
	 */
	public boolean getShowParentMembers() {
		return showParentMembers;
	}

	/**
	 * @param showParentMembers
	 *            the showParentMembers to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setShowParentMembers(boolean)
	 */
	public void setShowParentMembers(boolean showParentMembers) {
		this.showParentMembers = showParentMembers;
	}

	/**
	 * @return the showDimensionTitle
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getShowDimensionTitle()
	 */
	public boolean getShowDimensionTitle() {
		return showDimensionTitle;
	}

	/**
	 * @param showDimensionTitle
	 *            the showDimensionTitle to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setShowDimensionTitle(boolean)
	 */
	public void setShowDimensionTitle(boolean showDimensionTitle) {
		this.showDimensionTitle = showDimensionTitle;
	}

	/**
	 * @return the propertyCollector
	 */
	public PropertyCollector getPropertyCollector() {
		return propertyCollector;
	}

	/**
	 * @param propertyCollector
	 *            the propertyCollector to set
	 */
	public void setPropertyCollector(PropertyCollector propertyCollector) {
		this.propertyCollector = propertyCollector;
	}

	/**
	 * @return renderStrategy
	 */
	protected RenderStrategy getRenderStrategy() {
		return renderStrategy;
	}

	/**
	 * @return
	 */
	protected RenderStrategy createRenderStrategy() {
		return new RenderStrategyImpl();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#cellContent(com.eyeq.pivot4j.ui.RenderContext)
	 */
	public void cellContent(RenderContext context) {
		cellContent(context, getCellLabel(context));
	}

	public abstract void cellContent(RenderContext context, String label);

	/**
	 * @param context
	 * @return
	 */
	protected String getCellLabel(RenderContext context) {
		String label;

		Cell cell = context.getCell();

		switch (context.getCellType()) {
		case Header:
			if (context.getProperty() == null) {
				label = context.getMember().getCaption();
			} else {
				try {
					label = context.getMember().getPropertyFormattedValue(
							context.getProperty());
				} catch (OlapException e) {
					throw new PivotException(e);
				}
			}
			break;
		case Title:
			if (context.getProperty() != null) {
				label = context.getProperty().getCaption();
			} else if (context.getLevel() != null) {
				label = context.getLevel().getCaption();
			} else if (context.getHierarchy() != null) {
				label = context.getHierarchy().getCaption();
			} else {
				label = null;
			}
			break;
		case Value:
			if (cell == null) {
				Aggregator aggregator = context.getAggregator();

				if (aggregator == null) {
					label = null;
				} else {
					label = aggregator.getFormattedValue(context);
				}
			} else {
				label = cell.getFormattedValue();
			}

			break;
		case Aggregation:
			Aggregator aggregator = context.getAggregator();

			if (aggregator == null && context.getMember() != null) {
				label = context.getMember().getCaption();
			} else {
				label = aggregator.getLabel(context);
			}

			break;
		case None:
		default:
			label = null;
			break;
		}

		return label;
	}

	/**
	 * @return the aggregatorFactory
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getAggregatorFactory()
	 */
	public AggregatorFactory getAggregatorFactory() {
		return aggregatorFactory;
	}

	/**
	 * @param aggregatorFactory
	 *            the aggregatorFactory to set
	 */
	public void setAggregatorFactory(AggregatorFactory aggregatorFactory) {
		this.aggregatorFactory = aggregatorFactory;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getAggregators(org.olap4j.Axis,
	 *      com.eyeq.pivot4j.ui.aggregator.AggregatorPosition)
	 */
	@Override
	public List<String> getAggregators(Axis axis, AggregatorPosition position) {
		if (axis == null) {
			throw new NullArgumentException("axis");
		}

		if (position == null) {
			throw new NullArgumentException("position");
		}

		List<String> names = aggregatorNames.get(new AggregatorKey(axis,
				position));

		if (names == null) {
			names = Collections.emptyList();
		}

		return names;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#addAggregator(org.olap4j.Axis,
	 *      com.eyeq.pivot4j.ui.aggregator.AggregatorPosition, java.lang.String)
	 */
	@Override
	public void addAggregator(Axis axis, AggregatorPosition position,
			String name) {
		if (axis == null) {
			throw new NullArgumentException("axis");
		}

		if (position == null) {
			throw new NullArgumentException("position");
		}

		if (name == null) {
			throw new NullArgumentException("name");
		}

		AggregatorKey key = new AggregatorKey(axis, position);

		List<String> names = aggregatorNames.get(key);

		if (names == null) {
			names = new ArrayList<String>();
			aggregatorNames.put(key, names);
		}

		if (!names.contains(name)) {
			names.add(name);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#removeAggregator(org.olap4j.Axis,
	 *      com.eyeq.pivot4j.ui.aggregator.AggregatorPosition, java.lang.String)
	 */
	@Override
	public void removeAggregator(Axis axis, AggregatorPosition position,
			String name) {
		if (axis == null) {
			throw new NullArgumentException("axis");
		}

		if (position == null) {
			throw new NullArgumentException("position");
		}

		if (name == null) {
			throw new NullArgumentException("name");
		}

		AggregatorKey key = new AggregatorKey(axis, position);

		List<String> names = aggregatorNames.get(key);

		if (names != null) {
			names.remove(name);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setAggregators(org.olap4j.Axis,
	 *      com.eyeq.pivot4j.ui.aggregator.AggregatorPosition, java.util.List)
	 */
	@Override
	public void setAggregators(Axis axis, AggregatorPosition position,
			List<String> names) {
		if (axis == null) {
			throw new NullArgumentException("axis");
		}

		if (position == null) {
			throw new NullArgumentException("position");
		}

		AggregatorKey key = new AggregatorKey(axis, position);

		if (names == null || names.isEmpty()) {
			aggregatorNames.remove(key);
		} else {
			aggregatorNames.put(key, names);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#swapAxes()
	 */
	@Override
	public void swapAxes() {
		HashMap<AggregatorKey, List<String>> map = new HashMap<AggregatorKey, List<String>>(
				aggregatorNames.size());

		for (AggregatorKey key : aggregatorNames.keySet()) {
			List<String> names = aggregatorNames.get(key);

			if (key.axis == Axis.ROWS) {
				map.put(new AggregatorKey(Axis.COLUMNS, key.position), names);
			} else {
				map.put(new AggregatorKey(Axis.ROWS, key.position), names);
			}
		}

		this.aggregatorNames = map;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#saveState()
	 */
	@Override
	public Serializable saveState() {
		return new Serializable[] { showDimensionTitle, showParentMembers,
				hideSpans, aggregatorNames };
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void restoreState(Serializable state) {
		if (state == null) {
			throw new NullArgumentException("state");
		}

		Serializable[] states = (Serializable[]) state;

		this.showDimensionTitle = (Boolean) states[0];
		this.showParentMembers = (Boolean) states[1];
		this.hideSpans = (Boolean) states[2];
		this.aggregatorNames = (HashMap<AggregatorKey, List<String>>) states[3];

		initialize();
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		if (configuration == null) {
			throw new NullArgumentException("configuration");
		}

		configuration.setDelimiterParsingDisabled(true);

		if (configuration.getLogger() == null) {
			configuration.setLogger(LogFactory.getLog(getClass()));
		}

		configuration.addProperty("render.showDimensionTitle",
				showDimensionTitle);
		configuration
				.addProperty("render.showParentMembers", showParentMembers);
		configuration.addProperty("render.hideSpans", hideSpans);

		if (!aggregatorNames.isEmpty()) {
			int index = 0;

			for (AggregatorKey key : aggregatorNames.keySet()) {
				Axis axis = key.getAxis();
				AggregatorPosition position = key.getPosition();

				List<String> names = aggregatorNames.get(key);

				for (String name : names) {
					configuration
							.addProperty(String.format(
									"render.aggregations.aggregation(%s)",
									index), name);
					configuration
							.addProperty(
									String.format(
											"render.aggregations.aggregation(%s)[@axis]",
											index), axis.name());
					configuration.addProperty(String.format(
							"render.aggregations.aggregation(%s)[@position]",
							index), position.name());

					index++;
				}
			}
		}
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		if (configuration == null) {
			throw new NullArgumentException("configuration");
		}

		this.showDimensionTitle = configuration.getBoolean(
				"render.showDimensionTitle", true);
		this.showParentMembers = configuration.getBoolean(
				"render.showParentMembers", false);
		this.hideSpans = configuration.getBoolean("render.hideSpans", false);

		List<Object> aggregationSettings = configuration
				.getList("render.aggregations.aggregation");

		this.aggregatorNames.clear();

		if (aggregationSettings != null) {
			int index = 0;

			for (Object value : aggregationSettings) {
				Axis axis = Axis.Standard.valueOf(configuration
						.getString(String.format(
								"render.aggregations.aggregation(%s)[@axis]",
								index)));
				AggregatorPosition position = AggregatorPosition
						.valueOf(configuration.getString(String
								.format("render.aggregations.aggregation(%s)[@position]",
										index)));

				AggregatorKey key = new AggregatorKey(axis, position);

				List<String> names = aggregatorNames.get(key);

				if (names == null) {
					names = new ArrayList<String>();
					aggregatorNames.put(key, names);
				}

				if (!names.contains(value)) {
					names.add(value.toString());
				}

				index++;
			}
		}

		initialize();
	}

	static class AggregatorKey implements Serializable {

		private static final long serialVersionUID = 4244611391569825053L;

		Axis axis;

		AggregatorPosition position;

		AggregatorKey() {
		}

		/**
		 * @param axis
		 * @param position
		 */
		AggregatorKey(Axis axis, AggregatorPosition position) {
			if (axis == null) {
				throw new NullArgumentException("axis");
			}

			if (position == null) {
				throw new NullArgumentException("position");
			}

			this.axis = axis;
			this.position = position;
		}

		/**
		 * @return the axis
		 */
		Axis getAxis() {
			return axis;
		}

		/**
		 * @return the position
		 */
		AggregatorPosition getPosition() {
			return position;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(axis).append(position)
					.toHashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}

			if (getClass() != obj.getClass()) {
				return false;
			}

			AggregatorKey otherKey = (AggregatorKey) obj;

			return axis == otherKey.axis && position == otherKey.position;
		}
	}
}
