import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;

public class MouseSystemTray {

    public MouseSystemTray() { }

    public void createTray() {
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(createImage("ic_mouse.png", "mouse mover"));
        final SystemTray tray = SystemTray.getSystemTray();

        String pairingCode = "Unknown";
        try {
            pairingCode = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        // Create a pop-up menu components
        MenuItem pairingCodeItem = new MenuItem("Pairing Code: " + pairingCode);
        MenuItem aboutItem = new MenuItem("About");
        MenuItem exitItem = new MenuItem("Exit");

        exitItem.addActionListener(e -> {
            switch (e.getActionCommand()) {
                case "Exit":
                    System.exit(0);
                    break;
            }
        });

        //Add components to pop-up menu
        popup.add(pairingCodeItem);
        popup.addSeparator();
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    private static Image createImage(String path, String description) {
        URL imageURL = MouseSystemTray.class.getResource(path);
        return (new ImageIcon(imageURL, description)).getImage();
    }
}
