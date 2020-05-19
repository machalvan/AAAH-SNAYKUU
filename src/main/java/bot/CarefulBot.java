package bot;

import gameLogic.*;
import org.tensorflow.*;
import org.tensorflow.Graph;
import org.tensorflow.Session;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.types.UInt8;

public class CarefulBot implements Brain
{
	private GameState gamestate;
	private Snake self;

	public Direction getNextMove(Snake yourSnake, GameState gamestate)
	{



        System.out.println( "Hello World! I'm using tensorflow version " + TensorFlow.version() );
        String exportDir = "/home/halvan/Code/personal/AAAH-SNAYKUU/src/main/java/bot/model";

        try {
            SavedModelBundle smb = SavedModelBundle.load(exportDir, "serve");
            Session sess = smb.session();



            float[][] inputData = {{4, 3, 2, 1}};
            // We have to create tensor to feed it to session,
            // unlike in Python where you just pass Numpy array
            Tensor inputTensor = Tensor.create(inputData, Float.class);
            float[][] output = predict2(sess, inputTensor);
            for (int i = 0; i < output[0].length; i++) {
                System.out.println(output[0][i]);//should be 41. 51.5 62.
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        /*
        //Path modelPath = Paths.get("src/main/java/bot/surviveAsLongAsPossible_alone_selfNotLethal2.p");

        String fileName = "surviveAsLongAsPossible_alone_selfNotLethal2.p";

        Path modelPath = null;
        try {
            modelPath = Paths.get(CarefulBot.class.getClassLoader().getResource(fileName).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        System.out.println(modelPath);


        byte[] graph = new byte[0];
        try {
            graph = Files.readAllBytes(modelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Graph g = new Graph()) {
            g.importGraphDef(graph);
            //open session using imported graph
            try (Session sess = new Session(g)) {
                float[][] inputData = {{4, 3, 2, 1}};
                // We have to create tensor to feed it to session,
                // unlike in Python where you just pass Numpy array
                Tensor inputTensor = Tensor.create(inputData, Float.class);
                float[][] output = predict(sess, inputTensor);
                for (int i = 0; i < output[0].length; i++) {
                    System.out.println(output[0][i]);//should be 41. 51.5 62.
                }
            }
        }
        */


		self = yourSnake;
		this.gamestate = gamestate;

		Direction previousDirection = self.getCurrentDirection();
		if (gamestate.willCollide(self, previousDirection))
		{
			return previousDirection.turnLeft();
		}
		return previousDirection;
	}

    private static float[][] predict(Session sess, Tensor inputTensor) {
        Tensor result = sess.runner()
                .feed("input", inputTensor)
                .fetch("not_activated_output").run().get(0);
        float[][] outputBuffer = new float[1][3];
        result.copyTo(outputBuffer);
        return outputBuffer;
    }

    private static float[][] predict2(Session sess, Tensor inputTensor) {
        Tensor result = sess.runner()
                .feed("input_tensor", inputTensor)
                .fetch("not_activated_output").run().get(0);
        float[][] outputBuffer = new float[1][3];
        result.copyTo(outputBuffer);
        return outputBuffer;
    }
}
