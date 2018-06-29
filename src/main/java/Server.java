import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class Server {

    private Robot mRobot;
    private ServerSocket mServerSocket;
    private Socket mClientSocket;
    Timer mTimer;

    private int mouseX, mouseY;
    private long mTimeUpdated;

    public Server() {
        try {
            mRobot = new Robot();
            mServerSocket = new ServerSocket(63288);
            System.out.println("Server on port: " + mServerSocket.getLocalPort());
        } catch (IOException | AWTException e) {
            e.printStackTrace();
        }
    }

    public void waitForConnection() {
        try {
            System.out.println("Waiting for connection.");
            mClientSocket = mServerSocket.accept();
            System.out.println("Connected to client.");
            listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listen() {
        try {
            DataInputStream inStream = new DataInputStream(mClientSocket.getInputStream());
            monitorIdleTime();
            int inVal;
            while (true) {
                inVal = inStream.readInt();
                mTimeUpdated = System.currentTimeMillis();
                switch (inVal) {
                    case ProtocolConstants.CODE_CALIBRATE:
                        centerMouse();
                        break;
                    case ProtocolConstants.CODE_LEFT_DOWN:
                        mRobot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        break;
                    case ProtocolConstants.CODE_LEFT_CLICK:
                        mRobot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        mRobot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        break;
                    case ProtocolConstants.CODE_LEFT_UP:
                        mRobot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        break;
                    case ProtocolConstants.CODE_RIGHT_CLICK:
                        mRobot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                        mRobot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
                        break;
                    case ProtocolConstants.CODE_RIGHT_DOWN:
                        mRobot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                        break;
                    case ProtocolConstants.CODE_RIGHT_UP:
                        mRobot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
                        break;
                    case ProtocolConstants.CODE_KEYBOARD:
                        int unicodeChar = inStream.readInt();
                        int keycode;
                        if (unicodeChar == 0) {
                            keycode = KeyEvent.VK_BACK_SPACE;
                        } else if (unicodeChar == 10) {
                            keycode = KeyEvent.VK_ENTER;
                        } else {
                            keycode = KeyEvent.getExtendedKeyCodeForChar(unicodeChar);
                        }
                        try {
                            mRobot.keyPress(keycode);
                            mRobot.keyRelease(keycode);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Illegal Keycode: " + keycode);
                        }
                        break;
                    default:
                        mouseX -= inVal;
                        mouseY -= inStream.readInt();
                        moveMouse();
                        break;
                }
            }
        } catch (EOFException e) {
            System.out.println("Connection with client was terminated.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        mTimer.cancel();
    }

    private void moveMouse() {
        int x = mouseX;
        int y = mouseY;

        if (x < 0) {
            x = 0;
        }

        if (y < 0) {
            y = 0;
        }

        if (x > (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth()) {
            x = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        }

        if (y > (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()) {
            y = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        }

        mRobot.mouseMove(x, y);
    }

    private void centerMouse() {
        mouseX = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2;
        mouseY = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2;
        moveMouse();
    }

    private void monitorIdleTime() {
        mTimer = new Timer();
        TimerTask myTask = new TimerTask() {
            @Override
            public void run() {

            }
        };
        mTimer.schedule(myTask, 5000, 5000);
    }
}
