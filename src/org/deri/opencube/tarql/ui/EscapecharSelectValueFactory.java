package org.deri.opencube.tarql.ui;

import com.fluidops.iwb.ui.configuration.SelectValuesFactory;
import com.google.common.collect.Lists;

import java.util.List;

public class EscapecharSelectValueFactory implements SelectValuesFactory {
	public List<String> getSelectValues() {
		List<String> choices = Lists.newArrayList();

		choices.add("None");
		choices.add("\\");

		return choices;
	}
}
