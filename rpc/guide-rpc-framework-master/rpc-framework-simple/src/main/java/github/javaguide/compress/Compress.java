package github.javaguide.compress;

import github.javaguide.extension.SPI;

/**
 *
 * @author wangtao .
 * @createTime on 2020/10/3
 * @description 基于 Java 实现的通用压缩 / 解压缩组件
 */

@SPI
public interface Compress {

    /**
     * 压缩字节数组
     */
    byte[] compress(byte[] bytes);

    /**
     * 解压缩字节数组
     */
    byte[] decompress(byte[] bytes);
}
