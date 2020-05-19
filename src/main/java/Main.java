import gameLogic.Session;
import gameLogic.GameResult;
import org.tensorflow.Graph;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import userInterface.SettingsWindow;
import userInterface.MainWindow;
import userInterface.PostGameWindow;
import userInterface.GameEndType;
import javax.swing.UIManager;
import java.io.UnsupportedEncodingException;

class Main
{

	public static void main(String[] args)
	{

		try (Graph g = new Graph()) {
			final String value = "Hello from " + TensorFlow.version();

			// Construct the computation graph with a single operation, a constant
			// named "MyConst" with a value "value".
			try (Tensor t = Tensor.create(value.getBytes("UTF-8"))) {
				// The Java API doesn't yet include convenience functions for adding operations.
				g.opBuilder("Const", "MyConst").setAttr("dtype", t.dataType()).setAttr("value", t).build();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			// Execute the "MyConst" operation in a Session.
			try (org.tensorflow.Session s = new org.tensorflow.Session(g);
				 // Generally, there may be multiple output tensors,
				 // all of them must be closed to prevent resource leaks.
				 Tensor output = s.runner().fetch("MyConst").run().get(0)) {
				 //System.out.println(new String(output.bytesValue(), "UTF-8"));
			}
        }



		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e)
		{
		}
		SettingsWindow settingsWindow = new SettingsWindow();

		Session session = prepareSession(settingsWindow);

		//~ System.setSecurityManager(new ExitSecurityManager());

		GameEndType gameEndType;
		do
		{
			settingsWindow.dispose();

			int gameSpeed = settingsWindow.getGameSpeed();
			int pixelsPerUnit = settingsWindow.getPixelsPerUnit();
			gameEndType = runGame(session, gameSpeed, pixelsPerUnit);

			if (gameEndType == GameEndType.REMATCH)
			{
				try
				{
					session = settingsWindow.generateSession();
				}
				catch (Exception e)
				{
					javax.swing.JOptionPane.showMessageDialog(null, e);
					gameEndType = GameEndType.NEW_GAME;
				}
			}
			if (gameEndType == GameEndType.NEW_GAME)
				session = prepareSession(settingsWindow);

		}
		while (gameEndType != GameEndType.EXIT);
	}

	private static Session prepareSession(SettingsWindow settingsWindow)
	{
		try
		{
			settingsWindow.putThisDamnWindowInMyFace();

			while (!settingsWindow.isDone())
				sleep(10);

			return settingsWindow.generateSession();
		}
		catch (Exception e)
		{
			javax.swing.JOptionPane.showMessageDialog(settingsWindow, e);
			return prepareSession(settingsWindow);
		}
	}


	private static GameEndType runGame(Session session, int gameSpeed, int pixelsPerUnit)
	{
		MainWindow mainWindow = new MainWindow(session, pixelsPerUnit);
		session.tick();
		mainWindow.repaint();
		sleep(1000);

		while (!session.hasEnded())
		{
			session.tick();
			mainWindow.update();

			sleep(gameSpeed);
		}

		session.cleanup();

		PostGameWindow postGameWindow = new PostGameWindow(session);
		GameEndType gameEndType = postGameWindow.getGameEndType();
		mainWindow.dispose();

		return gameEndType;
	}


	private static void sleep(long ms)
	{
		try
		{
			Thread.currentThread().sleep(ms);
		}
		catch (InterruptedException e)
		{
		}
	}
}
