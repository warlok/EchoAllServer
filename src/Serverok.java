import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 * Created by dean on 7/24/15.
 */
public class Serverok {

    public static void main(String[] args) throws IOException {

        Vector<SocketChannel> connections = new Vector<>();

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(9595));
        ssc.configureBlocking(false);

        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();
            Set<SelectionKey> keySet = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = keySet.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                if (key.isValid()) {
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    connections.add(sc);
                    sc.register(key.selector(), SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel sc = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

                    int read = sc.read(buffer);
                    if (read ==-1) {
                        continue;
                    }
                    buffer.flip();
                    for(int i=0; i< buffer.limit(); i++) {
                        buffer.put(i, buffer.get(i));
                    }

                    for (Iterator<SocketChannel> iterChannel =
                                 connections.iterator(); iterChannel.hasNext();) {
                        SocketChannel channel = iterChannel.next();
                            try {
                                channel.write(buffer);
                                buffer.rewind();
                            } catch (java.io.IOException e) {
                                if (!channel.isConnected()) {
                                    System.out.println("Connection closed");
                                    iterChannel.remove();
                                }
                            }

                    }
                }
            } }
        }

    }

}
