import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class demo {

    public static void main(String[] args) {
//        try {
//            String str = "Jv2fBHXFcs5AbbybSwuTKv4vQbtSP7Xd_XznXc5PuKU"; // 要解密的字符串
//            String key = "UgeI13Vfui9Je1CNPjVPqkiZ5kmNMVya9dCkPER5g39J9bw5615upKpTQCtW15Qn"; // 密钥，必须是8字节
//
//            // 使用 URL 安全的 Base64 解码
//            byte[] decodedBytes = Base64.getUrlDecoder().decode(str);
//
//            // 处理密钥，确保为 8 字节
//            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
//            keyBytes = Arrays.copyOf(keyBytes, 8); // 截断或填充至 8 字节
//
//            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "DES");
//
//            // 使用 DES/ECB/PKCS5Padding 模式解密
//            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
//            cipher.init(Cipher.DECRYPT_MODE, secretKey);
//
//            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
//            String decryptedString = new String(decryptedBytes, StandardCharsets.UTF_8);
//            System.out.println("Decrypted: " + decryptedString);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
//            String encrypt = encrypt("校内库房自有仓", "UgeI13Vfui9Je1CNPjVPqkiZ5kmNMVya9dCkPER5g39J9bw5615upKpTQCtW15Qn");
//            System.out.println(encrypt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public  String encrypt(String str, String key) throws Exception {
        // 处理密钥（必须为8字节）
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        keyBytes = Arrays.copyOf(keyBytes, 8); // 保持与PHP一致的密钥处理

        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "DES");

        // 配置加密器
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // 执行加密
        byte[] encryptedBytes = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));

        // URL安全的Base64编码（自动替换 +/ 为 -_，并去除填充）
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes);
    }

}
