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

public class Server {

    private Robot mRobot;
    private ServerSocket mServerSocket;

    private int mouseX, mouseY;

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
            Socket clientSocket = mServerSocket.accept();
            System.out.println("Connected to client.");
            listen(clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getLocalPort() {
        return mServerSocket.getLocalPort();
    }

    private void listen(Socket clientSocket) {
        try {
            DataInputStream inStream = new DataInputStream(clientSocket.getInputStream());

            int inVal;
            while (true) {
                inVal = inStream.readInt();
                switch (inVal) {
                    case ProtocolConstants.CODE_CALIBRATE:
                        centerMouse();
                        break;
                    case ProtocolConstants.CODE_LEFT_CLICK_DOWN:
                        mRobot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        break;
                    case ProtocolConstants.CODE_LEFT_CLICK_UP:
                        mRobot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        break;
                    case ProtocolConstants.CODE_RIGHT_CLICK_DOWN:
                        mRobot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                        break;
                    case ProtocolConstants.CODE_RIGHT_CLICK_UP:
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
                }
            }
        } catch (EOFException e) {
            System.out.println("Connection with client was terminated.");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
