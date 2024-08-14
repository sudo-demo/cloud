package com.example.common.filter;



import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 构建可重复读取inputStream的request
 *
 * @author ruoyi
 */
public class RepeatedlyRequestWrapper extends HttpServletRequestWrapper {
    // 存储请求体字节数据的数组
    private final byte[] requestBodyBytes;
    // ByteArrayInputStream 用于多次读取字节数据
    private final ByteArrayInputStream bais;

    /**
     * 构造函数，包装 HttpServletRequest 对象，并读取请求体内容。
     *
     * @param request 被包装的 HttpServletRequest 对象
     * @throws IOException 如果读取请求体时发生 I/O 错误
     */
    public RepeatedlyRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 使用 try-with-resources 语句自动管理资源，确保输入流被正确关闭
        try (InputStream inputStream = request.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024]; // 缓冲区大小为 1024
            int bytesRead;
            // 读取请求体到缓冲区，直到流结束
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            // 将缓冲区内容转换为字节数组
            requestBodyBytes = baos.toByteArray();
        }
        // 初始化 ByteArrayInputStream，用于后续的重复读取
        bais = new ByteArrayInputStream(requestBodyBytes);
    }

    /**
     * 重写 getInputStream 方法，返回一个可以重复读取的 ServletInputStream。
     *
     * @return 包装后的 ServletInputStream 对象
     * @throws IOException 如果创建 ServletInputStream 时发生 I/O 错误
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStream() {
            // 标记流是否已经读取完毕
            @Override
            public boolean isFinished() {
                return true; // 对于非异步请求，此方法可以返回 true
            }

            // 标记流是否准备好被读取
            @Override
            public boolean isReady() {
                return true; // 对于非异步请求，此方法可以返回 true
            }

            // 设置读监听器，用于异步 IO 操作
            @Override
            public void setReadListener(ReadListener readListener) {
                // 此实现不支持异步 IO，留空
            }

            // 读取单个字节的数据
            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }

    /**
     * 重写 getReader 方法，返回一个可以重复读取的 BufferedReader。
     *
     * @return 包装后的 BufferedReader 对象
     * @throws IOException 如果创建 BufferedReader 时发生 I/O 错误
     */
    @Override
    public BufferedReader getReader() throws IOException {
        // 使用请求的字符编码创建 InputStreamReader
        String charEncoding = getCharacterEncoding();
        if (charEncoding == null) {
            // 如果请求没有指定字符编码，使用默认编码 UTF-8
            charEncoding = Charset.defaultCharset().name();
        }
        return new BufferedReader(new InputStreamReader(getInputStream(), charEncoding));
    }

    // 如果需要支持特定的字符编码，可以重写这个方法
    @Override
    public String getCharacterEncoding() {
        return super.getCharacterEncoding();
    }

    /**
     * 获取请求体作为 JSON 字符串。
     * 此方法假设请求体是 JSON 格式。
     *
     * @return 请求体的 JSON 字符串
     */
    public String getRequestParam() {
        // 确保使用请求编码获取字符串，如果未设置则使用默认 UTF-8 编码
        String characterEncoding = getCharacterEncoding();
        if (characterEncoding == null || characterEncoding.isEmpty()) {
            characterEncoding = StandardCharsets.UTF_8.name();
        }
        try {
            return new String(requestBodyBytes, characterEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
