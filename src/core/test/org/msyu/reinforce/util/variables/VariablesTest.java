package org.msyu.reinforce.util.variables;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class VariablesTest {

	private static final Map<String, String> VARIABLES = new HashMap<>();

	static {
		VARIABLES.put("module.name", "core");
	}

	@Test
	public void test() throws VariableSubstitutionException {
		assertEquals(
				"src/core/java",
				Variables.expand(
						"src/${module.name}/java",
						VARIABLES
				)
		);
	}

}
