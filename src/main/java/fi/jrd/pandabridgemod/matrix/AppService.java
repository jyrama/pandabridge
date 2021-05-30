package fi.jrd.pandabridgemod.matrix;

import fi.jrd.pandabridgemod.PandabridgeMod;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class AppService {
    private final EventLoopGroup workerGroup;
    private final EventLoopGroup bossGroup;
    private ChannelFuture binding;

    public AppService() {
        workerGroup = new NioEventLoopGroup();
        bossGroup = new NioEventLoopGroup();
    }

    public void run() throws InterruptedException {

        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();

                            p.addLast(new HttpServerCodec());
                            p.addLast(new HttpObjectAggregator(10000));
                            p.addLast(new AppServiceHttpHandler());
                        }
                    });

            binding = b.bind(PandabridgeMod.getApplicationServicePort()).sync();
            // not closing the future

        } finally {
            PandabridgeMod.logger.info("AppService done");
            // nor shutting down the groups here gracefully
        }
    }

    public void shutdown() throws InterruptedException {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();

        binding.channel().closeFuture().sync(); // possibly throws
    }

}
