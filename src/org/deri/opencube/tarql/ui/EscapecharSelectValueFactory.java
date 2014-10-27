package org.deri.opencube.tarql.ui;

import com.fluidops.iwb.ui.configuration.SelectValuesFactory;
import com.google.common.collect.Lists;

import java.util.List;

public class EscapecharSelectValueFactory implements SelectValuesFactory {
	public List<String> getSelectValues() {
		List<String> choices = Lists.newArrayList();

		choices.add("");
		choices.add("UTF-8");
		choices.add("UTF-16");
		choices.add("UTF-16LE");
		choices.add("UTF-16BE");
		choices.add("ISO-8859-1");
		choices.add("ISO-8859-2");
		choices.add("US-ASCII");

		return choices;
	}
}
