package org.rexcellentgames.byejava.scanner;

import java.util.HashMap;

public class Keywords {
	public static HashMap<String, TokenType> types = new HashMap<>();

	static {
		/*
			abstract	assert	boolean	break	byte	case
			catch	char	class	const	continue	default
			double	do	else	enum	extends	false
			final	finally	float	for	goto	if
			implements	import	instanceof	int	interface	long
			native	new	null	package	private	protected
			public	return	short	static	strictfp	super
			switch	synchronized	this	throw	throws	transient
			true	try	void	volatile	while
		 */

		types.put("class", TokenType.CLASS);
	}
}
