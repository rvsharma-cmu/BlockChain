package lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class BlockHelper {

    public static byte[] blockToData (Object obj) {
        byte[] res = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            res = Hex.encodeHexString(bos.toByteArray()).getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

	public static Object buildBlockFromArray (byte[] byteArray) {
        Object res = null;
        if (byteArray == null) {
            return null;
        }
        try {
            byte[] cur = Hex.decodeHex(new String(byteArray));
            ByteArrayInputStream bis = new ByteArrayInputStream(cur);
            ObjectInputStream ois = new ObjectInputStream(bis);
            res = ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (DecoderException e){
            e.printStackTrace();
        }
        return res;
    }
    
    @SuppressWarnings("unchecked")
	public static List<Block> buildBlock(byte[] arr){

        Object res = null;
        if (arr == null) {
            return null;
        }
        try {
            byte[] cur = Hex.decodeHex(new String(arr));
            ByteArrayInputStream bis = new ByteArrayInputStream(cur);
            ObjectInputStream ois = new ObjectInputStream(bis);
            res = ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (DecoderException e){
            e.printStackTrace();
        }
        return (List<Block>)res;
    }

}
