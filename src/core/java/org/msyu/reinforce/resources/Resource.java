package org.msyu.reinforce.resources;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Represents an entity in a hierarchical file system.
 * </p>
 * A {@code Resource} is a combination of three things:
 * <ol>
 *     <li>a pointer to where the physical entity represented by the Resource is located
 *     (the {@linkplain #getPath() "real path"});</li>
 *     <li>a representation of the entity's attributes (the {@linkplain #getAttributes() "attributes"});</li>
 *     <li>and the resource's intended location and name within an abstract file tree
 *     (the {@linkplain #getRelativePath() "relative path"}).</li>
 * </ol>
 * The real path is optional; when it is specified, the resource is called "materialized", otherwise "virtual".
 * <p/>
 * Handling of virtual resources is {@link org.msyu.reinforce.Target Target}-specific and must be explicitly documented
 * by said {@code Target}s.
 *
 * @see ResourceCollection
 */
public interface Resource {

	/**
	 * Get the path to the materialized resource, if available.
	 * <p/>
	 * More specifically:
	 * <ul>
	 *     <li>if this Resource corresponds to an entity that exists on a file system, then this method returns a Path
	 *     object that can be used to access said entity (e.g., via the corresponding {@link java.nio.file.FileSystem}
	 *     object);</li>
	 *     <li>otherwise, this method returns {@code null}. Such a resource is called <em>virtual</em>.</li>
	 * </ul>
	 * Relative paths returned by this method must be {@linkplain Path#resolve(Path) resolved} against the
	 * {@linkplain org.msyu.reinforce.Build#getCurrent() current Build}'s
	 * {@linkplain org.msyu.reinforce.Build#getBasePath() base path} by this method's callers before use.
	 *
	 * @return the path to the materialized resource, or {@code null} if the resource is virtual.
	 */
	Path getPath();

	/**
	 * Get the attributes of the resource.
	 * <p/>
	 * For a materialized resource, implementations of this method should use methods appropriate for the resources'
	 * respective file systems to obtain the attributes. For example, for regular files and directories, one can use
	 * {@link java.nio.file.Files#readAttributes(java.nio.file.Path, Class, java.nio.file.LinkOption...)
	 * Files.readAttributes(this.getPath(), BasicFileAttributes.class)}.
	 * <p/>
	 * For a virtual resource, implementation of this method must make an effort to construct an appropriate set of
	 * attributes, rather than immediately throwing an exception.
	 *
	 * @return an object that provides access to a cached set of the basic file attributes of this resource.
	 * This method must never return {@code null}.
	 *
	 * @throws ResourceAccessException if the attributes can't be determined for some reason.
	 */
	BasicFileAttributes getAttributes() throws ResourceAccessException;

	/**
	 * Get the (intended) relative path of this resource.
	 * <p/>
	 * The "relative path" of a resource determines the resource's intended location and/or name after being processed
	 * by Reinforce.
	 * <p/>
	 * Examples:
	 * <ul>
	 *     <li>
	 *         In a collection that represents "the contents of the directory {@code /foo}", the resource of the file
	 *         {@code /foo/bar/baz} will have the relative path {@code bar/baz}, as the intention is that the contents
	 *         of the root directory ({@code /foo}) will be unchanged by processing (e.g., copying into another root or
	 *         packing into an archive).
	 *     </li>
	 *     <li>
	 *         When {@linkplain org.msyu.reinforce.resources.ResourceCollection#getResourceIterator() iterated over},
	 *         the first resource such a collection will return will represent its root. Such a resource will have
	 *         an <em>empty path</em> (as defined {@linkplain Path here}) as its relative path.<br/>
	 *         (Note that there is a special case regarding the root of file tree collections.
	 *         See <a href="EagerlyCachingFileTreeResourceCollection.html#regularRoot">here</a> for details.)
	 *     </li>
	 *     <li>
	 *         Special resources that represent single files (that are not part of any natural file tree; see,
	 *         for example, {@linkplain org.msyu.reinforce.target.archive.AbstractArchiveTarget archive targets})
	 *         or locations (e.g. the {@link org.msyu.reinforce.target.JavacTarget JavacTarget}
	 *         {@linkplain org.msyu.reinforce.target.archive.AbstractArchiveTarget#reinterpret(String) interpreted} as
	 *         {@code "root"}) usually have their respective {@linkplain java.nio.file.Path#getFileName() file names}
	 *         as relative path.
	 *     </li>
	 * </ul>
	 * <p/>
	 * Paths returned by this method must be relative, that is, not {@linkplain java.nio.file.Path#isAbsolute() absolute}.
	 * <p/>
	 * Implementations of this method must never return {@code null}.
	 *
	 * @return the relative path of this resource.
	 */
	Path getRelativePath();

}
