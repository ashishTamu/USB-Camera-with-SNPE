package edu.ashishtamu.snpelib;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

import com.qualcomm.qti.snpe.FloatTensor;
import com.qualcomm.qti.snpe.NeuralNetwork;
import com.qualcomm.qti.snpe.SNPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ashish kumar on 12/15/2017.
 */

public class SnpeHandler {

    private static final String TAG = SnpeHandler.class.getSimpleName();
    private static final boolean DEBUG = true;
    /**
     * DSP run time
     * Power performance profile
     * Inputs can be DSP run time
     */

    /* Model Path */
    private File dlcPath=new File ("/data/local/tmp/inception_v3/inception_v3_quantized.dlc");
    private NeuralNetwork.Runtime runTime = NeuralNetwork.Runtime.GPU;
    private NeuralNetwork.PerformanceProfile performanceProfile = NeuralNetwork.PerformanceProfile.HIGH_PERFORMANCE;
    private FloatTensor inputTensor;
    private Application mApplication;
    private NeuralNetwork snpeNetwork;
    private SNPE.NeuralNetworkBuilder snpeBuilder;
    private Map<String, FloatTensor> outputs;
    private Map<String, FloatTensor> inputs = new HashMap<>();

    public File targetVector = new File("/data/local/tmp/inception_v3/target.txt");
    public String[] labels;
    public List<String> result = new LinkedList<>();

    public SnpeHandler(Application app,
                       File dlcConsPath,
                       NeuralNetwork.PerformanceProfile powerProfile,
                       NeuralNetwork.Runtime coreType) {
        this.dlcPath = dlcConsPath;
        this.performanceProfile = powerProfile;
        this.runTime = coreType;
        //this.inputTensor = input;
        this.mApplication = app;


    }



    public Application getApplication(){
        return this.mApplication;
    }

    public SNPE.NeuralNetworkBuilder getSnpeBuilder(){
        return this.snpeBuilder;
    }

    /* Outputs */

    /**
     * Select the neural network model and runtime target
     * Create one or more input tensor(s)
     * Populate one or more input tensor(s) with the network input(s)
     * Forward propagate the input tensor(s) through the network
     * Process the network output tensor(s)
     */

    /*Build Network */
    public NeuralNetwork buildSnpeNetwork() {
        try {
            snpeBuilder = new SNPE.NeuralNetworkBuilder(mApplication)
            //.setDebugEnabled(false)
            .setRuntimeOrder(runTime)
            .setPerformanceProfile(performanceProfile)
            .setModel(dlcPath);

            boolean isCoreSupported = snpeBuilder.isRuntimeSupported(runTime);
            if (!isCoreSupported) {
                Log.e(TAG, "!!! Error: Core not supported");
                snpeNetwork = null;
                return snpeNetwork;
            }

            snpeNetwork = snpeBuilder.build();
        } catch (IllegalStateException | IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return snpeNetwork;
    }

    public void releaseSnpeNetwork() {
        snpeNetwork.release();
        snpeNetwork = null;

    }

    public Map<String, FloatTensor> processTensorInput(NeuralNetwork snpeNetwork,
                                                       Bitmap bitmap) {

        FloatTensor tensor = snpeNetwork.createFloatTensor
                (snpeNetwork.getInputTensorsShapes().get("Mul:0"));
        if (DEBUG) {Log.d(TAG,"Start Preprocessing image");}
        writeRgbBitmapAsFloat(bitmap, tensor);
        if (DEBUG) Log.d(TAG," Preprocessing image done");
        inputs.put("Mul:0", tensor);
        return inputs;
    }

    public Map<String, FloatTensor> executeNetwork(NeuralNetwork snpeNetwork,
                                                   Bitmap bitmap) {
        if (DEBUG) Log.d(TAG,"Network execution start ");
        outputs = snpeNetwork.execute(processTensorInput(snpeNetwork,bitmap));
        if (DEBUG) Log.d(TAG,"Network execution done ");
        return outputs;
    }

    private void writeRgbBitmapAsFloat(Bitmap bitmap, FloatTensor tensor) {



        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = 299;
        int newHeight = 299;
        int cropW = Math.max(0,(width - newWidth)/2);
        //cropW = (cropW % 2 == 1)&& (cropW>0) ? cropW/2 : ((cropW % 2 == 1)? :0) ;
        //cropW = (cropW < 0)? 0: cropW;
        //cropW = 90;

        int cropH = Math.max (0,(height - newHeight) / 2);
       // cropH = 170;

        Bitmap image = bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
        final int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getPixels(pixels, 0, image.getWidth(), 0, 0,
                image.getWidth(), image.getHeight());
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                final int rgb = pixels[y * image.getWidth() + x];
                float b = (((rgb) & 0xFF) - 128) / 128.0f;
                float g = (((rgb >> 8) & 0xFF) - 128) / 128.0f;
                float r =(((rgb >> 16) & 0xFF) - 128) / 128.0f;
                float[] pixelFloats = {b, g, r};
                tensor.write(pixelFloats, 0, pixelFloats.length, y, x);
            }
        }
    }

    public List<String> snpeClassifyImage(Bitmap image) {
        try {
            labels = loadLabels(targetVector);
        } catch (IOException e) {
            e.printStackTrace();
        }


        final Map<String, FloatTensor> outputs = executeNetwork(snpeNetwork,image);


        for (Map.Entry<String, FloatTensor> output : outputs.entrySet()) {
            if (output.getKey().equals("softmax:0")) {
                for (Pair<Integer, Float> pair : topK(1, output.getValue())) {
                    result.add(labels[pair.first]);
                    result.add(String.valueOf(pair.second));
                }
            }
        }


        return result;
    }




    private Pair<Integer, Float>[] topK(int k, FloatTensor tensor) {
        final float[] array = new float[tensor.getSize()];
        tensor.read(array, 0, array.length);

        final boolean[] selected = new boolean[tensor.getSize()];
        final Pair<Integer, Float> topK[] = new Pair[k];
        int count = 0;
        while (count < k) {
            final int index = top(array, selected);
            selected[index] = true;
            topK[count] = new Pair<>(index, array[index]);
            count++;
        }
        return topK;
    }

    private int top(float[] array, boolean[] selected) {
        int index = 0;
        float max = -1.f;
        for (int i = 0; i < array.length; i++) {
            if (selected[i]) {
                continue;
            }
            if (array[i] > max) {
                max = array[i];
                index = i;
            }
        }
        return index;
    }

    private String[] loadLabels(File labelsFile) throws IOException {
        final List<String> list = new LinkedList<>();
        final BufferedReader inputStream = new BufferedReader(
                new InputStreamReader(new FileInputStream(labelsFile)));
        String line;
        while ((line = inputStream.readLine()) != null) {
            list.add(line);
        }
        return list.toArray(new String[list.size()]);
    }

}