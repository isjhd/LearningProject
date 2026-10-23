package github.javaguide.compress.gzip;

import github.javaguide.compress.Compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author wangtao .
 * @createTime on 2020/10/3
 */

public class GzipCompress implements Compress {

    // 定义4KB缓冲区（BUFFER_SIZE = 1024 * 4），解压缩时逐段读取字节，避免大内存占用；
    private static final int BUFFER_SIZE = 1024 * 4;

    /**
     * GZIP 是一种无损数据压缩算法
     * 1.纯流式压缩，适合网络传输 / RPC 数据（不生成文件，只压缩字节）
     * 2.压缩率高、速度快，是 Java 网络通信、RPC 框架的首选压缩方式
     * 3.和 ZIP 区别：GZIP 只压缩单条数据流，ZIP 是压缩文件包
     */

    @Override
    public byte[] compress(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes is null");
        }
        // ByteArrayOutputStream：内存字节输出流，把压缩后的数据存在内存中
        // GZIP 压缩流，包装底层流，实现 GZIP 算法压缩。
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(out)) {

            // 核心压缩：将原始字节数组写入GZIP压缩流
            gzip.write(bytes);
            // 刷新缓冲区，将缓存的数据强制输出
            gzip.flush();
            // 完成压缩：写入GZIP结束标记，必须调用，否则压缩数据不完整
            gzip.finish();
            // 将内存中的压缩数据转为字节数组返回
            return out.toByteArray();
        } catch (IOException e) {
            // 包装为运行时异常抛出，简化调用方的异常处理
            throw new RuntimeException("gzip compress error", e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes is null");
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPInputStream gunzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            // 创建缓冲区数组，用于分批读取解压数据
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;

            // 核心循环：分批读取解压后的数据，写入内存输出流
            // read(buffer) 返回实际读取的字节数，-1表示读取完毕
            while ((n = gunzip.read(buffer)) > -1) {
                // 写入：缓冲区 → 输出流（只写入实际读取的有效字节）
                out.write(buffer, 0, n);
            }
            // 将内存中的解压完成的数据，转为字节数组返回
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("gzip decompress error", e);
        }
    }
}
