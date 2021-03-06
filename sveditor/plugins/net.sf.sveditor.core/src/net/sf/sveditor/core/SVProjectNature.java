/****************************************************************************
 * Copyright (c) 2008-2010 Matthew Ballance and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Ballance - initial implementation
 ****************************************************************************/


package net.sf.sveditor.core;

import net.sf.sveditor.core.log.ILogLevel;
import net.sf.sveditor.core.log.LogFactory;
import net.sf.sveditor.core.log.LogHandle;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public class SVProjectNature implements IProjectNature, ILogLevel {
	public static final String		NATURE_ID = "net.sf.sveditor.core.SVNature";
	
	private IProject				fProject;

	
	public void configure() throws CoreException { }

	
	public void deconfigure() throws CoreException { }

	
	public IProject getProject() {
		return fProject;
	}

	
	public void setProject(IProject project) {
		fProject = project;
	}

	public static boolean hasSvProjectNature(IProject p) {
		boolean has = false;
		try {
			IProjectDescription d = p.getDescription();
			String natures[] = d.getNatureIds();
			
			if (natures != null) {
				for (String n : natures) {
					if (n.equals(SVProjectNature.NATURE_ID)) {
						has = true;
						break;
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return has;
	}
	
	/**
	 * Check the specified project and add the SV project
	 * nature if it isn't already present
	 * @param p
	 */
	public static void ensureHasSvProjectNature(IProject p) {
		LogHandle log = LogFactory.getLogHandle("SVProjectNature");

		try {
			IProjectDescription d = p.getDescription();
			String natures[] = d.getNatureIds();
			boolean has = false;
			
			if (natures != null) {
				for (String n : natures) {
					if (n.equals(SVProjectNature.NATURE_ID)) {
						has = true;
						break;
					}
				}
				if (!has) {
					log.debug(LEVEL_MIN, "Adding project nature to " + p.getName());
					String natures_t[] = new String[natures.length+1];
					for (int i=0; i<natures.length; i++) {
						natures_t[i] = natures[i];
					}
					natures_t[natures.length] = NATURE_ID;
					natures = natures_t;
				} else {
					log.debug(LEVEL_MIN, "Project " + p.getName() + " already has SVE nature");
				}
			} else {
				log.debug(LEVEL_MIN, "Adding project nature to " + p.getName());
				natures = new String[1];
				natures[0] = NATURE_ID;
			}
			
			if (!has) {
				d.setNatureIds(natures);
				
				// Attach the builder
				ICommand commands[] = d.getBuildSpec();
				boolean has_b = false;
				
				if (commands != null) {
					for (int i=0; i<commands.length; i++) {
						if (commands[i].getBuilderName().equals(SVProjectBuilder.BUILDER_ID)) {
							has_b = true;
							break;
						}
					}
					
					if (!has_b) {
						ICommand commands_t[] = new ICommand[commands.length+1];
						for (int i=0; i<commands.length; i++) {
							commands_t[i] = commands[i];
						}
						commands_t[commands.length] = d.newCommand();
						commands_t[commands.length].setBuilderName(SVProjectBuilder.BUILDER_ID);
						commands = commands_t;
					}
				} else {
					commands = new ICommand[1];
					commands[0] = d.newCommand();
					commands[0].setBuilderName(SVProjectBuilder.BUILDER_ID);
				}
				
				if (!has_b) {
					d.setBuildSpec(commands);
				}
				
				p.setDescription(d, new NullProgressMonitor());
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}
