/**
 * Distributed under The MIT License
 * http://www.opensource.org/licenses/MIT
 */
package com.majora.minecraft.experienceshelves.utils;

import java.io.File;

public final class FileUtils {
	
	public static boolean exists(final String file)
	{
		return new File(file).exists();
	}

}
