// Multi2SingleBit.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

/**
 * The Multi2SingleBit interface is implemented by the objects in the program
 * that has a role in the multibit to singlebit transformation.
 */
public interface Multi2SingleBit {
	//~ Methods ----------------------------------------------------------------

	/**
	 * This method performs the transformation itself and add the result
	 * statements to the appropriate function.
	 * The object parameter is one parameter that is needed for each
	 * class to implement this method and the parameter's role can vary
	 * from one class to another.
	 * If obj is not needed the method will be
	 * called be the parameter obj as null.
	 * @param obj the method's parameter.
	 * @return a BlockStatement containing the statements as single bits.
	 */
	public abstract BlockStatement multi2SingleBit(Object obj);
}
