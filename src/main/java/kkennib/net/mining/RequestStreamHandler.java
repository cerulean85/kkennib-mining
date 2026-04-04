package kkennib.net.mining;


import com.amazonaws.services.lambda.runtime.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface RequestStreamHandler {

    void handleRequest(InputStream input, OutputStream output, Context context) throws IOException;
}