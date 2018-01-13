/* Parser.java
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Parser
{
    private static Logger logger = LoggerFactory.getLogger(Parser.class);

    private final File file;

    public Parser(final File file)
    {
        this.file = file;
    }

    public void parse() throws IOException
    {
        int pngCount = 0;
        int byteCount = 0;

        // Parse until we find a PNG file within the file
        try (final FileInputStream inputStream = new FileInputStream(file)) {
            int nextByte;
            while ((nextByte = inputStream.read()) != -1) {
                byteCount += 1;
                boolean headerMatches = true;

                // Identify a PNG file based on the PNG header
                for (int i = 0; i < PNGExtractor.PNG_HEADER.length && nextByte != -1; i++) {
                    if (nextByte != (PNGExtractor.PNG_HEADER[i] & 0xff)) {
                        headerMatches = false;
                        break;
                    }
                    if (i + 1 < PNGExtractor.PNG_HEADER.length) {
                        nextByte = inputStream.read();
                        byteCount += 1;
                    }
                }

                if (logger.isTraceEnabled()) {
                    if (byteCount % 1000000 == 0) {
                        logger.trace("Processed {} bytes from file", byteCount);
                    }
                }

                if (headerMatches) {
                    // We found a PNG file so extract it
                    pngCount += 1;

                    logger.info("Found PNG file (current total: {})", pngCount);

                    try (final FileOutputStream fileOutputStream = new FileOutputStream(pngCount + ".png"))
                    {
                        final PNGExtractor pngExtractor = new PNGExtractor(inputStream, fileOutputStream);
                        pngExtractor.extract();
                    }
                }
            }
        }

        logger.info("Successfully finished parsing file.  Total PNG files extracted: {}", pngCount);
    }
}
