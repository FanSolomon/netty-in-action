package nia.chapter5;

import io.netty.buffer.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ByteProcessor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Random;

import static io.netty.channel.DummyChannelHandlerContext.DUMMY_INSTANCE;

/**
 * Created by kerr.
 *
 * Listing 5.1 Backing array
 *
 * Listing 5.2 Direct buffer data access
 *
 * Listing 5.3 Composite buffer pattern using ByteBuffer
 *
 * Listing 5.4 Composite buffer pattern using CompositeByteBuf
 *
 * Listing 5.5 Accessing the data in a CompositeByteBuf
 *
 * Listing 5.6 Access data
 *
 * Listing 5.7 Read all data
 *
 * Listing 5.8 Write data
 *
 * Listing 5.9 Using ByteBufProcessor to find \r
 *
 * Listing 5.10 Slice a ByteBuf
 *
 * Listing 5.11 Copying a ByteBuf
 *
 * Listing 5.12 get() and set() usage
 *
 * Listing 5.13 read() and write() operations on the ByteBuf
 *
 * Listing 5.14 Obtaining a ByteBufAllocator reference
 *
 * Listing 5.15 Reference counting
 *
 * Listing 5.16 Release reference-counted object
 */
public class ByteBufExamples {
    private final static Random random = new Random();
    private static final ByteBuf BYTE_BUF_FROM_SOMEWHERE = Unpooled.buffer(1024);
//    private static final ByteBuf BYTE_BUF_FROM_SOMEWHERE = Unpooled.directBuffer(1024);
    private static final Channel CHANNEL_FROM_SOMEWHERE = new NioSocketChannel();
    private static final ChannelHandlerContext CHANNEL_HANDLER_CONTEXT_FROM_SOMEWHERE = DUMMY_INSTANCE;
    /**
     * Listing 5.1 Backing array
     */
    public static void heapBuffer() {
        ByteBuf heapBuf = BYTE_BUF_FROM_SOMEWHERE; //get reference form somewhere
        if (heapBuf.hasArray()) {
            System.out.println("Is heapBuf!");
            byte x[] = {0x01, 0x01, 0x02, 0x71, 0x2b, 0x5f, 0x30};
            heapBuf.writeBytes(x);
            byte[] array = heapBuf.array();
            int offset = heapBuf.arrayOffset() + heapBuf.readerIndex();
            int length = heapBuf.readableBytes();
            System.out.println("offset is:" + offset + " and length is:" + length);
            ByteBuf byteBuf =heapBuf.readBytes(5);
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            System.out.println("byteBuf.toString()：" + bytes);
            System.out.println("heapBuf.readByte()" + heapBuf.readByte());
            offset = heapBuf.arrayOffset() + heapBuf.readerIndex();
            length = heapBuf.readableBytes();
            System.out.println("offset is:" + offset + " and length is:" + length);
            handleArray(array, offset, length);
        }
    }

    /**
     * Listing 5.2 Direct buffer data access
     */
    public static void directBuffer() {
        ByteBuf directBuf = BYTE_BUF_FROM_SOMEWHERE; //get reference form somewhere
        if (!directBuf.hasArray()) {
            System.out.println("Is directBuf!");
            byte x[] = {0x01, 0x01, 0x02, 0x71, 0x2b, 0x5f, 0x30};
            directBuf.writeBytes(x);
            int length = directBuf.readableBytes();
            byte[] array = new byte[length];
            directBuf.getBytes(directBuf.readerIndex(), array);
            System.out.println("length is:" + length);
            handleArray(array, 0, length);
        }
    }

    /**
     * Listing 5.3 Composite buffer pattern using ByteBuffer
     */
    public static void byteBufferComposite(ByteBuffer header, ByteBuffer body) {
        // Use an array to hold the message parts
        ByteBuffer[] message =  new ByteBuffer[]{ header, body };

        // Create a new ByteBuffer and use copy to merge the header and body
        ByteBuffer message2 =
                ByteBuffer.allocate(header.remaining() + body.remaining());
        message2.put(header);
        message2.put(body);
        message2.flip();
    }


    /**
     * Listing 5.4 Composite buffer pattern using CompositeByteBuf
     */
    public static void byteBufComposite() {
        CompositeByteBuf messageBuf = Unpooled.compositeBuffer();
        ByteBuf headerBuf = BYTE_BUF_FROM_SOMEWHERE; // can be backing or direct
        ByteBuf bodyBuf = BYTE_BUF_FROM_SOMEWHERE;   // can be backing or direct
        messageBuf.addComponents(headerBuf, bodyBuf);
        //...
        messageBuf.removeComponent(0); // remove the header
        for (ByteBuf buf : messageBuf) {
            System.out.println("Start " + buf.toString() + " End");
        }
    }

    /**
     * Listing 5.5 Accessing the data in a CompositeByteBuf
     */
    public static void byteBufCompositeArray() {
        CompositeByteBuf compBuf = Unpooled.compositeBuffer();
        int length = compBuf.readableBytes();
        byte[] array = new byte[length];
        compBuf.getBytes(compBuf.readerIndex(), array);
        handleArray(array, 0, array.length);
    }

    /**
     * Listing 5.6 Access data
     */
    public static void byteBufRelativeAccess() {
        ByteBuf buffer = BYTE_BUF_FROM_SOMEWHERE; //get reference form somewhere
        byte x[] = {0x01, 0x01, 0x02, 0x71, 0x2b, 0x5f, 0x30};
        buffer.writeBytes(x);
        for (int i = 0; i < buffer.capacity(); i++) {
            byte b = buffer.getByte(i);
            System.out.print((char) b);
        }
        int offset = buffer.arrayOffset() + buffer.readerIndex();
        int length = buffer.readableBytes();
        System.out.println();
        System.out.println("offset is:" + offset + " and length is:" + length);
    }

    /**
     * Listing 5.7 Read all data
     */
    public static void readAllData() {
        ByteBuf buffer = BYTE_BUF_FROM_SOMEWHERE; //get reference form somewhere
        byte x[] = {0x01, 0x01, 0x02, 0x71, 0x2b, 0x5f, 0x30};
        buffer.writeBytes(x);
        while (buffer.isReadable()) {
            System.out.println(buffer.readByte());
        }
    }

    /**
     * Listing 5.8 Write data
     */
    public static void write() {
        // Fills the writable bytes of a buffer with random integers.
        ByteBuf buffer = BYTE_BUF_FROM_SOMEWHERE; //get reference form somewhere
        while (buffer.writableBytes() >= 4) {
            buffer.writeInt(random.nextInt());
        }
    }

    /**
     * Listing 5.9 Using ByteProcessor to find \r
     *
     * use {@link io.netty.buffer.ByteBufProcessor in Netty 4.0.x}
     */
    public static void byteProcessor() {
        ByteBuf buffer = BYTE_BUF_FROM_SOMEWHERE; //get reference form somewhere
        byte x[] = {0x01, 0x01, 0x02, 0x71, 0x2b, 0x5f, 0x30};
        buffer.writeBytes(x);
        int index = buffer.forEachByte(ByteProcessor.FIND_CR);
        int index2 = buffer.forEachByte(new ByteProcessor.IndexOfProcessor((byte) '_'));
        System.out.println("index:" + index + " and index2 is:" + index2);
    }

    /**
     * Listing 5.9 Using ByteBufProcessor to find \r
     *
     * use {@link io.netty.util.ByteProcessor in Netty 4.1.x}
     */
    public static void byteBufProcessor() {
        ByteBuf buffer = BYTE_BUF_FROM_SOMEWHERE; //get reference form somewhere
        int index = buffer.forEachByte(ByteBufProcessor.FIND_CR);
    }

    /**
     * Listing 5.10 Slice a ByteBuf
     */
    public static void byteBufSlice() {
        Charset utf8 = Charset.forName("UTF-8");
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        ByteBuf sliced = buf.slice(0, 15);
        System.out.println(sliced.toString(utf8));
        buf.setByte(0, (byte)'J');
        assert buf.getByte(0) == sliced.getByte(0);
        boolean result = buf.getByte(0) == sliced.getByte(0);
        System.out.println("buf.getByte(0) == sliced.getByte(0)   " + result);
        System.out.println(buf.toString(utf8));
        System.out.println(sliced.toString(utf8));
    }

    /**
     * Listing 5.11 Copying a ByteBuf
     */
    public static void byteBufCopy() {
        Charset utf8 = Charset.forName("UTF-8");
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        ByteBuf copy = buf.copy(0, 15);
        System.out.println(copy.toString(utf8));
        buf.setByte(0, (byte)'J');
        assert buf.getByte(0) != copy.getByte(0);
        boolean result = buf.getByte(0) == copy.getByte(0);
        System.out.println("buf.getByte(0) == copy.getByte(0)   " + result);
        System.out.println(buf.toString(utf8));
        System.out.println(copy.toString(utf8));
    }

    /**
     * Listing 5.12 get() and set() usage
     */
    public static void byteBufSetGet() {
        Charset utf8 = Charset.forName("UTF-8");
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        System.out.println((char)buf.getByte(0));
        int readerIndex = buf.readerIndex();
        int writerIndex = buf.writerIndex();
        buf.setByte(0, (byte)'B');
        System.out.println((char)buf.getByte(0));
        System.out.println(buf.toString(utf8));
        assert readerIndex == buf.readerIndex();
        assert writerIndex == buf.writerIndex();
        System.out.println("readerIndex == buf.readerIndex()   " + (readerIndex == buf.readerIndex()));
        System.out.println("writerIndex == buf.writerIndex()   " + (writerIndex == buf.writerIndex()));
    }

    /**
     * Listing 5.13 read() and write() operations on the ByteBuf
     */
    public static void byteBufWriteRead() {
        Charset utf8 = Charset.forName("UTF-8");
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        System.out.println((char)buf.readByte());
        int readerIndex = buf.readerIndex();
        int writerIndex = buf.writerIndex();
        buf.writeByte((byte)'?');
        System.out.println(buf.toString(utf8));
        assert readerIndex == buf.readerIndex();
        assert writerIndex != buf.writerIndex();
        System.out.println("readerIndex == buf.readerIndex()   " + (readerIndex == buf.readerIndex()));
        System.out.println("writerIndex == buf.writerIndex()   " + (writerIndex == buf.writerIndex()));
    }

    private static void handleArray(byte[] array, int offset, int len) {
        System.out.println("Do something to handleArray...");
    }

    /**
     * Listing 5.14 Obtaining a ByteBufAllocator reference
     */
    public static void obtainingByteBufAllocatorReference(){
        Channel channel = CHANNEL_FROM_SOMEWHERE; //get reference form somewhere
        ByteBufAllocator allocator = channel.alloc();
        //...
        ChannelHandlerContext ctx = CHANNEL_HANDLER_CONTEXT_FROM_SOMEWHERE; //get reference form somewhere
        ByteBufAllocator allocator2 = ctx.alloc();
        //...
    }

    /**
     * Listing 5.15 Reference counting
     * */
    public static void referenceCounting(){
        Channel channel = CHANNEL_FROM_SOMEWHERE; //get reference form somewhere
        ByteBufAllocator allocator = channel.alloc();
        //...
        ByteBuf buffer = allocator.directBuffer();
        assert buffer.refCnt() == 1;
        System.out.println("buffer.refCnt():" + buffer.refCnt());
        //...
    }

    /**
     * Listing 5.16 Release reference-counted object
     */
    public static void releaseReferenceCountedObject(){
        ByteBuf buffer = BYTE_BUF_FROM_SOMEWHERE; //get reference form somewhere
        boolean released = buffer.release();
        //...
    }


}
