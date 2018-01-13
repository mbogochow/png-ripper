/* Ripper.java
 *
 * Copyright (C) 2018 Mike Bogochow
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package me.mbogo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Ripper
{
    private static Logger logger = LoggerFactory.getLogger(Ripper.class);

    public static void main(String[] args) throws IOException
    {
        if (args.length < 1) {
            logger.error("Must specify file to parse");
            return;
        }

        final String filename = args[0];
        final File file = new File(filename);

        if (!file.exists()) {
            logger.error("File '{}' does not exist", filename);
            return;
        }

        logger.info("Parsing file '{}'", filename);

        final Parser parser = new Parser(file);
        parser.parse();
    }
}
