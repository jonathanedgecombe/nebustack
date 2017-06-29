package com.nebustack.file;


import java.io.IOException;

public abstract class Reader {
	public abstract Frame read(FrameType type) throws IOException;
}
