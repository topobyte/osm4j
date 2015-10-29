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
// The methods for zig zag encoding in this file are based on the code from
// the file 'CodedOutputStream.java' from the Google Protocol Buffers library.
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

import java.io.IOException;

public abstract class CompactWriter
{

	public abstract void writeByte(int b) throws IOException;

	public abstract void write(byte[] bytes) throws IOException;

	public abstract void write(byte[] bytes, int off, int len)
			throws IOException;

	public void writeVariableLengthUnsignedInteger(long value)
			throws IOException
	{
		long store = value;
		while (true) {
			if ((store & ~0x7FL) == 0) {
				writeByte((int) store);
				return;
			} else {
				writeByte(((int) store & 0x7F) | 0x80);
				store >>>= 7;
			}
		}
	}

	public void writeVariableLengthSignedInteger(long value) throws IOException
	{
		long store = encodeZigZag(value);
		while (true) {
			if ((store & ~0x7FL) == 0) {
				writeByte((int) store);
				return;
			} else {
				writeByte(((int) store & 0x7F) | 0x80);
				store >>>= 7;
			}
		}
	}

	// Adapted from the protocol buffers library. See copyright above
	public static long encodeZigZag(final long n)
	{
		return (n << 1) ^ (n >> 63);
	}

	// Adapted from the protocol buffers library. See copyright above
	public static int getNumberOfBytesUnsigned(final long value)
	{
		long store = value;
		if ((store & (0xffffffffffffffffL << 7)) == 0)
			return 1;
		if ((store & (0xffffffffffffffffL << 14)) == 0)
			return 2;
		if ((store & (0xffffffffffffffffL << 21)) == 0)
			return 3;
		if ((store & (0xffffffffffffffffL << 28)) == 0)
			return 4;
		if ((store & (0xffffffffffffffffL << 35)) == 0)
			return 5;
		if ((store & (0xffffffffffffffffL << 42)) == 0)
			return 6;
		if ((store & (0xffffffffffffffffL << 49)) == 0)
			return 7;
		if ((store & (0xffffffffffffffffL << 56)) == 0)
			return 8;
		if ((store & (0xffffffffffffffffL << 63)) == 0)
			return 9;
		return 10;
	}

	// Adapted from the protocol buffers library. See copyright above
	public static int getNumberOfBytesSigned(final long value)
	{
		long store = encodeZigZag(value);
		if ((store & (0xffffffffffffffffL << 7)) == 0)
			return 1;
		if ((store & (0xffffffffffffffffL << 14)) == 0)
			return 2;
		if ((store & (0xffffffffffffffffL << 21)) == 0)
			return 3;
		if ((store & (0xffffffffffffffffL << 28)) == 0)
			return 4;
		if ((store & (0xffffffffffffffffL << 35)) == 0)
			return 5;
		if ((store & (0xffffffffffffffffL << 42)) == 0)
			return 6;
		if ((store & (0xffffffffffffffffL << 49)) == 0)
			return 7;
		if ((store & (0xffffffffffffffffL << 56)) == 0)
			return 8;
		if ((store & (0xffffffffffffffffL << 63)) == 0)
			return 9;
		return 10;
	}

	public void writeString(String string) throws IOException
	{
		byte[] bytes = string.getBytes();
		writeVariableLengthSignedInteger(bytes.length);
		write(bytes);
	}

	public void writeInt(int value) throws IOException
	{
		writeByte((value >>> 24) & 0xFF);
		writeByte((value >>> 16) & 0xFF);
		writeByte((value >>> 8) & 0xFF);
		writeByte((value) & 0xFF);
	}

	public void writeLong(long value) throws IOException
	{
		writeByte((int) (value >>> 56) & 0xFF);
		writeByte((int) (value >>> 48) & 0xFF);
		writeByte((int) (value >>> 40) & 0xFF);
		writeByte((int) (value >>> 32) & 0xFF);
		writeByte((int) (value >>> 24) & 0xFF);
		writeByte((int) (value >>> 16) & 0xFF);
		writeByte((int) (value >>> 8) & 0xFF);
		writeByte((int) (value) & 0xFF);
	}

}
