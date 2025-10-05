package com.example.justlocal.Utility;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;

public class TfliteEmbeddingHelper {
    private Interpreter interpreter;
    private int inputSize;      // e.g., 224
    private int embeddingDim;   // auto-detected from model output
    private final float IMAGE_MEAN = 127.5f;
    private final float IMAGE_STD = 127.5f;

    public TfliteEmbeddingHelper(Context context, String modelAssetPath, int inputSize) throws IOException {
        MappedByteBuffer model = FileUtil.loadMappedFile(context, modelAssetPath);
        Interpreter.Options opts = new Interpreter.Options();
        interpreter = new Interpreter(model, opts);
        this.inputSize = inputSize;

        // ✅ detect output dimension automatically
        int[] outputShape = interpreter.getOutputTensor(0).shape(); // e.g. [1,1280] or [1,1001]
        this.embeddingDim = outputShape[1];
    }

    public float[] getEmbedding(Bitmap bm) {
        Bitmap resized = Bitmap.createScaledBitmap(bm, inputSize, inputSize, true);

        // Normalize and convert to float buffer
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);
        inputBuffer.order(ByteOrder.nativeOrder());
        inputBuffer.rewind();

        int[] intValues = new int[inputSize * inputSize];
        resized.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize);
        int pixel = 0;
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                final int val = intValues[pixel++];
                float r = (((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
                float g = (((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
                float b = ((val & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
                inputBuffer.putFloat(r);
                inputBuffer.putFloat(g);
                inputBuffer.putFloat(b);
            }
        }

        // Prepare output buffer dynamically
        float[][] output = new float[1][embeddingDim];
        interpreter.run(inputBuffer, output);
        float[] embedding = output[0];

        // ✅ L2 normalize (optional, good for cosine similarity)
        float norm = 0f;
        for (float v : embedding) norm += v * v;
        norm = (float) Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < embedding.length; i++) embedding[i] /= norm;
        }
        return embedding;
    }

    public void close() {
        if (interpreter != null) interpreter.close();
    }
}
