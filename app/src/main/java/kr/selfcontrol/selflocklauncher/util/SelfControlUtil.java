package kr.selfcontrol.selflocklauncher.util;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by owner on 2015-12-24.
 */
public class SelfControlUtil {

    public static String getTimeToString(long time){
        time=time/1000;
        StringBuilder sb=new StringBuilder();
        if((int)(time/3600/24)>0){
            sb.append((int)time/3600/24+"days ");
        }
        time=time%(3600*24);
        if((int)(time/3600)>0){
            sb.append((int)time/3600+"hours ");
        }

        time=time%(3600);
        if((int)(time/60)>0){
            sb.append((int)time/60+"minitues ");
        }

        time=time%(60);
        if((int)(time)>0){
            sb.append((int)time+"seconds ");
        }
        return sb.toString();
    }
    public static String md5(String str){
        String MD5 = "";
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte byteData[] = md.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++){
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }
            MD5 = sb.toString();

        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            MD5 = null;
        }
        return MD5;
    }
    final static String secretKey   = "12345678901234567890123456789012"; //32bit
    static String IV                = "1234567890123456"; //16bit

    public static String encode(String str)  {
        try {
            byte[] keyData = secretKey.getBytes();

            SecretKey secureKey = new SecretKeySpec(keyData, "AES");

            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(IV.getBytes()));

            byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
            String enStr = new String(Base64.encode(encrypted, Base64.DEFAULT));
            return enStr;
        }catch(Exception exc){}
        return "ErrorError";
    }

    public static String decode(String str){
        try {
            byte[] keyData = secretKey.getBytes();
            SecretKey secureKey = new SecretKeySpec(keyData, "AES");
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(IV.getBytes("UTF-8")));

            byte[] byteStr = Base64.decode(str.getBytes(), Base64.DEFAULT);
            return new String(c.doFinal(byteStr),"UTF-8");
        }catch(Exception exc){}
        return "ErrorError";
    }
}
