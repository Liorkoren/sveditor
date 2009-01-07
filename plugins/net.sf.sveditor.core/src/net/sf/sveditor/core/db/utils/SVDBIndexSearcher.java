package net.sf.sveditor.core.db.utils;

import java.util.ArrayList;
import java.util.List;

import net.sf.sveditor.core.ISVDBIndex;
import net.sf.sveditor.core.db.SVDBClassHierarchy;
import net.sf.sveditor.core.db.SVDBFile;
import net.sf.sveditor.core.db.SVDBItem;
import net.sf.sveditor.core.db.SVDBItemType;
import net.sf.sveditor.core.db.SVDBModIfcClassDecl;
import net.sf.sveditor.core.db.SVDBScopeItem;
import net.sf.sveditor.core.db.SVDBTaskFuncScope;
import net.sf.sveditor.core.db.SVDBVarDeclItem;

public class SVDBIndexSearcher {
	private List<SVDBFile>			fFiles = new ArrayList<SVDBFile>();

	public SVDBIndexSearcher() {
	}
	
	public SVDBIndexSearcher(ISVDBIndex index) {
		fFiles.addAll(index.getFileList());
	}
	
	public void addIndex(ISVDBIndex index) {
		fFiles.addAll(index.getFileList());
	}
	
	public void addFile(SVDBFile file) {
		fFiles.add(file);
	}
	
	/**
	 * Finds all classes named 'name' 
	 * 
	 * @param name
	 * @return
	 */
	public SVDBModIfcClassDecl findNamedClass(String name) {
		for (SVDBFile f : fFiles) {
			for (SVDBItem it : f.getItems()) {
				if (it.getType() == SVDBItemType.Class && 
						it.getName() != null && 
						it.getName().equals(name)) {
					return (SVDBModIfcClassDecl)it;
				}
			}
		}

		return null;
	}
	
	/**
	 * Traverses scopes beginning with 'context' searching
	 * for items named 'name'
	 * 
	 * @param name
	 * @param context
	 * @return
	 */
	public List<SVDBItem> findVarsByNameInScopes(
			String				name,
			SVDBScopeItem		context,
			boolean				stop_on_first_match) {
		List<SVDBItem> ret = new ArrayList<SVDBItem>();


		// Search up the scope
		while (context != null) {
			
			// First, search the local variables
			for (SVDBItem it : context.getItems()) {
				if (it.getType() == SVDBItemType.VarDecl) {
					if (it.getName().equals(name)) {
						ret.add(it);
						
						if (stop_on_first_match) {
							break;
						}
					}
				}
			}
			
			if (ret.size() > 0 && stop_on_first_match) {
				break;
			}
			
			// Next, search the parameters, if we're in a function/task scope
			if (context.getType() == SVDBItemType.Function || 
					context.getType() == SVDBItemType.Task) {
				for (SVDBItem it : ((SVDBTaskFuncScope)context).getParams()) {
					if (it.getName().equals(name)) {
						ret.add(it);
						
						if (stop_on_first_match) {
							break;
						}
					}
				}
			}

			if (ret.size() > 0 && stop_on_first_match) {
				break;
			}

			if (context.getType() == SVDBItemType.Task ||
					context.getType() == SVDBItemType.Function) {
				
			}
		}
		
		return ret;
	}
	
	/**
	 * 
	 * @param it
	 * @return
	 */
	public SVDBClassHierarchy findClassTypeOfItem(SVDBItem it) {
		/*
		SVDBClassHierarchy  ret = null;
		SVDBModIfcClassDecl c = null;
		
		if (it.getType() == SVDBItemType.VarDecl) {
			SVDBVarDeclItem d = (SVDBVarDeclItem)it;
			
			if (d.getTypeName() != null) {
				if ((c = findNamedClass(d.getTypeName())) != null) {
					ret = new SVDBClassHierarchy(c);
					
					// Now, iterate through the 
					
					if (ret.getParameters().size() > 0) {
						// TODO: must customize this class
					}
					return findNamedClass(d.getTypeName());
				}
			}
		} else if (it.getType() == SVDBItemType.Function) {
			// Find the return type of the function
			SVDBTaskFuncScope f = (SVDBTaskFuncScope)it;
			
			if (f.getReturnType() != null && !f.getReturnType().equals("void")) {
				// See if we can find this type
				return findNamedClass(f.getReturnType());
			}
		}
		 */
		
		return null;
	}
	
	public List<SVDBItem> findByName(
			String				name,
			SVDBItemType	...	types) {
		List<SVDBItem> ret = new ArrayList<SVDBItem>();
		
		for (SVDBFile f : fFiles) {
			List<SVDBItem> r = SVDBSearchUtils.findItemsByName(f, name, types);
			ret.addAll(r);
		}
		
		return ret;
	}
	
	public List<SVDBItem> findByPrefixInTypeHierarchy(
			String						prefix,
			SVDBScopeItem				ref_type,
			SVDBItemType		... 	types) {
		List<SVDBItem> ret = new ArrayList<SVDBItem>();
		
		while (ref_type != null) {
			for (SVDBItem it : ref_type.getItems()) {
				boolean type_match = (types.length == 0);
				
				for (SVDBItemType type : types) {
					if (it.getType() == type) {
						type_match = true;
						break;
					}
				}
				
				if (type_match && it.getName().startsWith(prefix)) {
					ret.add(it);
				}
			}
			
			// Continue traversing the type hierarchy
			if (ref_type.getType() == SVDBItemType.Class &&
					((SVDBModIfcClassDecl)ref_type).getSuperClass() != null) {
				ref_type = findNamedClass(
						((SVDBModIfcClassDecl)ref_type).getSuperClass());
			} else {
				ref_type = null;
			}
		}
		
		return ret;
	}

	// public List<SVDBItem> findByNameInScopeHierarchy(
			
	
	public List<SVDBItem> findByNameInClassHierarchy(
			String				name,
			SVDBScopeItem		scope,
			SVDBItemType	...	types) {

		List<SVDBItem> ret = new ArrayList<SVDBItem>();
		
		while (scope != null && scope.getType() != SVDBItemType.Class) {
			scope = scope.getParent();
		}
		
		if (scope == null) {
			return ret;
		}
		
		// Now, search through the scope and the class hierarchy
		while (scope != null) {
			for (SVDBItem it : scope.getItems()) {
				boolean match_type = (types.length == 0);
				
				for (SVDBItemType t : types) {
					if (it.getType() == t) {
						match_type = true;
						break;
					}
				}
				if (match_type && it.getName().equals(name)) {
					ret.add(it);
				}
			}
			
			scope = findNamedClass(((SVDBModIfcClassDecl)scope).getSuperClass()); 
		}
		
		return ret;
	}
	
}
