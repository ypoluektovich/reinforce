package org.msyu.reinforce.target.ivy;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.msyu.reinforce.Build;
import org.msyu.reinforce.BuildException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class IvyRetrieveTarget extends Target {

	public static final String IVY_SETTINGS_XML_KEY = "ivysettings.xml";

	public static final String IVY_XML_KEY = "ivy.xml";

	public static final String SAVE_TO_KEY = "save to";

	private Path myIvySettingsXmlPath;

	private Path myIvyXmlPath;

	private String mySaveTo;

	public IvyRetrieveTarget(String name) {
		super(name);
	}

	@Override
	protected void initTarget(Map docMap, Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
		if (docMap.containsKey(IVY_SETTINGS_XML_KEY)) {
			Object ivySettingsXmlString = docMap.get(IVY_SETTINGS_XML_KEY);
			if (ivySettingsXmlString == null) {
				myIvySettingsXmlPath = null;
				Log.debug("Using default Ivy settings");
			} else if (ivySettingsXmlString instanceof String) {
				myIvySettingsXmlPath = Paths.get((String) ivySettingsXmlString);
				Log.debug("Using Ivy settings from %s", ivySettingsXmlString);
			} else {
				throw new TargetInitializationException("value of '" + IVY_SETTINGS_XML_KEY + "' must be a string, or null to use default settings");
			}
		} else {
			myIvySettingsXmlPath = Paths.get("ivysettings.xml");
			Log.debug("Using Ivy settings from %s", myIvySettingsXmlPath);
		}
		if (docMap.containsKey(IVY_XML_KEY)) {
			Object ivyXmlString = docMap.get(IVY_XML_KEY);
			if (ivyXmlString instanceof String) {
				myIvyXmlPath = Paths.get((String) ivyXmlString);
			} else {
				throw new TargetInitializationException("value of '" + IVY_XML_KEY + "' must be a string");
			}
		} else {
			myIvyXmlPath = Paths.get("ivy.xml");
		}
		if (docMap.containsKey(SAVE_TO_KEY)) {
			Object saveTo = docMap.get(SAVE_TO_KEY);
			if (saveTo instanceof String) {
				mySaveTo = ((String) saveTo);
				// the following mimics the behaviour of standalone mode of Ivy 2.2.0
				if (!mySaveTo.contains("[")) {
					mySaveTo += "/lib/[conf]/[artifact].[ext]";
				}
			} else {
				throw new TargetInitializationException("value of '" + IVY_XML_KEY + "' must be a string");
			}
		} else {
			// the default value mimics the behaviour of standalone mode of Ivy 2.2.0
			mySaveTo = "lib/[conf]/[artifact].[ext]";
		}
	}

	@Override
	public void run() throws BuildException {
		Ivy ivy = Ivy.newInstance();

		if (myIvySettingsXmlPath != null) {
			Path resolvedIvySettingsXml = Build.getCurrent().getBasePath().resolve(myIvySettingsXmlPath);
			try {
				ivy.configure(resolvedIvySettingsXml.toFile());
			} catch (Exception e) {
				throw new BuildException("error while loading ivy settings", e);
			}
		}

		ResolveReport resolveReport;
		try {
			resolveReport = ivy.resolve(Build.getCurrent().getBasePath().resolve(myIvyXmlPath).toFile());
		} catch (Exception e) {
			throw new BuildException("error while resolving ivy modules", e);
		}
		if (resolveReport.hasError()) {
			throw new BuildException("error while resolving ivy modules");
		}

		try {
			ivy.retrieve(
					resolveReport.getModuleDescriptor().getModuleRevisionId(),
					mySaveTo,
					new RetrieveOptions()
			);
		} catch (Exception e) {
			throw new BuildException("error while retrieving ivy artifacts", e);
		}
	}

}
