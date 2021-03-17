package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class NioServer {

    static List<SocketChannel> channelList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9000));
        // 设置 ServerSocketChannel 为非阻塞
        serverSocketChannel.configureBlocking(false);
        System.out.println("服务启动成功");

        while (true){
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null){
                //有客户端连接
                System.out.println("连接成功");
                // 设置 SocketChannel 为非阻塞
                socketChannel.configureBlocking(false);
                channelList.add(socketChannel);

                // 如果连接的客户端很多，但大部分都没有发送数据，那么会导致很多次的空轮询，浪费时间
                for (SocketChannel sc : channelList) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                    int len = sc.read(byteBuffer);
                    if (len > 0) {
                        System.out.println("接受到消息：" + new String(byteBuffer.array()));
                    } else if (len == -1) {
                        channelList.remove(sc);
                        System.out.println("客户端连接断开");
                    }
                }
            }
        }
    }
}
