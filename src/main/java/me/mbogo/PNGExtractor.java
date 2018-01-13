/* PNGExtractor.java
 *
 * Copyright (C) 2018 Mike Bogochow
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package me.mbogo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PNGExtractor
{
    private static Logger logger = LoggerFactory.getLogger(PNGExtractor.class);

    public static byte[] PNG_HEADER = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
    public static int LENGTH_FIELD_NUM_BYTES = 4;
    public static int CHUNK_TYPE_FIELD_NUM_BYTES = 4;
    public static int CRC_FIELD_NUM_BYTES = 4;

    private final InputStream inputStream;
    private final OutputStream outputStream;

    public PNGExtractor(final InputStream inputStream, final OutputStream outputStream)
    {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    private void read(final byte[] byteBuffer) throws IOException
    {
        if (inputStream.read(byteBuffer) == -1) {
            throw new IOException("Reached end of stream in middle of PNG file");
        }
    }

    private void transfer(final int numBytes) throws IOException
    {
        final byte[] bytes = new byte[numBytes];
        read(bytes);
        outputStream.write(bytes);
    }

    private boolean extractChunk() throws IOException
    {
        // First parse the length
        final byte[] lengthBytes = new byte[LENGTH_FIELD_NUM_BYTES];
        read(lengthBytes);

        // Convert the bytes to an int value
        int length = 0;
        int offset = (lengthBytes.length * 8) - 8;
        for (final byte lengthByte : lengthBytes) {
            assert offset >= 0;
            length = (length & ~0xff) | ((lengthByte & 0xff) << offset);
            offset -= 8;
        }

        logger.debug("PNG chunk data length: {}", length);

        // Read the chunk type; stop processing after IEND
        final byte[] chunkTypeBytes = new byte[CHUNK_TYPE_FIELD_NUM_BYTES];
        read(chunkTypeBytes);

        final String chunkType = new String(chunkTypeBytes);

        logger.debug("Chunk type: {}", chunkType);

        // Now write the chunk contents to the output stream
        outputStream.write(lengthBytes);
        outputStream.write(chunkTypeBytes);

        // Transfer the rest of the PNG file from the input stream to the output stream
        transfer(length + CRC_FIELD_NUM_BYTES);

        return !chunkType.equals("IEND");
    }

    public void extract() throws IOException
    {
        // First, write the PNG header to the output stream
        outputStream.write(PNG_HEADER);

        // Now extract all chunks to the output stream
        while (extractChunk());
    }
}
