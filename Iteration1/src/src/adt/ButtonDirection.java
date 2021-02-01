package src.adt;

/**
 * Written for SYSC3303 - Group 6 - Iteration 1 @ Carleton University
 * @author Ethan Prentice (101070194)
 */
public enum ButtonDirection {
	UP,
	DOWN;
	
	/**
	 * @param name the name of the ButtonDirection to return
	 * @return the ButtonDirection associated with the name parameter.  If no such ButtonDirection exists, return null.
	 */
	public static ButtonDirection fromString(String name) {
		for (ButtonDirection v : ButtonDirection.values()) {
			if (v.name().toLowerCase().equals(name.toLowerCase())) {
				return v;
			}
		}
		return null;
	}
}
