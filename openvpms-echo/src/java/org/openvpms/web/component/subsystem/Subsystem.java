package org.openvpms.web.component.subsystem;

import java.util.List;


/**
 * Manages the user interface for a business domain subsystem. A subsystem is
 * essentially a set of related {@link Workspace}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public interface Subsystem {

    /**
     * Returns a localised title for the subsystem.
     *
     * @return a localised title for the subsystem.
     */
    String getTitle();

    /**
     * Returns the current workspace.
     *
     * @return the current workspace, or  <code>null</code> if there is no
     *         current workspace
     */
    Workspace getWorkspace();

    /**
     * Sets the current workspace.
     *
     * @param workspace the current workspace
     */
    void setWorkspace(Workspace workspace);

    /**
     * Returns the default workspace.
     *
     * @return the default workspace, or <code>null</code>  if there is no
     *         default workspace
     */
    Workspace getDefaultWorkspace();

    /**
     * Returns the workspaces.
     *
     * @return a list of the woprkspaces
     */
    List<Workspace> getWorkspaces();

}
