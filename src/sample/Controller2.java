package sample;
import com.sun.javafx.scene.control.skin.LabeledText;
import com.sun.javafx.scene.control.skin.ListViewSkin;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

public class Controller2
{
    public static Client client;
    public static String activeChatId;
    public boolean groupActiveChat;

    public ListView listContacts;
    public ListView listChatMessages;
    public TextField txtMessage;

    public Label lblChatIsOnline;
    public Label lblChatUsername;

    public Button btnSend;
    public Button btnFile;

    public ObservableList<String> items = FXCollections.observableArrayList();
    public ObservableList<String> chatMessages = FXCollections.observableArrayList();
    public ObservableList<String> contacts = FXCollections.observableArrayList();

    public HashMap<String, Boolean> isOnline;
    public HashMap<String, Date> lastOnline;

    public Controller2()
    {
        client.controller2 = this;
        client.sendProfile();
        activeChatId = "";
        isOnline = new HashMap<String, Boolean>();
        lastOnline = new HashMap<String, Date>();
    }

    public void listContactsClick(Event event) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                txtMessage.setVisible(true);
                btnFile.setVisible(true);
                btnSend.setVisible(true);
                txtMessage.setText("");
                txtMessage.requestFocus();
                String itemStr = (String) (listContacts.getSelectionModel().getSelectedItem());
                if (itemStr == null)
                    return;
                itemStr = itemStr.split(" - ")[0];
                groupActiveChat = false;
                if (!activeChatId.equals(itemStr)) {
                    activeChatId = itemStr;
                    int idx = listContacts.getSelectionModel().getSelectedIndex();
                    contacts.set(idx, itemStr);
                    System.out.println(activeChatId);
                    chatMessages.clear();
                    listChatMessages.setItems(chatMessages);
                    client.sendGetChatQuery(false, itemStr);
                    lblChatUsername.setText(itemStr);
                    if (isOnline.get(activeChatId))
                    {
                        lblChatIsOnline.setText("online");
//                        lastOnline.put(activeChatId, new Date());
                    }
                    else
                    {
                        lblChatIsOnline.setText("last seen " + new Date().toString());
                    }
                }
            }
        });
    }

    public void receiveMessage(Message message)
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                System.out.println(message.toString());
                if (message.src.getUsername().equals(activeChatId) || message.dstId.equals(activeChatId))
                {
                    chatMessages.add(message.toString());
                    listChatMessages.setItems(chatMessages);
                }
                else
                {
                    for (int i = 0; i < contacts.size(); i++)
                    {
                        String s2 = contacts.get(i).split(" - ")[0];
                        if (s2.equals(message.src.getUsername()))
                        {
                            contacts.set(i, message.src.getUsername() + " - unread");
//                            listContacts.setItems(contacts);
                            return;
                        }
                    }
                }
                listChatMessages.scrollTo(chatMessages.size() - 1);
            }
        });
    }

    public void receiveUserProfile(UserProfile userProfile)
    {
        Platform.runLater(new Runnable()
        {
            @Override public void run()
            {
                if (userProfile.src.getUsername().equals(client.getUsername()))
                    return;
                boolean flag = true;
                for (String s: contacts)
                {
                    String s2 = s.split(" - ")[0];
                    if (s2.equals(userProfile.src.getUsername()))
                    {
                        flag = false;
                        break;
                    }
                }
                if (flag)
                {
                    contacts.add(userProfile.src.getUsername());
                    listContacts.setItems(contacts);
                }
                isOnline.put(userProfile.src.getUsername(), userProfile.src.isConnected);
                if (activeChatId.equals(userProfile.src.getUsername()))
                {
                    if (isOnline.get(activeChatId))
                        lblChatIsOnline.setText("online");
                    else
                        lblChatIsOnline.setText("last seen " + new Date().toString());
                }
            }
        });
    }

    public void listGroupsClick(Event event)
    {
        String itemStr = (String)(listContacts.getSelectionModel().getSelectedItem());
        groupActiveChat = true;
        if (!activeChatId.equals(itemStr))
        {
            activeChatId = itemStr;
            chatMessages.clear();
            listChatMessages.setItems(chatMessages);
        }
    }

    public void btnCreateGroupAction(ActionEvent actionEvent)
    {
    }

    public void btnFileAction(ActionEvent actionEvent)
    {
        JFileChooser fileChooser = new JFileChooser();
        int res = fileChooser.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION)
            client.sendFileMessage(false, activeChatId, fileChooser.getSelectedFile());
    }

    public void btnSendAction(ActionEvent actionEvent)
    {
        client.sendTextMessage(groupActiveChat, activeChatId, txtMessage.getText());
        chatMessages.add(client.getUsername() + ": " + txtMessage.getText());
        listChatMessages.setItems(chatMessages);
        txtMessage.setText("");
    }

    public void txtMessageKeyPress(Event event)
    {
//        if (((KeyEvent)event).getCode().equals(KeyCode.ENTER))
//            btnSendAction(null);
    }

    public void listMessagesDragDrop(Event event) {
        Dragboard db = ((DragEvent)event).getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            String filePath = null;
            for (File file:db.getFiles())
            {
                filePath = file.getAbsolutePath();
                Controller2.client.sendFileMessage(false, Controller2.activeChatId, file);
//                        System.out.println(filePath);
            }
        }
        ((DragEvent)event).setDropCompleted(success);
        ((DragEvent)event).consume();
    }
}
