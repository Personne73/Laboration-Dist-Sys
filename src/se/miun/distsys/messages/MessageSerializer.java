package se.miun.distsys.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MessageSerializer {

    public byte[] serializeMessage(Message message) {
        ByteArrayOutputStream bos;
        ObjectOutputStream oos;
        byte[] byteArray = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeUnshared(message);            
            oos.flush();            
			oos.close();
			bos.close();
            byteArray = bos.toByteArray();

        } catch (Exception e) {
        	e.printStackTrace();
        }
        oos = null;
        bos = null;
        return byteArray;
    }

    public Message deserializeMessage(byte[] byteRepresentation) {
        if (byteRepresentation == null) {
            return null;
        }        
        ByteArrayInputStream bis;
        ObjectInputStream ois;
        Message message;
        try {

            bis = new ByteArrayInputStream(byteRepresentation);
            ois = new ObjectInputStream(bis);
            message = (Message) ois.readUnshared();
            ois.close();
            bis.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return message;
    }
}
