package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class NioSelectorServer {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9000));
        serverSocketChannel.configureBlocking(false);
        // 打开 Selector 处理 Channel；即创建 epoll
        Selector selector = Selector.open();
        // 把 ServerSocketChannel 注册到 selector 上，并且 selector 对客户端 accept 连接感兴趣
        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务启动成功");

        while (true) {
            // 阻塞等待需要处理的事件发生
            selector.select();

            // 获取 selector 中注册的全部事件的 SelectionKey 实例
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            // 遍历 SelectionKey 对事件进行处理
            for (SelectionKey key : selectionKeys) {
                if (key.isAcceptable()) {
                    // 如果是 OP_ACCEPT 事件，则进行连接获取和事件注册
                    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = serverChannel.accept();
                    socketChannel.configureBlocking(false);
                    // 这里只注册了读事件，如果需要给客户端发送消息可以注册写事件
                    SelectionKey selectionKey1 = socketChannel.register(selector, SelectionKey.OP_WRITE);
                    System.out.println("客户端连接成功");
                } else if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                    int len = socketChannel.read(byteBuffer);
                    if (len > 0) {
                        System.out.println("接受到消息：" + new String(byteBuffer.array()));
                    } else if (len == -1) {
                        System.out.println("客户端断开连接");
                        socketChannel.close();
                    }
                }

                // 从事件集合中删除本次处理的 key，防止下次 select 重复处理
                selectionKeys.remove(key);
            }


        }
    }
}
