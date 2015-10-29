// Copyright 2015 Sebastian Kuerten
//
// This file is part of osm4j.
//
// osm4j is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// osm4j is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with osm4j. If not, see <http://www.gnu.org/licenses/>.
//
//
// The method for zig zag decoding in this file is based on the code from
// the file 'CodedInputStream.java' from the Google Protocol Buffers library.
// The copyright notice from that file is repeated here:
//
//
// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// https://developers.google.com/protocol-buffers/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package de.topobyte.osm4j.tbo.io;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;

public abstract class CompactReader
{

	public abstract int readByte() throws IOException;

	public abstract int read(byte[] buffer, int off, int len)
			throws IOException;

	public long readVariableLengthSignedInteger() throws IOException
	{
		int shift = 0;
		long result = 0;
		while (shift < 64) {
			final int b = readByte();
			result |= (long) (b & 0x7F) << shift;
			if ((b & 0x80) == 0) {
				return decodeZigZag(result);
			}
			shift += 7;
		}
		throw new IOException("invalid encoding for a long value");
	}

	// Adapted from the protocol buffers library. See copyright above
	public static long decodeZigZag(final long n)
	{
		return (n >>> 1) ^ -(n & 1);
	}

	private static Charset charset = Charset.forName("UTF-8");

	public String readString() throws IOException
	{
		int length = (int) readVariableLengthSignedInteger();
		byte[] buffer = new byte[length];
		readFully(buffer);
		return new String(buffer, charset);
	}

	public void readFully(byte[] buffer) throws IOException
	{
		readFully(buffer, 0, buffer.length);
	}

	private void readFully(byte[] buffer, int off, int len) throws IOException
	{
		while (len > 0) {
			int n = read(buffer, off, len);
			if (n < 0) {
				throw new EOFException();
			}
			off += n;
			len -= n;
		}
	}

	public int readInt() throws IOException
	{
		int b1 = readByte();
		int b2 = readByte();
		int b3 = readByte();
		int b4 = readByte();
		return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
	}

	public long readLong() throws IOException
	{
		long i1 = readInt();
		long i2 = readInt();
		return (i1 << 32) | i2;
	}

}
