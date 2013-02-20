package org.msyu.reinforce.target.ivy;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.event.EventManager;
import org.apache.ivy.core.event.IvyEvent;
import org.apache.ivy.core.event.IvyListener;
import org.apache.ivy.core.event.retrieve.EndRetrieveArtifactEvent;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.msyu.reinforce.Build;
import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.resources.FileSystemResource;
import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.resources.ResourceCollection;
import org.msyu.reinforce.resources.ResourceEnumerationException;
import org.msyu.reinforce.resources.ResourceIterator;
import org.msyu.reinforce.resources.ResourceListCollection;

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

	public IvyRetrieveTarget(String name) {
		super(name);
	}

	@Override
	protected void initTarget(Map docMap, Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
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
		if (docMap.containsKey(CONFS_KEY)) {
			Object confsObject = docMap.get(CONFS_KEY);
			if (confsObject instanceof String) {
				myConfs = new String[]{(String) confsObject};
			} else if (confsObject instanceof List) {
				List confsList = (List) confsObject;
				List<String> confNames = new ArrayList<>(confsList.size());
				for (Object confName : confsList) {
					if (confName instanceof String) {
						confNames.add((String) confName);
					} else {
						throw new TargetInitializationException("all conf names must be strings");
					}
				}
				myConfs = confNames.toArray(new String[confNames.size()]);
			} else {
				throw new TargetInitializationException("value of '" + CONFS_KEY + "' must be a string or a list of strings");
			}
		}
	}

	@Override
	public void run() throws ExecutionException {
		List<Resource> retrievedFiles = new ArrayList<>();
		Ivy ivy = getIvyObject(retrievedFiles);

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
			resolveReport = ivy.resolve(Build.getCurrent().getBasePath().resolve(myIvyXmlPath).toFile());
		} catch (Exception e) {
			throw new ExecutionException("error while resolving ivy modules", e);
		}
		if (resolveReport.hasError()) {
			throw new ExecutionException("error while resolving ivy modules");
		}

		try {
			RetrieveOptions retrieveOptions = new RetrieveOptions();

			/*
			Ivy 2.2.0 (and, AFAIK, 2.3.0) does not report in any way artifacts
			that do not need to be copied (are up-to-date). To make the
			retrieved files collection fill up properly, we're forcing the
			artifacts to be overwritten regardless of their current state
			(and, therefore, to be reported via events).
			*/
			retrieveOptions.setOverwriteMode(RetrieveOptions.OVERWRITEMODE_ALWAYS);

			if (myConfs != null) {
				retrieveOptions.setConfs(myConfs);
			}
			ivy.retrieve(
					resolveReport.getModuleDescriptor().getModuleRevisionId(),
					mySaveTo,
					retrieveOptions
			);
		} catch (Exception e) {
			throw new ExecutionException("error while retrieving ivy artifacts", e);
		}

		myRetrievedFiles = new ResourceListCollection(retrievedFiles);
	}

	private Ivy getIvyObject(List<Resource> retrievedFiles) {
		Ivy ivy = new Ivy();
		ivy.setEventManager(getEventManager(retrievedFiles));
		ivy.bind();
		ivy.getLoggerEngine().pushLogger(new DefaultMessageLogger(myIvyLoggingLevel));
		return ivy;
	}

	private EventManager getEventManager(final List<Resource> retrievedFiles) {
		EventManager eventManager = new EventManager();
		eventManager.addIvyListener(
				new IvyListener() {
					@Override
					public void progress(IvyEvent event) {
						if (!(event instanceof EndRetrieveArtifactEvent)) {
							return;
						}
						Path artifactPath = ((EndRetrieveArtifactEvent) event).getDestFile().toPath().toAbsolutePath();
						Log.debug("Retrieved artifact: %s", artifactPath);
						retrievedFiles.add(new FileSystemResource(artifactPath, null, artifactPath.getFileName()));
					}
				},
				EndRetrieveArtifactEvent.NAME
		);
		return eventManager;
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

}
