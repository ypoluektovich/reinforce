package org.msyu.reinforce.target.ivy;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.retrieve.RetrieveReport;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.msyu.reinforce.Build;
import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.TargetInvocation;
import org.msyu.reinforce.resources.FileSystemResource;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.resources.ResourceCollection;
import org.msyu.reinforce.resources.ResourceEnumerationException;
import org.msyu.reinforce.resources.ResourceIterator;
import org.msyu.reinforce.resources.ResourceListCollection;
import org.msyu.reinforce.util.variables.VariableSubstitutionException;
import org.msyu.reinforce.util.variables.Variables;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class IvyRetrieveTarget extends Target implements ResourceCollection {

	public static final String IVY_LOGGING_KEY = "ivy logging";

	public static final String IVY_SETTINGS_XML_KEY = "ivysettings.xml";

	public static final String IVY_XML_KEY = "ivy.xml";

	public static final String SAVE_TO_KEY = "save to";

	public static final String CONFS_KEY = "confs";

	private int myIvyLoggingLevel = Message.MSG_INFO;

	private Path myIvySettingsXmlPath;

	private Path myIvyXmlPath;

	private String mySaveTo;

	private String[] myConfs;

	private ResourceCollection myRetrievedFiles;

	public IvyRetrieveTarget(TargetInvocation invocation) {
		super(invocation);
	}

	@Override
	protected void initTarget(Map docMap) throws TargetInitializationException {
		if (docMap.containsKey(IVY_LOGGING_KEY)) {
			Object ivyLoggingSetting = docMap.get(IVY_LOGGING_KEY);
			if (!(ivyLoggingSetting instanceof String)) {
				throw new TargetInitializationException("ivy logging level must be specified as a string");
			}
			switch (((String) ivyLoggingSetting).toLowerCase(Locale.ENGLISH)) {
				case "debug":
					myIvyLoggingLevel = Message.MSG_DEBUG;
					break;
				case "verbose":
					myIvyLoggingLevel = Message.MSG_VERBOSE;
					break;
				case "warn":
					myIvyLoggingLevel = Message.MSG_WARN;
					break;
				case "error":
					myIvyLoggingLevel = Message.MSG_ERR;
					break;
			}
		}
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
				try {
					myIvyXmlPath = Paths.get(Variables.expand((String) ivyXmlString));
				} catch (VariableSubstitutionException e) {
					throw new TargetInitializationException(
							"error while expanding variables in '" + IVY_XML_KEY + "' setting",
							e
					);
				}
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
		initConfs(docMap);
	}

	private void initConfs(Map docMap) throws TargetInitializationException {
		if (!docMap.containsKey(CONFS_KEY)) {
			return;
		}
		Object confsObject = docMap.get(CONFS_KEY);
		if (confsObject instanceof List) {
			List confsList = (List) confsObject;
			List<String> confNames = new ArrayList<>(confsList.size());
			for (int i = 0; i < confsList.size(); i++) {
				confNames.add(expandConf(confsList.get(i), i));
			}
			myConfs = confNames.toArray(new String[confNames.size()]);
		} else {
			myConfs = new String[]{expandConf(confsObject, 0)};
		}
		Log.verbose("Retrieving confs: %s", (Object) myConfs);
	}

	private String expandConf(Object confSetting, int index) throws TargetInitializationException {
		if (!(confSetting instanceof String)) {
			throw new TargetInitializationException("invalid setting under '" + CONFS_KEY + "' at #" + index + ": must be a string");
		}
		try {
			return Variables.expand((String) confSetting);
		} catch (VariableSubstitutionException e) {
			throw new TargetInitializationException("error while expanding variables in element " + index + " of '" + CONFS_KEY + "'", e);
		}
	}

	@Override
	public void run() throws ExecutionException {
		Ivy ivy = getIvyObject();

		if (myIvySettingsXmlPath != null) {
			Path resolvedIvySettingsXml = Build.getCurrent().getBasePath().resolve(myIvySettingsXmlPath);
			try {
				ivy.configure(resolvedIvySettingsXml.toFile());
			} catch (Exception e) {
				throw new ExecutionException("error while loading ivy settings", e);
			}
		}

		ResolveReport resolveReport;
		try {
			ResolveOptions resolveOptions = new ResolveOptions();
			if (myConfs != null) {
				resolveOptions.setConfs(myConfs);
			}
			resolveReport = ivy.resolve(
					Build.getCurrent().getBasePath().resolve(myIvyXmlPath).toFile().toURI().toURL(),
					resolveOptions
			);
		} catch (Exception e) {
			throw new ExecutionException("error while resolving ivy modules", e);
		}
		if (resolveReport.hasError()) {
			throw new ExecutionException("error while resolving ivy modules");
		}

		RetrieveReport retrieveReport;
		try {
			RetrieveOptions retrieveOptions = new RetrieveOptions();
			retrieveOptions.setDestArtifactPattern(mySaveTo);
			if (myConfs != null) {
				retrieveOptions.setConfs(myConfs);
			}
			retrieveReport = ivy.retrieve(
					resolveReport.getModuleDescriptor().getModuleRevisionId(),
					retrieveOptions
			);
		} catch (Exception e) {
			throw new ExecutionException("error while retrieving ivy artifacts", e);
		}

		myRetrievedFiles = getRetrievedResourceCollection(retrieveReport);
	}

	private ResourceListCollection getRetrievedResourceCollection(RetrieveReport retrieveReport) {
		Path retrieveRoot = retrieveReport.getRetrieveRoot().toPath().toAbsolutePath();
		List<Resource> retrievedFiles = new ArrayList<>();
		for (Object file : retrieveReport.getRetrievedFiles()) {
			Path path = ((File) file).toPath().toAbsolutePath();
			retrievedFiles.add(new FileSystemResource(path, null, retrieveRoot.relativize(path)));
		}
		return new ResourceListCollection(retrievedFiles);
	}

	private Ivy getIvyObject() {
		Ivy ivy = new Ivy();
		ivy.bind();
		ivy.getLoggerEngine().pushLogger(new DefaultMessageLogger(myIvyLoggingLevel));
		return ivy;
	}

	@Override
	public ResourceIterator getResourceIterator() throws ResourceEnumerationException {
		return myRetrievedFiles.getResourceIterator();
	}

	@Override
	public List<Resource> rebuildCache() throws ResourceEnumerationException {
		return myRetrievedFiles.rebuildCache();
	}

	@Override
	public Resource getRoot() {
		return myRetrievedFiles.getRoot();
	}

	@Override
	public boolean isEmpty() throws ResourceEnumerationException {
		return myRetrievedFiles.isEmpty();
	}

}
