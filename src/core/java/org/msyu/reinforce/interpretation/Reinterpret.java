package org.msyu.reinforce.interpretation;

import java.util.List;

public class Reinterpret {

	public static boolean checkInterpretationSpecValidity(Object interpretationSpec) {
		if (interpretationSpec == null) {
			return false;
		} else if (interpretationSpec instanceof String) {
			return true;
		} else if (interpretationSpec instanceof List) {
			for (Object subSpec : ((List) interpretationSpec)) {
				if (!checkInterpretationSpecValidity(subSpec)) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public static Object reinterpret(Object object, Object interpretationSpec) throws ReinterpretationException {
		if (interpretationSpec instanceof String) {
			return reinterpretString(object, (String) interpretationSpec);
		} else if (interpretationSpec instanceof List) {
			return reinterpretList(object, (List) interpretationSpec);
		} else {
			throw new InvalidInterpretationSpecException(interpretationSpec);
		}
	}


	private static Object reinterpretString(Object object, String interpretationSpec) throws ReinterpretationException {
		try {
			return reinterpretOnce(object, interpretationSpec);
		} catch (UnknownInterpretationException firstException) {
			Object uninterpretedObject;
			while (true) {
				uninterpretedObject = object;
				object = reinterpretOnce(object, Reinterpretable.DEFAULT_INTERPRETATION_SPEC);
				if (object == uninterpretedObject) {
					break;
				}
				try {
					return reinterpretOnce(object, interpretationSpec);
				} catch (UnknownInterpretationException uie) {
					firstException.addSuppressed(uie);
				} catch (ReinterpretationException re) {
					firstException.addSuppressed(re);
					break;
				}
			}
			throw firstException;
		}
	}

	private static Object reinterpretOnce(Object object, String interpretationSpec) throws ReinterpretationException {
		if (object instanceof Reinterpretable) {
			return ((Reinterpretable) object).reinterpret(interpretationSpec);
		} else {
			throw new NonReinterpretableObjectException(object);
		}
	}


	private static Object reinterpretList(Object object, List interpretationSpecs) throws ReinterpretationException {
		for (Object interpretationSpec : interpretationSpecs) {
			object = reinterpret(object, interpretationSpec);
		}
		return object;
	}

}
