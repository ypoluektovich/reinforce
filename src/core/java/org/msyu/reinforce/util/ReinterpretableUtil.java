package org.msyu.reinforce.util;

import org.msyu.reinforce.Reinterpretable;
import org.msyu.reinforce.ReinterpretationException;

import java.util.List;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class ReinterpretableUtil {

	public static Object reinterpret(Object object, Object interpretationSpec) throws ReinterpretationException {
		if (interpretationSpec instanceof String) {
			return reinterpretOnce(object, (String) interpretationSpec);
		} else if (interpretationSpec instanceof List) {
			return reinterpretList(object, (List) interpretationSpec);
		} else {
			throw new ReinterpretationException("reinterpretation specification must be a string or a list of specifications");
		}
	}

	private static Object reinterpretOnce(Object object, String interpretationSpec) throws ReinterpretationException {
		if (object instanceof Reinterpretable) {
			return ((Reinterpretable) object).reinterpret(interpretationSpec);
		} else {
			throw new ReinterpretationException("can't reinterpret non-Reinterpretable item: " + object);
		}
	}

	private static Object reinterpretList(Object object, List interpretationSpecs) throws ReinterpretationException {
		for (Object interpretationSpec : interpretationSpecs) {
			object = reinterpret(object, interpretationSpec);
		}
		return object;
	}

}
