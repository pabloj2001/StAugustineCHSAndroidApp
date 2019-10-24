package ca.staugustinechs.staugustineapp;

public class TitanTagEncryption {

    public static String encrypt(String data){
        int time = getTime();
        byte[] bytes = data.getBytes();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append((char) (b ^ (time & 0x000000FF)));
        }
        //System.out.println(time + ": " + sb.toString());
        return sb.toString();
    }

    public static String decrypt(String data){
        int time = getTime();
        char[] chars = data.toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            bytes[i] = (byte) (((byte) chars[i]) ^ time);
        }
        //System.out.println(new String(bytes));
        return new String(bytes);
    }

    public static int getTime(){
        //return 12;
        return (int) Math.ceil(System.currentTimeMillis() / 1000 / 5);
    }

}
